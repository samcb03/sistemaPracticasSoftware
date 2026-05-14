package uv.lis.GUI.controller;


import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
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
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;


public class FXMLGenerateReportController extends ValidationHandler {

    private static final String PARTIAL_REPORT = "Reporte Parcial";
    private static final String FINAL_REPORT = "Reporte Final";

    private final FinalReportCommon finalReportCommon = new FinalReportCommon();

    @FXML private Label labelMessage;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonBack;
    @FXML private ComboBox<String> comboBoxReportType;
    @FXML private TextField textFieldActivity1;
    @FXML private TextField textFieldAdvance1;
    @FXML private TextArea  textAreaObservation1;
    @FXML private TextField textFieldActivity2;
    @FXML private TextField textFieldAdvance2;
    @FXML private TextArea  textAreaObservation2;
    @FXML private TextField textFieldResult1;
    @FXML private TextField textFieldResultAdvance1;
    @FXML private TextArea  textAreaObservationResult1;
    @FXML private TextField textFieldResult2;
    @FXML private TextField textFieldAdvanceResult2;
    @FXML private TextArea  textAreaObservationResult2;
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
    public void generateReport() {
        validateFields();
    }

    private void validateFields() {
        String reportType = comboBoxReportType.getSelectionModel().getSelectedItem();

        Optional<String> reportTypeValidation = InputValidator.validateComboBox(
            reportType, "un tipo de reporte"
        );

        Optional<String> activity1Validation = InputValidator.validateLettersOnly(
            textFieldActivity1.getText(), "Actividad 1"
        );
        Optional<String> advance1Validation = InputValidator.validatePositiveInteger(
            textFieldAdvance1.getText(), "Porcentaje de Avance de Actividad 1"
        );
        Optional<String> observation1Validation = InputValidator.validateLettersOnly(
            textAreaObservation1.getText(), "Observación de Actividad 1"
        );

        Optional<String> activity2Validation = InputValidator.validateLettersOnly(
            textFieldActivity2.getText(), "Actividad 2"
        );
        Optional<String> advance2Validation = InputValidator.validatePositiveInteger(
            textFieldAdvance2.getText(), "Porcentaje de Avance de Actividad 2"
        );
        Optional<String> observation2Validation = InputValidator.validateLettersOnly(
            textAreaObservation2.getText(), "Observación de Actividad 2"
        );

        Optional<String> result1Validation = InputValidator.validateLettersOnly(
            textFieldResult1.getText(), "Entregable 1"
        );
        Optional<String> resultAdvance1Validation = InputValidator.validatePositiveInteger(
            textFieldResultAdvance1.getText(), "Porcentaje de Avance de Entregable 1"
        );
        Optional<String> resultObservation1Validation = InputValidator.validateLettersOnly(
            textAreaObservationResult1.getText(), "Observación de Entregable 1"
        );

        Optional<String> result2Validation = InputValidator.validateLettersOnly(
            textFieldResult2.getText(), "Entregable 2"
        );
        Optional<String> resultAdvance2Validation = InputValidator.validatePositiveInteger(
            textFieldAdvanceResult2.getText(), "Porcentaje de Avance de Entregable 2"
        );
        Optional<String> resultObservation2Validation = InputValidator.validateLettersOnly(
            textAreaObservationResult2.getText(), "Observación de Entregable 2"
        );

        Optional<String> generalObservationsValidation = InputValidator.validateLettersOnly(
            textAreaGeneralObservations.getText(), "Observaciones Generales"
        );

        Optional<String> validationError = reportTypeValidation
            .or(() -> activity1Validation)
            .or(() -> advance1Validation)
            .or(() -> observation1Validation)
            .or(() -> activity2Validation)
            .or(() -> advance2Validation)
            .or(() -> observation2Validation)
            .or(() -> result1Validation)
            .or(() -> resultAdvance1Validation)
            .or(() -> resultObservation1Validation)
            .or(() -> result2Validation)
            .or(() -> resultAdvance2Validation)
            .or(() -> resultObservation2Validation)
            .or(() -> generalObservationsValidation);

        handleValidation(validationError, this::GenerateReport);
    }

    private void GenerateReport() {
        try {
            Report report = buildReport();
            JasperPrint jasperPrint = finalReportCommon.generateFinalReport(report);
            showSuccess("Reporte generado correctamente.");
            displayReport(jasperPrint);
            clearFields();
        } catch (OperationException e) {
            showError(e.getMessage());
        } catch (JRException e) {
            showError("Error al generar el reporte");
        }
    }

    private void displayReport(JasperPrint jasperPrint) {
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("Reporte Final");
        viewer.setVisible(true);
    }

    private Report buildReport() {
        Report report = new Report();

        report.setActivityName1(textFieldActivity1.getText().trim());
        report.setAdvancePercentageActivity1(textFieldAdvance1.getText().trim());
        report.setObservationsActivity1(textAreaObservation1.getText().trim());

        report.setActivityName2(textFieldActivity2.getText().trim());
        report.setAdvancePercentageActivity2(textFieldAdvance2.getText().trim());
        report.setObservationsActivity2(textAreaObservation2.getText().trim());

        report.setResult1(textFieldResult1.getText().trim());
        report.setResultAdvance1(textFieldResultAdvance1.getText().trim());
        report.setResultObservations1(textAreaObservationResult1.getText().trim());

        report.setResult2(textFieldResult2.getText().trim());
        report.setResultAdvance2(textFieldAdvanceResult2.getText().trim());
        report.setResultObservations2(textAreaObservationResult2.getText().trim());

        report.setGeneralObservations(textAreaGeneralObservations.getText().trim());

        return report;
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
        textFieldAdvanceResult2.clear();
        textAreaObservationResult2.clear();

        textAreaGeneralObservations.clear();

        labelMessage.setText("");
    }
}