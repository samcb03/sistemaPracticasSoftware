package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
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
import uv.lis.logic.common.FinalReportCommon;
import uv.lis.logic.dto.ActivityProgress;
import uv.lis.logic.dto.DeliverableResult;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;

public class FXMLGenerateReportController extends ValidationHandler {

    private static final Logger LOGGER
        = Logger.getLogger(FXMLGenerateReportController.class.getName());

    private static final String PARTIAL_REPORT = "Reporte Parcial";
    private static final String FINAL_REPORT = "Reporte Final";

    private static final String REPORT_GENERATED_MESSAGE = "Reporte generado correctamente.";
    private static final String REPORT_GENERATION_ERROR = "Error al generar el reporte";

    private final FinalReportCommon finalReportCommon = new FinalReportCommon();

    @FXML private Label labelMessage;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonBack;
    @FXML private ComboBox<String> comboBoxReportType;
    @FXML private TextField textFieldActivity1;
    @FXML private TextField textFieldAdvance1;
    @FXML private TextArea textAreaObservation1;
    @FXML private TextField textFieldActivity2;
    @FXML private TextField textFieldAdvance2;
    @FXML private TextArea textAreaObservation2;
    @FXML private TextField textFieldResult1;
    @FXML private TextField textFieldResultAdvance1;
    @FXML private TextArea textAreaObservationResult1;
    @FXML private TextField textFieldResult2;
    @FXML private TextField textFieldResultAdvance2;
    @FXML private TextArea textAreaObservationResult2;
    @FXML private TextArea textAreaGeneralObservations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        loadReportTypes();
    }

    private void loadReportTypes() {
        comboBoxReportType.setItems(FXCollections.observableArrayList(
            PARTIAL_REPORT,
            FINAL_REPORT
        ));
    }

    @FXML
    public void validatorReport() {
        validateFields();
    }

    private void validateFields() {
        Optional<String> reportTypeValidation = validateReportType();
        Optional<String> firstActivityValidation = validateFirstActivityBlock();
        Optional<String> secondActivityValidation = validateSecondActivityBlock();
        Optional<String> firstDeliverableValidation = validateFirstDeliverableBlock();
        Optional<String> secondDeliverableValidation = validateSecondDeliverableBlock();
        Optional<String> generalObservationsValidation = InputValidator.validateText(
            textAreaGeneralObservations.getText(), "Observaciones Generales");

        Optional<String> validationError = reportTypeValidation
            .or(() -> firstActivityValidation)
            .or(() -> secondActivityValidation)
            .or(() -> firstDeliverableValidation)
            .or(() -> secondDeliverableValidation)
            .or(() -> generalObservationsValidation);

        handleValidation(validationError, this::generateReport);
    }

    private Optional<String> validateReportType() {
        String reportType = comboBoxReportType.getSelectionModel().getSelectedItem();
        return InputValidator.validateComboBox(reportType, "un tipo de reporte");
    }
    //FIXME cambiar estos metodos con lambas 
    private Optional<String> validateFirstActivityBlock() {
        Optional<String> activityValidation = InputValidator.validateText(
            textFieldActivity1.getText(), "Actividad 1");
        Optional<String> advanceValidation = InputValidator.validatePositiveInteger(
            textFieldAdvance1.getText(), "Porcentaje de Avance de Actividad 1");
        Optional<String> observationValidation = InputValidator.validateText(
            textAreaObservation1.getText(), "Observación de Actividad 1");

        return activityValidation
            .or(() -> advanceValidation)
            .or(() -> observationValidation);
    }

    private Optional<String> validateSecondActivityBlock() {
        Optional<String> activityValidation = InputValidator.validateText(
            textFieldActivity2.getText(), "Actividad 2");
        Optional<String> advanceValidation = InputValidator.validatePositiveInteger(
            textFieldAdvance2.getText(), "Porcentaje de Avance de Actividad 2");
        Optional<String> observationValidation = InputValidator.validateText(
            textAreaObservation2.getText(), "Observación de Actividad 2");

        return activityValidation
            .or(() -> advanceValidation)
            .or(() -> observationValidation);
    }

    private Optional<String> validateFirstDeliverableBlock() {
        Optional<String> resultValidation = InputValidator.validateText(
            textFieldResult1.getText(), "Entregable 1");
        Optional<String> advanceValidation = InputValidator.validatePositiveInteger(
            textFieldResultAdvance1.getText(), "Porcentaje de Avance de Entregable 1");
        Optional<String> observationValidation = InputValidator.validateText(
            textAreaObservationResult1.getText(), "Observación de Entregable 1");

        return resultValidation
            .or(() -> advanceValidation)
            .or(() -> observationValidation);
    }

    private Optional<String> validateSecondDeliverableBlock() {
        Optional<String> resultValidation = InputValidator.validateText(
            textFieldResult2.getText(), "Entregable 2");
        Optional<String> advanceValidation = InputValidator.validatePositiveInteger(
            textFieldResultAdvance2.getText(), "Porcentaje de Avance de Entregable 2");
        Optional<String> observationValidation = InputValidator.validateText(
            textAreaObservationResult2.getText(), "Observación de Entregable 2");

        return resultValidation
            .or(() -> advanceValidation)
            .or(() -> observationValidation);
    }

    private void generateReport() {
        try {
            FinalReport finalReport = buildFinalReport();
            JasperPrint jasperPrint = finalReportCommon.generateFinalReport(finalReport);
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

        return finalReport;
    }

    private ActivityProgress buildFirstActivity() {
        ActivityProgress activityProgress = new ActivityProgress();
        activityProgress.setName(textFieldActivity1.getText().trim());
        activityProgress.setAdvancePercentage(textFieldAdvance1.getText().trim());
        activityProgress.setObservations(textAreaObservation1.getText().trim());
        return activityProgress;
    }

    private ActivityProgress buildSecondActivity() {
        ActivityProgress activityProgress = new ActivityProgress();
        activityProgress.setName(textFieldActivity2.getText().trim());
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
        comboBoxReportType.getSelectionModel().clearSelection();

        textFieldActivity1.clear();
        textFieldAdvance1.clear();
        textAreaObservation1.clear();

        textFieldActivity2.clear();
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
}