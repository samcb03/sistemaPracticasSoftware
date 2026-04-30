package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validatePhoneNumber;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class FXMLRegisterAffiliatedOrganizationController extends ValidationHandler {

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldCity;
    @FXML private TextField textFieldState;
    @FXML private TextField textFieldSector;
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
            validateLettersOnly(textFieldName.getText().trim(), "El nombre"),
            validateLettersOnly(textFieldCity.getText().trim(), "La ciudad"),
            validateLettersOnly(textFieldState.getText().trim(), "El estado"),
            validateLettersOnly(textFieldSector.getText().trim(), "El sector"),
            validateEmail(textFieldEmail.getText().trim()),
            validatePhoneNumber(textFieldPhoneNumber.getText().trim()),
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
        textFieldEmail.clear();
        textFieldPhoneNumber.clear();
        textFieldNumberOfDirectUsers.clear();
        textFieldNumberOfIndirectUsers.clear();
    }
}