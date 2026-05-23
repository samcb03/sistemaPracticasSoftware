package uv.lis.GUI.controller;

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

    private void loadOrganizationNames() {
        try {
            ArrayList<String> organizationNames = affiliatedOrganizationDAO.getAllOrganizationNames();
            comboBoxOrganizationName.setItems(FXCollections.observableArrayList(organizationNames));
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerProjectSupervisor);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldPosition.getText(), "El cargo"),
            validateEmail(textFieldEmail.getText().trim(), "El correo electrónico"),
            validateComboBox(comboBoxOrganizationName.getValue(), "la organización")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerProjectSupervisor() {
        ProjectSupervisor projectSupervisor = buildProjectSupervisor();
        try {
            boolean registered = projectSupervisorDAO.registerProjectSupervisor(projectSupervisor);
            if (registered) {
                showSuccess("Encargado técnico registrado correctamente");
                clearFields();
            } else {
                showError("Error al registrar al encargado técnico");
            }
        } catch (OperationException operationException) {
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
            showError("Error al recuperar la organización seleccionada");
        }
    }
    
    return projectSupervisor;
}

    @Override
    protected void clearFields() {
        textFieldName.clear();
        textFieldPosition.clear();
        textFieldEmail.clear();
        comboBoxOrganizationName.setValue(null);
    }
}