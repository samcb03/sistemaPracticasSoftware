package uv.lis.GUI.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import uv.lis.logic.dao.ActivityDAO;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.common.FinalReportCommon;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.ActivityProgress;
import uv.lis.logic.dto.DeliverableResult;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateFinalReportController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLGenerateFinalReportController.class.getName());

    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte";

    private final FinalReportCommon finalReportCommon = new FinalReportCommon();
    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @FXML private Label labelMessage;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonBack;
    @FXML private TextField textFieldAdvance1;
    @FXML private TextArea textAreaObservation1;
    @FXML private TextField textFieldAdvance2;
    @FXML private TextArea textAreaObservation2;
    @FXML private TextField textFieldResult1;
    @FXML private TextField textFieldResultAdvance1;
    @FXML private TextArea textAreaObservationResult1;
    @FXML private TextField textFieldResult2;
    @FXML private TextField textFieldResultAdvance2;
    @FXML private TextArea textAreaObservationResult2;
    @FXML private TextArea textAreaGeneralObservations;
    @FXML private ComboBox<String> comboBoxActivity1;
    @FXML private ComboBox<String> comboBoxActivity2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        loadStudentActivities();
    }

    @FXML
    public void validatorReport() {
        validateFields();
    }

    private void validateFields() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateFirstActivityBlock(),
            validateSecondActivityBlock(),
            validateFirstDeliverableBlock(),
            validateSecondDeliverableBlock(),
            InputValidator.validateText(textAreaGeneralObservations.getText(), "Observaciones Generales")
        );

        Optional<String> validationError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        handleValidation(validationError, this::generateReport);
    }

    private Optional<String> validateFirstActivityBlock() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateComboBox(comboBoxActivity1.getValue(), "Actividad 1"),
            InputValidator.validatePositiveInteger(textFieldAdvance1.getText(), 
                "Porcentaje de Avance de Actividad 1"),
            InputValidator.validateText(textAreaObservation1.getText(), "Observación de Actividad 1")
        );

        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        return firstError;
    }

    private Optional<String> validateSecondActivityBlock() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateComboBox(comboBoxActivity2.getValue(), "Actividad 2"),
            InputValidator.validatePositiveInteger(textFieldAdvance2.getText(), 
                "Porcentaje de Avance de Actividad 2"),
            InputValidator.validateText(textAreaObservation2.getText(), "Observación de Actividad 2")
        );

        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        return firstError;
    }

    private Optional<String> validateFirstDeliverableBlock() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateText(textFieldResult1.getText(), "Entregable 1"),
            InputValidator.validatePositiveInteger(textFieldResultAdvance1.getText(), 
                "Porcentaje de Avance de Entregable 1"),
            InputValidator.validateText(textAreaObservationResult1.getText(), "Observación de Entregable 1")
        );

        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        return firstError;
    }

    private Optional<String> validateSecondDeliverableBlock() {
        Stream<Optional<String>> validationStream = Stream.of(
            InputValidator.validateText(textFieldResult2.getText(), "Entregable 2"),
            InputValidator.validatePositiveInteger(textFieldResultAdvance2.getText(), 
                "Porcentaje de Avance de Entregable 2"),
            InputValidator.validateText(textAreaObservationResult2.getText(), "Observación de Entregable 2")
        );

        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        return firstError;
    }

    private void generateReport() {
        try {
            FinalReport finalReport = buildFinalReport();
            JasperPrint jasperPrint = finalReportCommon.generateFinalReport(finalReport);
            persistReport(finalReport);
 
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

    private void persistReport(FinalReport finalReport) throws OperationException {
        boolean isRegistered = reportDAO.registerFinalReport(finalReport);
 
        if (!isRegistered) {
            LOGGER.log(Level.WARNING, "El reporte final no se guardó en la base de datos");
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("Reporte Final");
        viewer.setVisible(true);
    }

    private FinalReport buildFinalReport() {
        FinalReport finalReport = new FinalReport();
 
        finalReport.setFirstActivity(buildFirstActivity());
        finalReport.setSecondActivity(buildSecondActivity());
        finalReport.setFirstDeliverable(buildFirstDeliverable());
        finalReport.setSecondDeliverable(buildSecondDeliverable());
        finalReport.setGeneralObservations(textAreaGeneralObservations.getText().trim());
 
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent != null) {
            finalReport.setStudentId(currentStudent.getIdStudent());
        }
 
        return finalReport;
    }

    private ActivityProgress buildFirstActivity() {
        ActivityProgress activityProgress = new ActivityProgress();
        activityProgress.setName(comboBoxActivity1.getValue());
        activityProgress.setAdvancePercentage(textFieldAdvance1.getText().trim());
        activityProgress.setObservations(textAreaObservation1.getText().trim());
        return activityProgress;
    }

    private ActivityProgress buildSecondActivity() {
        ActivityProgress activityProgress = new ActivityProgress();
        activityProgress.setName(comboBoxActivity2.getValue());
        activityProgress.setAdvancePercentage(textFieldAdvance2.getText().trim());
        activityProgress.setObservations(textAreaObservation2.getText().trim());
        return activityProgress;
    }

    private DeliverableResult buildFirstDeliverable() {
        DeliverableResult deliverableResult = new DeliverableResult();
        deliverableResult.setResult(textFieldResult1.getText().trim());
        deliverableResult.setAdvancePercentage(textFieldResultAdvance1.getText().trim());
        deliverableResult.setObservations(textAreaObservationResult1.getText().trim());
        return deliverableResult;
    }

    private DeliverableResult buildSecondDeliverable() {
        DeliverableResult deliverableResult = new DeliverableResult();
        deliverableResult.setResult(textFieldResult2.getText().trim());
        deliverableResult.setAdvancePercentage(textFieldResultAdvance2.getText().trim());
        deliverableResult.setObservations(textAreaObservationResult2.getText().trim());
        return deliverableResult;
    }

    @Override
    protected void clearFields() {

        comboBoxActivity1.getSelectionModel().clearSelection();
        textFieldAdvance1.clear();
        textAreaObservation1.clear();

        comboBoxActivity2.getSelectionModel().clearSelection();;
        textFieldAdvance2.clear();
        textAreaObservation2.clear();

        textFieldResult1.clear();
        textFieldResultAdvance1.clear();
        textAreaObservationResult1.clear();

        textFieldResult2.clear();
        textFieldResultAdvance2.clear();
        textAreaObservationResult2.clear();

        textAreaGeneralObservations.clear();

        labelMessage.setText("");
    }

    private void loadStudentActivities() {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent != null) {
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
            List<ComboBox<String>> comboBoxActivities =
                List.of(comboBoxActivity1, comboBoxActivity2);

            for (ComboBox<String> comboBoxActivity : comboBoxActivities) {
                comboBoxActivity.setItems(activityNames);
            }
            preselectLastActivities(comboBoxActivities, activityNames);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar actividades", e);
            showError("Error al cargar las actividades registradas");
        }
    }

    private void preselectLastActivities(List<ComboBox<String>> comboBoxActivities,
            ObservableList<String> activityNames) {
        int available = Math.min(activityNames.size(), comboBoxActivities.size());

        for (int slot = 0; slot < available; slot++) {
            int nameIndex = activityNames.size() - available + slot;
            comboBoxActivities.get(slot).setValue(activityNames.get(nameIndex));
        }
    }

    @FXML
    public void goToRegisterActivity(javafx.event.ActionEvent event) {
        FXMLLoader loader = this.navigateToWithLoader("/uv/lis/GUI/view/FXMLRegisterActivity.fxml");
        if (loader != null) {
            Stage registerStage = (Stage) ((Parent) loader.getRoot()).getScene().getWindow();
            registerStage.setOnHidden(e -> loadStudentActivities());
        }
    }
}