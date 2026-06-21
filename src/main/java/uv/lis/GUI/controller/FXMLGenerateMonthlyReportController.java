package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.MAX_HOURS_PER_PARTIAL_REPORT;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.common.MonthlyReportCommon;
import uv.lis.logic.dao.AdvanceDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.Advance;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateMonthlyReportController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLGenerateMonthlyReportController.class.getName());

    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay alumno en sesión";
    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte.";
    private static final String VALIDATED_REPORT_MESSAGE = "Error al verificar reporte mensual";
    private static final String LOAD_ACTIVITIES = "Error al cargar actividades";
    private static final String REPORT_VIEWER_TITLE = "Reporte Mensual";
    private static final String MINIMUN_ACTIVITY_MESSAGE = "El reporte debe contener mínimo una actividad";
    private static final String LIMITED_HOURS_REPORT ="Has alcanzado el límite de 420 horas acumuladas. " 
                                                    + "No es posible generar más reportes";
    private static final String PARTIAL_HOURS_LIMITED = "Las horas reportadas no pueden exceder las " 
                                                    + MAX_HOURS_PER_PARTIAL_REPORT + " horas";
    private static final String DUPLICATED_REPORT_MESSAGE = "Ya se ha generado un reporte para el mes de ";
    private static final String EMPTY_TEXT = "";
    private static final String REPORT_BLOCK = "7";
    private static final String FIRST_PERIOD_SUFFIX = "51";
    private static final String SECOND_PERIOD_SUFFIX = "01";

    private static final int MAX_ACTIVITY_INPUTS = 7;
    private static final int MONTH_JANUARY = 1;
    private static final int MONTH_FEBRUARY = 2;
    private static final int MONTH_MARCH = 3;
    private static final int MONTH_APRIL = 4;
    private static final int MONTH_MAY = 5;
    private static final int MONTH_JUNE = 6;
    private static final int MONTH_JULY = 7;
    private static final int MONTH_AUGUST = 8;
    private static final int MONTH_SEPTEMBER = 9;
    private static final int MONTH_OCTOBER = 10;
    private static final int MONTH_NOVEMBER = 11;
    private static final int MONTH_DECEMBER = 12;
    private static final int MONTH_UNKNOWN = -1;
    private static final int INITIAL_REPORTED_HOURS = 0;
    private static final int MAX_ACCUMULATED_HOURS = 420;

    private static final List<String> MONTH_NAMES_FIRST_PERIOD = List.of(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio"
    );

    private static final List<String> MONTH_NAMES_SECOND_PERIOD = List.of(
        "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private static final Map<String, Integer> MONTH_NUMBERS_BY_NAME = Map.ofEntries(
        Map.entry("enero", MONTH_JANUARY),
        Map.entry("febrero", MONTH_FEBRUARY),
        Map.entry("marzo", MONTH_MARCH),
        Map.entry("abril", MONTH_APRIL),
        Map.entry("mayo", MONTH_MAY),
        Map.entry("junio", MONTH_JUNE),
        Map.entry("julio", MONTH_JULY),
        Map.entry("agosto", MONTH_AUGUST),
        Map.entry("septiembre", MONTH_SEPTEMBER),
        Map.entry("octubre", MONTH_OCTOBER),
        Map.entry("noviembre", MONTH_NOVEMBER),
        Map.entry("diciembre", MONTH_DECEMBER)
    );

    @FXML private Label labelStudentName;
    @FXML private Label labelCoordinatorName;
    @FXML private Label labelProfessorName;
    @FXML private Label labelMessage;
    @FXML private Label labelReportedHours;
    @FXML private Label labelAccumulatedHours;
    @FXML private Label labelSection;
    @FXML private Label labelBlock;
    @FXML private Label labelSubject;
    @FXML private Label labelNumberReport;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonRegisterActivity;
    @FXML private Button buttonBack;
    @FXML private Label labelActivity1;
    @FXML private Label labelActivity2;
    @FXML private Label labelActivity3;
    @FXML private Label labelActivity4;
    @FXML private Label labelActivity5;
    @FXML private Label labelActivity6;
    @FXML private Label labelActivity7;
    @FXML private Label labelObservation1;
    @FXML private Label labelObservation2;
    @FXML private Label labelObservation3;
    @FXML private Label labelObservation4;
    @FXML private Label labelObservation5;
    @FXML private Label labelObservation6;
    @FXML private Label labelObservation7;
    @FXML private ComboBox<String> comboBoxMonth;

    private Label[] activityFields;
    private Label[] observationFields;

    private MonthlyReportCommon monthlyReportCommon;
    private Student currentStudent;
    private ReportContextDAO reportContextDAO;
    private AdvanceDAO advanceDAO;
    private String schoolPeriod;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        monthlyReportCommon = new MonthlyReportCommon();
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        reportContextDAO = new ReportContextDAO();
        advanceDAO = new AdvanceDAO();

        initializeFieldArrays();
        labelBlock.setText(REPORT_BLOCK);
        loadStudentData();

        comboBoxMonth.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldMonth, newMonth) -> {
            if (newMonth != null) {
                loadRegisteredActivities();
            }
        }
        );
    }

    @FXML
    public void validateMonthlyReport() {
        String studentId = currentStudent.getIdStudent();
        Optional<String> selectedMonth = getSelectedMonth();

        if (selectedMonth.isEmpty()) {
            showError("Seleccione un mes antes de generar el reporte.");
        } else {
            String currentMonth = selectedMonth.get();
            String accumulatedText = labelAccumulatedHours.getText();
            String reportedText = labelReportedHours.getText();
            int accumulatedHours = Integer.parseInt(accumulatedText);
            int hoursReported = Integer.parseInt(reportedText);

            if(!hasAtLeastOneActivity()) {
                showError(MINIMUN_ACTIVITY_MESSAGE);
            } else if(!accumulatedText.isEmpty() && accumulatedHours >= MAX_ACCUMULATED_HOURS) {
                showError(LIMITED_HOURS_REPORT);
            } else if (!reportedText.isEmpty() && hoursReported >= MAX_HOURS_PER_PARTIAL_REPORT) {
                showError(PARTIAL_HOURS_LIMITED);
            } else {
                boolean duplicated = false;
                try {
                    duplicated = reportContextDAO.hasReportAlreadyBeenGenerated(studentId, currentMonth);
                    if (duplicated) {
                        showError(DUPLICATED_REPORT_MESSAGE + currentMonth);
                    } else {
                        generateMonthlyReport();
                    }
                } catch (OperationException e) {
                    showError(VALIDATED_REPORT_MESSAGE);
                }
            }
        }
    }

    private void initializeFieldArrays() {
        activityFields = new Label[] {
            labelActivity1, labelActivity2, labelActivity3,
            labelActivity4, labelActivity5, labelActivity6,
            labelActivity7
        };
        observationFields = new Label[] {
            labelObservation1, labelObservation2, labelObservation3,
            labelObservation4, labelObservation5, labelObservation6,
            labelObservation7
        };
    }

    private void loadStudentData() {
        if (currentStudent == null) {
            showError(NO_STUDENT_IN_SESSION_MESSAGE);
        } else {
            fetchAndDisplayContext();
        }
    }

    private void fetchAndDisplayContext() {
        try {
            MonthlyReport context = reportContextDAO.getMonthlyReportData(currentStudent.getIdStudent());
            populateContextLabels(context);

        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al cargar contexto del reporte mensual", operationException);
            showError(operationException.getMessage());
        }
    }

    private void loadRegisteredActivities() {
        Optional<String> selectedMonth = getSelectedMonth();

        if(selectedMonth.isPresent()) {
            try {
                MonthlyReport context = reportContextDAO.getMonthlyReportData(currentStudent.getIdStudent());
                
                int month = convertMonthNameToNumber(getSelectedMonth().get());
                int year = LocalDate.now().getYear(); 

                List<Activity> registered = reportContextDAO.getRecordedActivitiesByMonth(
                    context.getIdProject(), month, year);
                    
                clearFields(); 

                for (int i = 0; i < registered.size() && i < activityFields.length; i++) {
                    activityFields[i].setText(registered.get(i).getName());
                    observationFields[i].setText(registered.get(i).getDescription());
                }
                int totalHours = reportContextDAO.getSumOfReportedHours(context.getIdProject(), month, year);
                labelReportedHours.setText(String.valueOf(totalHours));

                int previouslyAccumulatedHours = advanceDAO.getAccumulatedHoursByProject(context.getIdProject());
                labelAccumulatedHours.setText(String.valueOf(previouslyAccumulatedHours));
                buttonRegisterActivity.setDisable(registered.size() >= MAX_ACTIVITY_INPUTS);

            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE,LOAD_ACTIVITIES, e);
                showError(LOAD_ACTIVITIES);
            }
        }
    }

    private void loadAvailableMonth() {
        List<String> availableMonths;

        if(schoolPeriod !=null && schoolPeriod.endsWith(FIRST_PERIOD_SUFFIX)) {
            availableMonths = MONTH_NAMES_FIRST_PERIOD;
        } else if(schoolPeriod !=null && schoolPeriod.endsWith(SECOND_PERIOD_SUFFIX)) {
            availableMonths = MONTH_NAMES_SECOND_PERIOD;
        } else {
            availableMonths = List.of();
            LOGGER.log(Level.WARNING, "El periodo escolar no es válido: {0}", schoolPeriod);
        }
        comboBoxMonth.setItems(FXCollections.observableArrayList(availableMonths));
    }

    private void populateContextLabels(MonthlyReport context) {
        labelStudentName.setText(context.getStudentName());
        labelCoordinatorName.setText(context.getCoordinatorName());
        labelProfessorName.setText(context.getProfessorName());
        labelSection.setText(context.getSection());
        labelSubject.setText(context.getNrcSubject());
        labelNumberReport.setText(String.valueOf(context.getReportNumber()));
        schoolPeriod = context.getPeriod();
        loadAvailableMonth();
    }

    private void generateMonthlyReport() {
        Optional<String> selectedMonthOpt = getSelectedMonth();
        if(selectedMonthOpt.isEmpty()) {
            showError("Seleccione un mes antes de generar el reporte");
        } else {
            try {

                String selectedMonth = selectedMonthOpt.get();
                int month = convertMonthNameToNumber(selectedMonth);
                int year = LocalDate.now().getYear();

                MonthlyReport context = reportContextDAO.getMonthlyReportData(currentStudent.getIdStudent());
                MonthlyReport report = buildReport(selectedMonth);

                JasperPrint jasperPrint = monthlyReportCommon.generateMonthlyReport(report);

                int previousAccumulated = advanceDAO.getAccumulatedHoursByProject(context.getIdProject());
                int currentReported = reportContextDAO.getSumOfReportedHours(context.getIdProject(), month, year);
                int newAccumulated = previousAccumulated + currentReported;

                if (!advanceDAO.existsAdvanceForReport(context.getIdReport())) {
                    Advance advance = new Advance();
                    advance.setProjectId(context.getIdProject());
                    advance.setReportId(report.getId());
                    advance.setWeekNumber(context.getReportNumber());
                    advance.setAccumulatedHours(newAccumulated);
                    advanceDAO.registerAdvance(advance);

                } else {
                    LOGGER.log(Level.INFO, "Ya existe un avance registrado para el reporte ID {0}. "
                        + "Se omitirá el registro de avance.", context.getIdReport());
                }

                showSuccess(REPORT_GENERATED_MESSAGE);
                displayReport(jasperPrint);
                clearFields();
                loadRegisteredActivities();

            } catch (OperationException operationException) {
                LOGGER.log(Level.SEVERE, "Error de operación al generar el reporte mensual", operationException);
                showError(operationException.getMessage());
            } catch (JRException jasperException) {
                LOGGER.log(Level.SEVERE, "Error de JasperReports al generar el reporte mensual", jasperException);
                showError(REPORT_GENERATION_ERROR);
            }
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle(REPORT_VIEWER_TITLE);
        viewer.setVisible(true);
    }

    private MonthlyReport buildReport(String selectedMonth) {
        MonthlyReport report = new MonthlyReport();
        report.setStudentName(currentStudent.getFirstName() + " " + currentStudent.getLastName());
        report.setStudentId(currentStudent.getIdStudent());
        report.setMonth(selectedMonth);
        report.setBlock(REPORT_BLOCK);
        report.setSection(labelSection.getText());

        int totalReportedHours = INITIAL_REPORTED_HOURS;
        for (int index = 0; index < MAX_ACTIVITY_INPUTS; index++) {
            String activityText = activityFields[index].getText().trim();
            String observationText = observationFields[index].getText().trim();
            report.addActivityEntry(schoolPeriod, activityText, observationText);
        }

        String hoursText = labelReportedHours.getText();
        if (!hoursText.isEmpty()) {
            totalReportedHours = Integer.parseInt(hoursText);
        }
        report.setReportedHours(totalReportedHours);
        
        String accumulatedHoursText = labelAccumulatedHours.getText();
        if (!accumulatedHoursText.isEmpty()) {
            report.setAccumulatedHours(Integer.parseInt(accumulatedHoursText));
        }
        return report;
    }
    
    private int convertMonthNameToNumber(String monthName) {
        int monthNumber = MONTH_NUMBERS_BY_NAME.getOrDefault(monthName.toLowerCase(), MONTH_UNKNOWN);
        return monthNumber;
    }
    
    private Optional<String> getSelectedMonth() {
        Optional<String> selectedMonth;
        String selected = comboBoxMonth.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            selectedMonth = Optional.empty();
        } else {
            selectedMonth = Optional.of(selected);
        }
        return selectedMonth;
    }

    @FXML
    public void goToRegisterActivity(javafx.event.ActionEvent event) {
        FXMLLoader loader = this.navigateToWithLoader("/uv/lis/GUI/view/FXMLRegisterActivity.fxml");
        if (loader != null) {
            Stage registerStage = (Stage) ((Parent) loader.getRoot()).getScene().getWindow();
            registerStage.setOnHidden(e -> loadRegisteredActivities());
        }
    }

    private boolean hasAtLeastOneActivity() {
        boolean hasActivity = false;
        for (Label activityField : activityFields) {
            if (!activityField.getText().trim().isEmpty()) {
                hasActivity = true;
            }
        }
        return hasActivity;
    }

    @Override
    protected void clearFields() {
        for (Label label : activityFields) {
            label.setText(EMPTY_TEXT);;
        }
        for (Label label : observationFields) {
            label.setText(EMPTY_TEXT);;
        }
        labelMessage.setText(EMPTY_TEXT);
    }
}