package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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


//FIXME : No 

public class FXMLGenerateMonthlyReportController extends ValidationHandler {

    @FXML private Label     labelStudentName;
    @FXML private Label     labelCoordinatorName;
    @FXML private Label     professorName;
    @FXML private Label     labelMessage;
    @FXML private Label     labelMonth;
    @FXML private Label     labelReportedHours;
    @FXML private Label     labelAccumulatedHours;
    @FXML private Label     labelSection;
    @FXML private Label     labelSubject;
    @FXML private Label     labelNumberReport;
    @FXML private Button    buttonGenerate;
    @FXML private Button    buttonBack;

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

    private TextField[] activities;
    private TextField[] observations;

    private MonthlyReportCommon monthlyReportCommon;
    private Student currentStudent;
    private ReportContextDAO reportContextDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        monthlyReportCommon = new MonthlyReportCommon();
        currentStudent      = SessionManager.getInstance().getCurrentStudent();
        reportContextDAO = new ReportContextDAO();

        activities = new TextField[]{
            textFieldActivity1, textFieldActivity2, textFieldActivity3,
            textFieldActivity4, textFieldActivity5, textFieldActivity6,
            textFieldActivity7
        };
        observations = new TextField[]{
            textFieldObservation1, textFieldObservation2, textFieldObservation3,
            textFieldObservation4, textFieldObservation5, textFieldObservation6,
            textFieldObservation7
        };

        loadStudentData();
    }

    private void loadStudentData() {
        if(currentStudent == null) {
            showError("No hay alumno en sesión");
        } else {
            try {
                MonthlyReport context = reportContextDAO.getMonthlyReportData(
                    currentStudent.getIdStudent());
                labelStudentName.setText(context.getStudentName());
                labelCoordinatorName.setText(context.getCoordinadorName());
                professorName.setText(context.getProfessorName());
                labelMonth.setText(context.getMonth());
                labelSection.setText(context.getSection());
                labelSubject.setText(context.getNrcSubject());
                labelNumberReport.setText(String.valueOf(context.getReportNumber()));

                String accumulatedHours = reportContextDAO.getTotalReportedHoursByStudentId(currentStudent.getIdStudent());
                labelAccumulatedHours.setText(accumulatedHours);

            } catch(OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void validateMonthlyReport() {
        Optional<String> validationError = validateFields();
        handleValidation(validationError, this::generateMonthlyReport);
    }

    private Optional<String> validateFields() {
        Optional<String> validationResult = Optional.empty();
        for (int i = 0; i < activities.length && validationResult.isEmpty(); i++) {
            String activityText    = activities[i].getText().trim();
            String observationText = observations[i].getText().trim();

            Optional<String> validateActivity = InputValidator.validateText(
                activityText, "Actividad " + (i + 1));
            if (validateActivity.isPresent()) {
                validationResult = validateActivity;
            } else {
                Optional<String> validateObservation = InputValidator.validateText(
                    observationText, "Observación " + (i + 1));
                if (validateObservation.isPresent()) {
                    validationResult = validateObservation;
                }
            }
        }
        return validationResult;
    }

    private void generateMonthlyReport() {
        try {
            MonthlyReport report = buildReport();
            JasperPrint jasperPrint = monthlyReportCommon.generateMonthlyReport(report);
            showSuccess("Reporte generado correctamente.");
            displayReport(jasperPrint);
            clearFields();
        } catch (OperationException e) {
            showError(e.getMessage());
        } catch (JRException e) {
            e.printStackTrace();
            showError("Error al generar el reporte.");
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("Reporte Mensual");
        viewer.setVisible(true);
    }

    private MonthlyReport buildReport() {
        MonthlyReport report = new MonthlyReport();

        report.setStudentName(
            currentStudent.getFirstName() + " " + currentStudent.getLastName());

        int totalHours = 0;
        for (int i = 0; i < activities.length; i++) {
            String activity    = activities[i].getText().trim();
            String observation = observations[i].getText().trim();
            String period      = report.getPeriod();
            report.addActivity(period, activity, observation);
            if (!activity.isEmpty()) totalHours++; 
        }

        report.setReportedHours(totalHours); 
        return report;
    }

    @Override
    protected void clearFields() {
        for (TextField tf : activities)    tf.clear();
        for (TextField tf : observations)  tf.clear();
        labelMessage.setText("");
    }
}