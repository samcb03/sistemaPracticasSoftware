package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePhoneNumber;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validatePostalCode;
import static uv.lis.logic.utils.InputValidator.validateStreet;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterAffiliatedOrganizationController extends ValidationHandler {

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldCity;
    @FXML private TextField textFieldState;
    @FXML private TextField textFieldSector;
    @FXML private TextField textFieldStreet;
    @FXML private TextField textFieldStreetNumber;
    @FXML private TextField textFieldPostalCode;
    @FXML private TextField textFieldEmail;
    @FXML private TextField textFieldPhoneNumber;
    @FXML private TextField textFieldNumberOfDirectUsers;
    @FXML private TextField textFieldNumberOfIndirectUsers;

    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        setupControls(labelError, buttonBack);
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerAffiliatedOrganization);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldCity.getText(), "La ciudad"),
            validateText(textFieldState.getText(), "El estado"),
            validateText(textFieldSector.getText(), "El sector"),
            validateStreet(textFieldStreet.getText(), "La calle"),
            validatePositiveInteger(textFieldStreetNumber.getText().trim(), "El número de calle"),
            validatePostalCode(textFieldPostalCode.getText(), "El código postal"),
            validateEmail(textFieldEmail.getText().trim(), "El correo electrónico"),
            validatePhoneNumber(textFieldPhoneNumber.getText().trim(), "El número de teléfono"),
            validatePositiveInteger(textFieldNumberOfDirectUsers.getText().trim(), 
                "El número de usuarios directos"),
            validatePositiveInteger(textFieldNumberOfIndirectUsers.getText().trim(), 
                "El número de usuarios indirectos")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerAffiliatedOrganization() {
        AffiliatedOrganization organization = buildOrganization();
        try {
            boolean registrationSuccessful = affiliatedOrganizationDAO.registerOrganization(organization);
            if (registrationSuccessful) {
                showSuccess("Organización registrada correctamente");
                clearFields();
            } else {
                showError("Error al registrar la organización");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private AffiliatedOrganization buildOrganization() {
        AffiliatedOrganization organization = new AffiliatedOrganization();
        organization.setName(textFieldName.getText().trim());
        organization.setCity(textFieldCity.getText().trim());
        organization.setState(textFieldState.getText().trim());
        organization.setStreet(textFieldStreet.getText().trim());
        organization.setPostalCode(textFieldPostalCode.getText().trim());
        organization.setStreetNumber(textFieldStreetNumber.getText().trim());
        organization.setSector(textFieldSector.getText().trim());
        organization.setEmail(textFieldEmail.getText().trim());
        organization.setPhoneNumber(textFieldPhoneNumber.getText().trim());
        organization.setNumberOfDirectUsers(Integer.parseInt(textFieldNumberOfDirectUsers.getText().trim()));
        organization.setNumberOfIndirectUsers(Integer.parseInt(textFieldNumberOfIndirectUsers.getText().trim()));
        return organization;
    }

    @Override
    public void clearFields() {
        textFieldName.clear();
        textFieldCity.clear();
        textFieldState.clear();
        textFieldSector.clear();
        textFieldStreet.clear();
        textFieldStreetNumber.clear();
        textFieldPostalCode.clear();
        textFieldEmail.clear();
        textFieldPhoneNumber.clear();
        textFieldNumberOfDirectUsers.clear();
        textFieldNumberOfIndirectUsers.clear();
    }
}