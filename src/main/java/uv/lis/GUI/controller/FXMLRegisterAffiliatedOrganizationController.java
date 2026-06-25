package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePhoneNumber;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validatePostalCode;
import static uv.lis.logic.utils.InputValidator.validateRegister;
import static uv.lis.logic.utils.InputValidator.validateAddressNumber;

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

    private static final String NAME_FIELD = "El nombre";
    private static final String CITY_FIELD = "La ciudad";
    private static final String STATE_FIELD = "El estado";
    private static final String SECTOR_FIELD = "El sector";
    private static final String STREET_FIELD = "La calle";
    private static final String STREET_NUMBER_FIELD = "El número de calle";
    private static final String POSTAL_CODE_FIELD = "El código postal";
    private static final String EMAIL_FIELD = "El correo electrónico";
    private static final String PHONE_NUMBER_FIELD = "El número de teléfono";
    private static final String NUMBER_OF_DIRECT_USERS = "El número de usuarios directos";
    private static final String NUMBER_OF_INDIRECT_USERS = "El número de usuarios indirectos";
    private static final String SUCCESSFUL_ORGANIZATION_REGISTER_MESSAGE = "Organización registrada correctamente";
    private static final String ERROR_ORGANIZATION_REGISTER_MESSAGE = "Error al registrar la organización";

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
        handleValidation(firstValidationError, this::performRegistration);
    }
    
    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateRegister(textFieldName.getText(), NAME_FIELD),
            validateText(textFieldCity.getText(), CITY_FIELD),
            validateText(textFieldState.getText(), STATE_FIELD),
            validateText(textFieldSector.getText(), SECTOR_FIELD),
            validateText(textFieldStreet.getText(), STREET_FIELD),
            validateAddressNumber(textFieldStreetNumber.getText().trim(), STREET_NUMBER_FIELD),
            validatePostalCode(textFieldPostalCode.getText(), POSTAL_CODE_FIELD),
            validateEmail(textFieldEmail.getText().trim(), EMAIL_FIELD),
            validatePhoneNumber(textFieldPhoneNumber.getText().trim(), PHONE_NUMBER_FIELD),
            validatePositiveInteger(textFieldNumberOfDirectUsers.getText().trim(), NUMBER_OF_DIRECT_USERS),
            validatePositiveInteger(textFieldNumberOfIndirectUsers.getText().trim(), NUMBER_OF_INDIRECT_USERS)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        AffiliatedOrganization organization = buildOrganization();
        try {
            boolean registrationSuccessful = affiliatedOrganizationDAO.registerOrganization(organization);
            if (registrationSuccessful) {
                showSuccess(SUCCESSFUL_ORGANIZATION_REGISTER_MESSAGE);
                clearFields();
            } else {
                showError(ERROR_ORGANIZATION_REGISTER_MESSAGE);
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