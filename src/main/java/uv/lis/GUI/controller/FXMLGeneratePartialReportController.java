package uv.lis.GUI.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLGeneratePartialReportController extends ValidationHandler {

    private static final Logger LOGGER
        = Logger.getLogger(FXMLGeneratePartialReportController.class.getName());

    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte";
    private static final String NO_STUDENT_MESSAGE = "No hay un estudiante en sesión";
    private static final String LOAD_ACTIVITIES_ERROR = "No se pudieron cargar las actividades";
    private static final String PARTIAL_REPORT_TITLE = "Reporte Parcial";

    private static final int DEFAULT_REPORT_NUMBER = 1;
    private static final int FIRST_WEEK_SLOT = 0;
    private static final int INVALID_ADVANCE = 0;

    private final PartialReportCommon partialReportCommon = new PartialReportCommon();
    private final ActivityDAO activityDAO = new ActivityDAO();

    private ComboBox<String>[] comboBoxActivities;
    private TextField[] textFieldAdvances;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        groupComponents();
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
            List<Activity> activities = activityDAO.getActivitiesByStudentId(studentId);
            ObservableList<String> activityNames = FXCollections.observableArrayList(
                activities.stream()
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
            InputValidator.validateText(
                textAreaGeneralObservations.getText(), "Observaciones Generales"),
            InputValidator.validateText(
                textAreaResults.getText(), "Resultados Obtenidos")
        );

        Optional<String> validationError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        handleValidation(validationError, this::generateReport);
    }

    private Optional<String> validateFirstActivityRow() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateComboBox(
                comboBoxActivity1.getValue(), "una actividad"),
            InputValidator.validatePositiveInteger(
                textFieldAdvance1.getText(), "Porcentaje de Avance de Actividad 1")
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

            showSuccess(REPORT_GENERATED_MESSAGE);
            displayReport(jasperPrint);
            clearFields();
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE,
                "Error de operación al generar el reporte", operationException);
            showError(operationException.getMessage());
        } catch (JRException jasperException) {
            LOGGER.log(Level.SEVERE,
                "Error de JasperReports al generar el reporte", jasperException);
            showError(REPORT_GENERATION_ERROR);
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle(PARTIAL_REPORT_TITLE);
        viewer.setVisible(true);
    }

    /* The context fields (student, professor, NRC, project, supervisor, planned
       advance) are completed by PartialReportCommon through ReportContextDAO and
       the active session, just like the final report flow. */
    private PartialReport buildPartialReport() {
        PartialReport partialReport = new PartialReport();

        partialReport.setReportNumber(DEFAULT_REPORT_NUMBER);
        partialReport.setActivityName(comboBoxActivity1.getValue());
        partialReport.setResult(textAreaResults.getText().trim());
        partialReport.setObservations(textAreaGeneralObservations.getText().trim());

        fillActivityMatrices(partialReport);
        return partialReport;
    }

    private void fillActivityMatrices(PartialReport partialReport) {
        String[] activityNames = partialReport.getActivityNames();
        int[][] realAdvances = partialReport.getRealAdvances();

        for (int activityIndex = 0; activityIndex < comboBoxActivities.length; activityIndex++) {
            activityNames[activityIndex] = readActivityName(activityIndex);
            realAdvances[FIRST_WEEK_SLOT][activityIndex]
                = parseAdvance(textFieldAdvances[activityIndex].getText());
        }

        partialReport.setRealAdvanceWeek(realAdvances[FIRST_WEEK_SLOT][0]);
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
    }
}