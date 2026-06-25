package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.EMAIL_REGEX;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProjectSupervisorController extends ValidationHandler {

    private static final String POSITION_FIELD = "El cargo";
    private static final String NAME_FIELD = "El nombre";
    private static final String EMAIL_FIELD = "El correo electrónico";
    private static final String ORGANIZATION_FIELD = "La organización";
    private static final String SUCCESSFUL_PROJECT_REGISTER_MESSAGE = "Encargado técnico registrado correctamente";
    private static final String ERROR_PROJECT_REGISTER_MESSAGE = "Error al registrar al encargado técnico";

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldPosition;
    @FXML private TextField textFieldEmail;
    @FXML private ComboBox<String> comboBoxOrganizationName;

    private ProjectSupervisorDAO projectSupervisorDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projectSupervisorDAO = new ProjectSupervisorDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        setupControls(labelError, buttonBack);
        loadOrganizationNames();
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::performRegistration);
    }

    @Override
    protected void clearFields() {
        textFieldName.clear();
        textFieldPosition.clear();
        textFieldEmail.clear();
        comboBoxOrganizationName.setValue(null);
    }

    private void loadOrganizationNames() {
        try {
            ArrayList<String> organizationNames = affiliatedOrganizationDAO.getAllOrganizationNames();
            comboBoxOrganizationName.setItems(FXCollections.observableArrayList(organizationNames));
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldName.getText(), NAME_FIELD),
            validateText(textFieldPosition.getText(), POSITION_FIELD),
            validateEmail(textFieldEmail.getText().trim(), EMAIL_FIELD),
            validateComboBox(comboBoxOrganizationName.getValue(), ORGANIZATION_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        ProjectSupervisor projectSupervisor = buildProjectSupervisor();
        try {
            boolean registered = projectSupervisorDAO.registerProjectSupervisor(projectSupervisor);
            if (registered) {
                showSuccess(SUCCESSFUL_PROJECT_REGISTER_MESSAGE);
                clearFields();
            } else {
                showError(ERROR_PROJECT_REGISTER_MESSAGE);
            }
        } catch(OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private ProjectSupervisor buildProjectSupervisor() {
        ProjectSupervisor projectSupervisor = new ProjectSupervisor();
        projectSupervisor.setName(textFieldName.getText().trim());
        projectSupervisor.setPosition(textFieldPosition.getText().trim());
        projectSupervisor.setEmail(textFieldEmail.getText().trim());
        projectSupervisor.setIsActive(true);
        String selectedOrganization = comboBoxOrganizationName.getValue();
        
        if (selectedOrganization != null) {
            try {
                int organizationId = affiliatedOrganizationDAO.getOrganizationIdByName(selectedOrganization);
                projectSupervisor.setOrganizationInt(organizationId); 
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
        return projectSupervisor;
    }
}