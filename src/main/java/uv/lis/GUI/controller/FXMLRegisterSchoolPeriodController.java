package uv.lis.GUI.controller;

import static uv.lis.logic.utils.DateValidator.validatePeriodEndDate;
import static uv.lis.logic.utils.DateValidator.validatePeriodStartDate;
import static uv.lis.logic.utils.InputValidator.PERIOD_TERM_FALL;
import static uv.lis.logic.utils.InputValidator.PERIOD_TERM_SPRING;
import static uv.lis.logic.utils.InputValidator.validateComboBox;

import java.net.URL;
import java.sql.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterSchoolPeriodController extends ValidationHandler {

    private static final String TERM_FIELD = "El término del periodo";
    private static final String START_DATE_FIELD = "La fecha de inicio";
    private static final String SUCCESSFUL_SCHOOL_PERIOD_REGISTER_MESSAGE = "Periodo escolar registrado correctamente";
    private static final String ERROR_SCHOOL_PERIOD_REGISTER_MESSAGE = "Error al registrar el periodo escolar";

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private ComboBox<String> comboBoxTerm;
    @FXML private DatePicker datePickerStartDate;
    @FXML private DatePicker datePickerEndDate;

    private SchoolPeriodDAO schoolPeriodDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        schoolPeriodDAO = new SchoolPeriodDAO();
        comboBoxTerm.getItems().addAll(PERIOD_TERM_FALL, PERIOD_TERM_SPRING);
        setupControls(labelError, buttonBack);
    }

    @FXML
    public void validateFields() {
        Optional<String> validationError = getFirstValidationError();
        handleValidation(validationError, this::performRegistration);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateComboBox(comboBoxTerm.getValue(), TERM_FIELD),
            validatePeriodStartDate(comboBoxTerm.getValue(), datePickerStartDate.getValue(), START_DATE_FIELD),
            validatePeriodEndDate(comboBoxTerm.getValue(), datePickerStartDate.getValue(),
                datePickerEndDate.getValue())
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        try {
            SchoolPeriod schoolPeriod = buildSchoolPeriod();
            boolean isRegistered = schoolPeriodDAO.registerSchoolPeriod(schoolPeriod);

            if (isRegistered) {
                showSuccess(SUCCESSFUL_SCHOOL_PERIOD_REGISTER_MESSAGE);
                clearFields();
            } else {
                showError(ERROR_SCHOOL_PERIOD_REGISTER_MESSAGE);
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private SchoolPeriod buildSchoolPeriod() {
        int startYear = datePickerStartDate.getValue().getYear();
        String periodCode = startYear + comboBoxTerm.getValue();
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setName(periodCode);
        schoolPeriod.setStartDate(Date.valueOf(datePickerStartDate.getValue()));
        schoolPeriod.setEndDate(Date.valueOf(datePickerEndDate.getValue()));
        return schoolPeriod;
    }

    @Override
    protected void clearFields() {
        comboBoxTerm.getSelectionModel().clearSelection();
        datePickerStartDate.setValue(null);
        datePickerEndDate.setValue(null);
        comboBoxTerm.requestFocus();
    }
}