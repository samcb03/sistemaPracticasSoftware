package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.common.MonthlyReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateMonthlyReportController extends ValidationHandler {

    private static final Logger LOGGER
        = Logger.getLogger(FXMLGenerateMonthlyReportController.class.getName());

    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay alumno en sesión";
    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte.";
    private static final String ACTIVITY_FIELD_LABEL = "Actividad ";
    private static final String OBSERVATION_FIELD_LABEL = "Observación ";
    private static final String REPORT_VIEWER_TITLE = "Reporte Mensual";
    private static final String EMPTY_TEXT = "";

    private static final int MAX_ACTIVITY_INPUTS = 7;
    private static final int HOURS_PER_ACTIVITY_ENTRY = 1;
    private static final int INITIAL_REPORTED_HOURS = 0;
    private static final int ROW_NUMBER_OFFSET = 1;

    @FXML private Label labelStudentName;
    @FXML private Label labelCoordinatorName;
    @FXML private Label labelProfessorName;
    @FXML private Label labelMessage;
    @FXML private Label labelMonth;
    @FXML private Label labelReportedHours;
    @FXML private Label labelAccumulatedHours;
    @FXML private Label labelSection;
    @FXML private Label labelSubject;
    @FXML private Label labelNumberReport;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonBack;

    @FXML private TextField textFieldActivity1;
    @FXML private TextField textFieldActivity2;
    @FXML private TextField textFieldActivity3;
    @FXML private TextField textFieldActivity4;
    @FXML private TextField textFieldActivity5;
    @FXML private TextField textFieldActivity6;
    @FXML private TextField textFieldActivity7;

    @FXML private TextField textFieldObservation1;
    @FXML private TextField textFieldObservation2;
    @FXML private TextField textFieldObservation3;
    @FXML private TextField textFieldObservation4;
    @FXML private TextField textFieldObservation5;
    @FXML private TextField textFieldObservation6;
    @FXML private TextField textFieldObservation7;

    private TextField[] activityFields;
    private TextField[] observationFields;

    private MonthlyReportCommon monthlyReportCommon;
    private Student currentStudent;
    private ReportContextDAO reportContextDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        monthlyReportCommon = new MonthlyReportCommon();
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        reportContextDAO = new ReportContextDAO();

        initializeFieldArrays();
        loadStudentData();
    }

    private void initializeFieldArrays() {
        activityFields = new TextField[] {
            textFieldActivity1, textFieldActivity2, textFieldActivity3,
            textFieldActivity4, textFieldActivity5, textFieldActivity6,
            textFieldActivity7
        };
        observationFields = new TextField[] {
            textFieldObservation1, textFieldObservation2, textFieldObservation3,
            textFieldObservation4, textFieldObservation5, textFieldObservation6,
            textFieldObservation7
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
            MonthlyReport context = reportContextDAO.getMonthlyReportData(
                currentStudent.getIdStudent());
            populateContextLabels(context);

            String accumulatedHours = reportContextDAO.getTotalReportedHoursByStudentId(
                currentStudent.getIdStudent());
            labelAccumulatedHours.setText(accumulatedHours);
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE,
                "Error al cargar contexto del reporte mensual", operationException);
            showError(operationException.getMessage());
        }
    }

    private void populateContextLabels(MonthlyReport context) {
        labelStudentName.setText(context.getStudentName());
        labelCoordinatorName.setText(context.getCoordinatorName());
        labelProfessorName.setText(context.getProfessorName());
        labelMonth.setText(context.getMonth());
        labelSection.setText(context.getSection());
        labelSubject.setText(context.getNrcSubject());
        labelNumberReport.setText(String.valueOf(context.getReportNumber()));
    }

    @FXML
    public void validateMonthlyReport() {
        Optional<String> validationError = validateFields();
        handleValidation(validationError, this::generateMonthlyReport);
    }

    private Optional<String> validateFields() {
        Optional<String> validationResult = Optional.empty();

        for (int index = 0; index < MAX_ACTIVITY_INPUTS && validationResult.isEmpty(); index++) {
            validationResult = validateActivityRow(index);
        }
        return validationResult;
    }

    private Optional<String> validateActivityRow(int index) {
        int rowNumber = index + ROW_NUMBER_OFFSET;
        String activityText = activityFields[index].getText().trim();
        String observationText = observationFields[index].getText().trim();

        Optional<String> activityValidation = InputValidator.validateText(
            activityText, ACTIVITY_FIELD_LABEL + rowNumber);
        Optional<String> observationValidation = InputValidator.validateText(
            observationText, OBSERVATION_FIELD_LABEL + rowNumber);

        return activityValidation.or(() -> observationValidation);
    }

    private void generateMonthlyReport() {
        try {
            MonthlyReport report = buildReport();
            JasperPrint jasperPrint = monthlyReportCommon.generateMonthlyReport(report);
            showSuccess(REPORT_GENERATED_MESSAGE);
            displayReport(jasperPrint);
            clearFields();
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE,
                "Error de operación al generar el reporte mensual", operationException);
            showError(operationException.getMessage());
        } catch (JRException jasperException) {
            LOGGER.log(Level.SEVERE,
                "Error de JasperReports al generar el reporte mensual", jasperException);
            showError(REPORT_GENERATION_ERROR);
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle(REPORT_VIEWER_TITLE);
        viewer.setVisible(true);
    }

    private MonthlyReport buildReport() {
        MonthlyReport report = new MonthlyReport();

        report.setStudentName(
            currentStudent.getFirstName() + " " + currentStudent.getLastName());
        report.setStudentId(currentStudent.getIdStudent());

        int totalReportedHours = INITIAL_REPORTED_HOURS;
        for (int index = 0; index < MAX_ACTIVITY_INPUTS; index++) {
            String activityText = activityFields[index].getText().trim();
            String observationText = observationFields[index].getText().trim();
            report.addActivityEntry(report.getPeriod(), activityText, observationText);

            if (!activityText.isEmpty()) {
                totalReportedHours += HOURS_PER_ACTIVITY_ENTRY;
            }
        }

        report.setReportedHours(totalReportedHours);
        return report;
    }

    @Override
    protected void clearFields() {
        for (TextField field : activityFields) {
            field.clear();
        }
        for (TextField field : observationFields) {
            field.clear();
        }
        labelMessage.setText(EMPTY_TEXT);
    }
}