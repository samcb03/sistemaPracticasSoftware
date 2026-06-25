package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateDescriptiveText;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validateProjectCapacity;
import static uv.lis.logic.utils.InputValidator.validateRegister;

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

    private static final String CAPACITY_FIELD = "El cupo";
    private static final String NAME_FIELD = "El nombre";
    private static final String METHODOLOGY_FIELD = "La metodología";
    private static final String OBJECTIVE_FIELD = "El objetivo";
    private static final String DESCRIPTION_FIELD = "La descripción";
    private static final String ORGANIZATION_FIELD = " organización";
    private static final String SUPERVISOR_FIELD = " responsable técnico";
    private static final String SUCCESSFUL_PROJECT_REGISTER_MESSAGE = "Proyecto registrado correctamente";
    private static final String ERROR_PROJECT_REGISTER_MESSAGE = "Error al registrar el proyecto";

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textAreaDescription;
    @FXML private ComboBox<String> comboBoxOrganizationName;
    @FXML private ComboBox<String> comboBoxSupervisorName;

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
                comboBoxSupervisorName.getItems().clear(); 
                int organizationId = affiliatedOrganizationDAO.getOrganizationIdByName(selectedOrganization);
                ArrayList<String> supervisorNames = projectSupervisorDAO.getSupervisorsByOrganizationId(organizationId);
                comboBoxSupervisorName.setItems(FXCollections.observableArrayList(supervisorNames));
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::performRegistration);
    }

    private Optional<String> getFirstValidationError() {
        String capacityProject = textFieldCapacity.getText().trim();
        Optional<String> formatError = validatePositiveInteger(capacityProject, CAPACITY_FIELD);
        Optional<String> capacityError = Optional.empty();

        if (formatError.isEmpty()) {
            capacityError = validateProjectCapacity(Integer.parseInt(capacityProject), CAPACITY_FIELD);
        }

        Stream<Optional<String>> validationStream = Stream.of(
            validateRegister(textFieldName.getText(), NAME_FIELD),
            validateText(textFieldMethodology.getText(), METHODOLOGY_FIELD), 
                formatError, capacityError,
            validateDescriptiveText(textFieldObjective.getText(), OBJECTIVE_FIELD),
            validateDescriptiveText(textAreaDescription.getText(), DESCRIPTION_FIELD),
            validateComboBox(comboBoxOrganizationName.getValue(), ORGANIZATION_FIELD),
            validateComboBox(comboBoxSupervisorName.getValue(), SUPERVISOR_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        try {
            Project project = buildProject();
            boolean projectRegistered = projectDAO.registerProject(project);
            if (projectRegistered) {
                showSuccess(SUCCESSFUL_PROJECT_REGISTER_MESSAGE);
                clearFields();
            } else {
                showError(ERROR_PROJECT_REGISTER_MESSAGE);
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
        String selectedSupervisor = comboBoxSupervisorName.getValue();
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
        comboBoxSupervisorName.getSelectionModel().clearSelection();
        comboBoxSupervisorName.getItems().clear(); 
    }
}