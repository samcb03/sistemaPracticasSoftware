package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
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


public class FXMLRegisterProjectController extends ValidationHandler {

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldMethodology;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldObjective;
    @FXML private TextArea textAreaDescription;
    @FXML private ComboBox<String> comboBoxOrganizationName;
    @FXML private ComboBox<String> comboBoxProjectSupervisor;

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
        loadProjectSupervisorNames();
    }

    private void loadOrganizationNames() {
        try {
            ArrayList<String> organizationNames = affiliatedOrganizationDAO.getAllOrganizationNames();
            comboBoxOrganizationName.setItems(FXCollections.observableArrayList(organizationNames));
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private void loadProjectSupervisorNames() {
        try {
            ArrayList<String> supervisorNames = projectSupervisorDAO.getAllSupervisorNames();
            comboBoxProjectSupervisor.setItems(FXCollections.observableArrayList(supervisorNames));
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerProject);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(textFieldName.getText().trim(), "El nombre"),
            validateLettersOnly(textFieldMethodology.getText().trim(), "La metodología"),
            validatePositiveInteger(textFieldCapacity.getText().trim(), "El cupo"),
            validateLettersOnly(textFieldObjective.getText().trim(), "El objetivo"),
            validateLettersOnly(textAreaDescription.getText().trim(), "La descripción"),
            validateComboBox(comboBoxOrganizationName.getValue(), " organización"),
            validateComboBox(comboBoxProjectSupervisor.getValue(), " responsable de proyecto")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerProject() {
        Project project = buildProject();
        try {
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

    private Project buildProject() {
        Project project = new Project();
        project.setName(textFieldName.getText().trim());
        project.setMethodology(textFieldMethodology.getText().trim());
        project.setCapacity(Integer.parseInt(textFieldCapacity.getText().trim()));
        project.setObjective(textFieldObjective.getText().trim());
        project.setDescription(textAreaDescription.getText().trim());

        String selectedOrganization = comboBoxOrganizationName.getValue();
        try {
            int organizationId = affiliatedOrganizationDAO.getOrganizationIdByName(selectedOrganization);
            project.setIdAffiliatedOrganization(organizationId);
            int supervisorId = projectSupervisorDAO.getSupervisorIdByName(comboBoxProjectSupervisor.getValue()); 
            project.setIdProjectSupervisor(supervisorId);
        } catch (OperationException e) {
            showError(e.getMessage());
        }
        return project;
    }

    @Override
    public void clearFields() {
        textFieldName.clear();
        textFieldMethodology.clear();
        textFieldCapacity.clear();
        textFieldObjective.clear();
        textAreaDescription.clear();
    }
}