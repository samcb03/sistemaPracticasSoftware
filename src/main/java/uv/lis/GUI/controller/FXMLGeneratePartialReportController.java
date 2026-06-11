package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.WeeklyAdvanceGridEditor;
import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;
import uv.lis.logic.utils.WorkProgressCalculator;

public class FXMLGeneratePartialReportController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLGeneratePartialReportController.class.getName());

    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte";
    private static final String NO_STUDENT_MESSAGE = "No hay un estudiante en sesión";
    private static final String LOAD_ACTIVITIES_ERROR = "No se pudieron cargar las actividades";
    private static final String PARTIAL_REPORT_TITLE = "Reporte Parcial";
    private static final String DUPLICATE_ACTIVITY_MESSAGE
        = "La actividad ya fue seleccionada. Cada actividad solo puede elegirse una vez.";
    private static final String PLANNED_EDITOR_TITLE = "Editar Avance Esperado";
    private static final String REAL_EDITOR_TITLE = "Editar Avance Real";
    private static final String NO_ACTIVITY_SELECTED_MESSAGE 
        = "Selecciona al menos una actividad antes de editar el avance.";

    private static final int DEFAULT_REPORT_NUMBER = 1;
    private static final int INVALID_ADVANCE = 0;
    private static final int MAX_ADVANCE = 100;

    private final PartialReportCommon partialReportCommon = new PartialReportCommon();
    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @FXML private Label labelMessage;
    @FXML private Button buttonBack;

    @FXML private ComboBox<String> comboBoxActivity1;
    @FXML private ComboBox<String> comboBoxActivity2;
    @FXML private ComboBox<String> comboBoxActivity3;
    @FXML private ComboBox<String> comboBoxActivity4;
    @FXML private ComboBox<String> comboBoxActivity5;
    @FXML private ComboBox<String> comboBoxActivity6;

    @FXML private TextField textFieldAdvance1;
    @FXML private TextField textFieldAdvance2;
    @FXML private TextField textFieldAdvance3;
    @FXML private TextField textFieldAdvance4;
    @FXML private TextField textFieldAdvance5;
    @FXML private TextField textFieldAdvance6;

    @FXML private TextArea textAreaGeneralObservations;
    @FXML private TextArea textAreaResults;
    @FXML private AnchorPane paneAdvanceEditor;
    @FXML private Label labelAdvanceEditorTitle;
    @FXML private GridPane gridAdvanceEditor;
    @FXML private Label labelAdvanceEditorMessage;

    private ComboBox<String>[] comboBoxActivities;
    private TextField[] textFieldAdvances;
    private WeeklyAdvanceGridEditor advanceGridEditor;
    private List<Activity> studentActivities = new ArrayList<>();
    private int[][] plannedAdvances = new int[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
    private int[][] realAdvances = new int[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
    private boolean manualAdvances;
    private boolean editingPlanned;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        groupComponents();
        advanceGridEditor = new WeeklyAdvanceGridEditor(gridAdvanceEditor);
        loadStudentActivities();
    }

    @SuppressWarnings("unchecked")
    private void groupComponents() {
        comboBoxActivities = new ComboBox[] {
            comboBoxActivity1, comboBoxActivity2, comboBoxActivity3,
            comboBoxActivity4, comboBoxActivity5, comboBoxActivity6
        };
        textFieldAdvances = new TextField[] {
            textFieldAdvance1, textFieldAdvance2, textFieldAdvance3,
            textFieldAdvance4, textFieldAdvance5, textFieldAdvance6
        };
    }

    private void loadStudentActivities() {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            showError(NO_STUDENT_MESSAGE);
        } else {
            populateActivityComboBoxes(currentStudent.getIdStudent());
        }
    }

    private void populateActivityComboBoxes(String studentId) {
        try {
            studentActivities = activityDAO.getActivitiesByStudentId(studentId);
            ObservableList<String> activityNames = FXCollections.observableArrayList(
                studentActivities.stream()
                    .map(Activity::getName)
                    .collect(Collectors.toList()));

            for (ComboBox<String> comboBoxActivity : comboBoxActivities) {
                comboBoxActivity.setItems(activityNames);
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, LOAD_ACTIVITIES_ERROR, operationException);
            showError(LOAD_ACTIVITIES_ERROR);
        }
    }

    @FXML
    public void validatorReport() {
        validateFields();
    }

    private void validateFields() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateFirstActivityRow(),
            validateNoDuplicateActivities(),
            InputValidator.validateText(textAreaGeneralObservations.getText(), "Observaciones Generales"),
            InputValidator.validateText(textAreaResults.getText(), "Resultados Obtenidos")
        );

        Optional<String> validationError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        handleValidation(validationError, this::generateReport);
    }

    private Optional<String> validateNoDuplicateActivities() {
        Optional<String> duplicateError = Optional.empty();
        Set<String> selectedActivities = new HashSet<>();
        int index = 0;

        while (index < comboBoxActivities.length && duplicateError.isEmpty()) {
            String selectedActivity = comboBoxActivities[index].getValue();

            if (selectedActivity != null && !selectedActivities.add(selectedActivity)) {
                duplicateError = Optional.of(DUPLICATE_ACTIVITY_MESSAGE);
            }
            index++;
        }
        return duplicateError;
    }

    private Optional<String> validateFirstActivityRow() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateComboBox(comboBoxActivity1.getValue(), "una actividad"),
            InputValidator.validateMaxIntValue(
                textFieldAdvance1.getText(),  MAX_ADVANCE,"Porcentaje de Avance de Actividad 1")
        );

        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        return firstError;
    }

    private void generateReport() {
        try {
            PartialReport partialReport = buildPartialReport();
            JasperPrint jasperPrint = partialReportCommon.generatePartialReport(partialReport);
            persistReport(partialReport);
 
            showSuccess(REPORT_GENERATED_MESSAGE);
            displayReport(jasperPrint);
            clearFields();
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error de operación al generar el reporte", operationException);
            showError(operationException.getMessage());
        } catch (JRException jasperException) {
            LOGGER.log(Level.SEVERE, "Error de JasperReports al generar el reporte", jasperException);
            showError(REPORT_GENERATION_ERROR);
        }
    }

    private void persistReport(PartialReport partialReport) throws OperationException {
        boolean isRegistered = reportDAO.registerPartialReport(partialReport);
 
        if (!isRegistered) {
            LOGGER.log(Level.WARNING, "El reporte parcial no se guardó en la base de datos");
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle(PARTIAL_REPORT_TITLE);
        viewer.setVisible(true);
    }

    private PartialReport buildPartialReport() {
        PartialReport partialReport = new PartialReport();
 
        partialReport.setReportNumber(DEFAULT_REPORT_NUMBER);
        partialReport.setActivityName(comboBoxActivity1.getValue());
        partialReport.setResult(textAreaResults.getText().trim());
        partialReport.setObservations(textAreaGeneralObservations.getText().trim());
 
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent != null) {
            partialReport.setStudentId(currentStudent.getIdStudent());
        }
 
        fillActivityInputs(partialReport);
        applyManualAdvances(partialReport);
        return partialReport;
    }

    private void fillActivityInputs(PartialReport partialReport) {
        String[] activityNames = partialReport.getActivityNames();
        int[] realWeeklyAdvances = partialReport.getRealWeeklyAdvances();

        for (int activityIndex = 0; activityIndex < comboBoxActivities.length; activityIndex++) {
            activityNames[activityIndex] = readActivityName(activityIndex);
            realWeeklyAdvances[activityIndex] = parseAdvance(textFieldAdvances[activityIndex].getText());
        }
    }

    private String readActivityName(int activityIndex) {
        String selectedActivity = comboBoxActivities[activityIndex].getValue();
        return selectedActivity == null ? "" : selectedActivity;
    }

    private int parseAdvance(String rawValue) {
        int parsedAdvance = INVALID_ADVANCE;

        if (rawValue != null && !rawValue.isBlank()) {
            try {
                parsedAdvance = Integer.parseInt(rawValue.trim());
            } catch (NumberFormatException numberFormatException) {
                LOGGER.log(Level.WARNING, "Valor de avance no numérico: {0}", rawValue);
            }
        }
        return parsedAdvance;
    }

    @FXML
    public void openPlannedAdvanceEditor() {
        editingPlanned = true;
        prepareAndShowEditor();
    }

    @FXML
    public void openRealAdvanceEditor() {
        editingPlanned = false;
        prepareAndShowEditor();
    }

    private void prepareAndShowEditor() {
        Activity[] activitiesBySlot = resolveSelectedActivities();

        if (hasAnySelectedActivity(activitiesBySlot)) {
            if (!manualAdvances) {
                computeAutoAdvances(activitiesBySlot);
            }
            int[][] target = editingPlanned ? plannedAdvances : realAdvances;
            String editorTitle = editingPlanned ? PLANNED_EDITOR_TITLE : REAL_EDITOR_TITLE;

            advanceGridEditor.buildGrid(activitiesBySlot, target);
            labelAdvanceEditorTitle.setText(editorTitle);
            labelAdvanceEditorMessage.setText("");
            paneAdvanceEditor.setVisible(true);
        } else {
            showError(NO_ACTIVITY_SELECTED_MESSAGE);
        }
    }

    @FXML
    public void saveAdvanceEdits() {
        Optional<String> validationError = advanceGridEditor.validateAdvances();

        if (validationError.isPresent()) {
            labelAdvanceEditorMessage.setText(validationError.get());
        } else {
            int[][] target = editingPlanned ? plannedAdvances : realAdvances;

            advanceGridEditor.applyEditsTo(target);
            manualAdvances = true;
            labelAdvanceEditorMessage.setText("");
            paneAdvanceEditor.setVisible(false);
        }
    }

    @FXML
    public void cancelAdvanceEdits() {
        paneAdvanceEditor.setVisible(false);
    }

    private Activity[] resolveSelectedActivities() {
        Activity[] activitiesBySlot = new Activity[PartialReport.MAX_ACTIVITIES];

        for (int slot = 0; slot < comboBoxActivities.length; slot++) {
            String selectedName = comboBoxActivities[slot].getValue();
            activitiesBySlot[slot] = findActivityByName(selectedName);
        }
        return activitiesBySlot;
    }

    private Activity findActivityByName(String activityName) {
        Activity foundActivity = null;

        if (activityName != null) {
            int index = 0;

            while (index < studentActivities.size() && foundActivity == null) {
                Activity activity = studentActivities.get(index);

                if (activityName.equals(activity.getName())) {
                    foundActivity = activity;
                }
                index++;
            }
        }
        return foundActivity;
    }

    private boolean hasAnySelectedActivity(Activity[] activitiesBySlot) {
        boolean hasActivity = false;
        int index = 0;

        while (index < activitiesBySlot.length && !hasActivity) {
            if (activitiesBySlot[index] != null) {
                hasActivity = true;
            }
            index++;
        }
        return hasActivity;
    }

    private void computeAutoAdvances(Activity[] activitiesBySlot) {
        for (int slot = 0; slot < PartialReport.MAX_ACTIVITIES; slot++) {
            Activity activity = activitiesBySlot[slot];

            if (activity != null) {
                fillAutoColumn(activity, slot);
            }
        }
    }

    private void fillAutoColumn(Activity activity, int slot) {
        int activityWeeks = WorkProgressCalculator.calculateActivityWeeks(activity);
        int totalWeeks = Math.min(activityWeeks, PartialReport.MAX_WEEKS);
        int plannedWeekly = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);
        int writtenReal = parseAdvance(textFieldAdvances[slot].getText());
        int realWeekly = WorkProgressCalculator.calculateWeeklyRealAdvance(writtenReal, activity);

        for (int weekIndex = 0; weekIndex < totalWeeks; weekIndex++) {
            plannedAdvances[weekIndex][slot] = plannedWeekly;
            realAdvances[weekIndex][slot] = realWeekly;
        }
    }

    private void applyManualAdvances(PartialReport partialReport) {
        if (manualAdvances) {
            partialReport.setPlannedAdvances(plannedAdvances);
            partialReport.setRealAdvances(realAdvances);
            partialReport.setManualAdvances(true);
        }
    }

    @Override
    protected void clearFields() {
        for (ComboBox<String> comboBoxActivity : comboBoxActivities) {
            comboBoxActivity.getSelectionModel().clearSelection();
        }
        for (TextField textFieldAdvance : textFieldAdvances) {
            textFieldAdvance.clear();
        }

        textAreaGeneralObservations.clear();
        textAreaResults.clear();
        labelMessage.setText("");

        plannedAdvances = new int[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
        realAdvances = new int[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
        manualAdvances = false;
        paneAdvanceEditor.setVisible(false);
    }
}