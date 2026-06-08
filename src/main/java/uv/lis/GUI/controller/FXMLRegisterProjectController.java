package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validateProjectCapacity;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProjectController extends ValidationHandler {

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textAreaDescription;
    @FXML private ComboBox<String> comboBoxOrganizationName;
    @FXML private ComboBox<String> comboBoxResponsableName;

    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private ProjectSupervisorDAO projectSupervisorDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        projectSupervisorDAO = new ProjectSupervisorDAO();
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
    public void filterSupervisors() {
        String selectedOrganization = comboBoxOrganizationName.getValue();
        if (selectedOrganization != null) {
            try {
                comboBoxResponsableName.getItems().clear(); 
                
                int organizationId = affiliatedOrganizationDAO.getOrganizationIdByName(selectedOrganization);
                ArrayList<String> supervisorNames = projectSupervisorDAO.getSupervisorsByOrganizationId(organizationId);
                comboBoxResponsableName.setItems(FXCollections.observableArrayList(supervisorNames));
                
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerProject);
    }

    private Optional<String> getFirstValidationError() {
        String capacityProject = textFieldCapacity.getText().trim();
        Optional<String> formatError = validatePositiveInteger(capacityProject, "El cupo");
        Optional<String> capacityError = Optional.empty();
            if (formatError.isEmpty()) {
                capacityError = validateProjectCapacity(Integer.parseInt(capacityProject), "El cupo");
            }

        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldMethodology.getText(), "La metodología"),
            formatError,
            capacityError,
            validateText(textFieldObjective.getText(), "El objetivo"),
            validateText(textAreaDescription.getText(), "La descripción"),
            validateComboBox(comboBoxOrganizationName.getValue(), " organización"),
            validateComboBox(comboBoxResponsableName.getValue(), " responsable técnico")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerProject() {
        try {
            Project project = buildProject();
            boolean projectRegistered = projectDAO.registerProject(project);
            if (projectRegistered) {
                showSuccess("Proyecto registrado correctamente");
                clearFields();
            } else {
                showError("Error al registrar el proyecto");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Project buildProject() throws OperationException {
        Project project = new Project();
        project.setName(textFieldName.getText().trim());
        project.setMethodology(textFieldMethodology.getText().trim());
        project.setCapacity(Integer.parseInt(textFieldCapacity.getText().trim()));
        project.setObjective(textFieldObjective.getText().trim());
        project.setDescription(textAreaDescription.getText().trim());
        project.setActive(true);

        String selectedOrganization = comboBoxOrganizationName.getValue();
        String selectedSupervisor = comboBoxResponsableName.getValue();
        
        int organizationId = affiliatedOrganizationDAO.getOrganizationIdByName(selectedOrganization);
        int supervisorId = projectSupervisorDAO.getSupervisorIdByName(selectedSupervisor); 
        
        project.setIdAffiliatedOrganization(organizationId);
        project.setIdSupervisor(supervisorId); 
        
        return project;
    }

    @Override
    public void clearFields() {
        textFieldName.clear();
        textFieldMethodology.clear();
        textFieldCapacity.clear();
        textFieldObjective.clear();
        textAreaDescription.clear();
        comboBoxOrganizationName.getSelectionModel().clearSelection();
        comboBoxResponsableName.getSelectionModel().clearSelection();
        comboBoxResponsableName.getItems().clear(); 
    }
}