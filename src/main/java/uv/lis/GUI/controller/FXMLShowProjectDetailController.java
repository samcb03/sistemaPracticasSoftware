package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validateRegister;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.StudentListCell;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLShowProjectDetailController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLShowProjectDetailController.class.getName());
    private static final String NO_PROJECT_LOADED_ERROR = "No hay información del proyecto cargada";
    private static final String NO_CHANGES_ERROR = "No se realizaron cambios en el proyecto";
    private static final String HAS_STUDENTS_ERROR = "No se puede inactivar el proyecto porque tiene alumnos asignados";
    private static final String INACTIVATION_FAILED_ERROR = "No se pudo inactivar el proyecto";
    private static final String PROJECT_UPDATED_MESSAGE = "Proyecto actualizado correctamente";
    private static final String PROJECT_INACTIVATED_MESSAGE = "Proyecto inactivado correctamente";
    private static final String CONFIRMATION_TITLE = "Confirmar inactivación";
    private static final String CONFIRMATION_CONTENT = "¿Está seguro de que desea inactivar este proyecto?";
    private static final String PROJECT_NAME_FIELD = "El nombre del proyecto";
    private static final String DESCRIPTION_FIELD = "La descripción";
    private static final String OBJECTIVE_FIELD = "El objetivo";
    private static final String METHODOLOGY_FIELD = "La metodología";
    private static final String CAPACITY_FIELD = "El cupo";

    @FXML private Label labelName;
    @FXML private Label labelDescription;
    @FXML private Label labelObjective;
    @FXML private Label labelOrganization;
    @FXML private Label labelCapacity;
    @FXML private Label labelMethodology;
    @FXML private Label labelMessage;
    @FXML private ListView<Student> listViewStudent;
    @FXML private Button buttonModifyProject;
    @FXML private Button buttonSave;
    @FXML private Button buttonInactivateProject;
    @FXML private Button buttonBack;
    @FXML private TextField textFieldName;
    @FXML private TextArea textAreaDescription;
    @FXML private TextField textFieldObjective;
    @FXML private TextField textFieldCapacity;
    @FXML private TextField textFieldMethodology;

    private final RequestProjectDAO requestProjectDAO = new RequestProjectDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        configureStudentListCellFactory();
    }

    public void initializeData(Project project) {
        currentProject = project;
        displayProjectInformation();
        loadAssignedStudents();
        disableInactiveControl(currentProject);
    }

    @Override
    protected void clearFields() {
        /* No fields to clear in this detail view */
    }

    private void configureStudentListCellFactory() {
        listViewStudent.setCellFactory(listView -> new StudentListCell());
    }

    private void displayProjectInformation() {
        labelName.setText(currentProject.getName());
        labelDescription.setText(currentProject.getDescription());
        labelObjective.setText(currentProject.getObjective());
        labelOrganization.setText(currentProject.getAffiliatedOrganizationName());
        labelCapacity.setText(String.valueOf(currentProject.getCapacity()));
        labelMethodology.setText(currentProject.getMethodology());
    }

    private void loadAssignedStudents() {
        try {
            ArrayList<Student> assignedStudents 
                = requestProjectDAO.getAssignedStudentsByProjectId(currentProject.getId());
            listViewStudent.setItems(FXCollections.observableArrayList(assignedStudents));

            if (assignedStudents.isEmpty()) {
                LOGGER.log(Level.INFO, "El proyecto con ID {0} no tiene alumnos asignados", currentProject.getId());
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los alumnos asignados al proyecto", e);
            showError(e.getMessage());
        }
    }

    @FXML
    private void enableEditMode() {
        if (currentProject == null) {
            showError(NO_PROJECT_LOADED_ERROR);
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
        }
    }

    @FXML
    private void saveProject() {
        Optional<String> validationError = validateInputs();

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            executeProjectUpdate();
        }
    }

    @FXML
    private void handleInactivateProject() {
        if (currentProject == null) {
            showError(NO_PROJECT_LOADED_ERROR);
        } else if (hasAssignedStudents()) {
            showError(HAS_STUDENTS_ERROR);
        } else if (confirmInactivation()){
            executeInactivation();
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentProject.getName());
        textAreaDescription.setText(currentProject.getDescription());
        textFieldObjective.setText(currentProject.getObjective());
        textFieldCapacity.setText(String.valueOf(currentProject.getCapacity()));
        textFieldMethodology.setText(currentProject.getMethodology());
    }

    private Optional<String> validateInputs() {
        return Stream.of(
            validateRegister(textFieldName.getText(), PROJECT_NAME_FIELD),
            validateText(textAreaDescription.getText(), DESCRIPTION_FIELD),
            validateText(textFieldObjective.getText(), OBJECTIVE_FIELD),
            validateText(textFieldMethodology.getText(), METHODOLOGY_FIELD),
            validatePositiveInteger(textFieldCapacity.getText(), CAPACITY_FIELD)
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private void executeProjectUpdate() {
        try {
            Project updatedProject = buildUpdatedProject();
            boolean isUpdated = projectDAO.modifyProject(updatedProject);
            handleUpdateResult(isUpdated, updatedProject);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el proyecto", e);
            showError(e.getMessage());
        }
    }

    private Project buildUpdatedProject() {
        Project project = new Project();
        project.setId(currentProject.getId());
        project.setName(textFieldName.getText().trim());
        project.setDescription(textAreaDescription.getText().trim());
        project.setObjective(textFieldObjective.getText().trim());
        project.setCapacity(Integer.parseInt(textFieldCapacity.getText().trim()));
        project.setMethodology(textFieldMethodology.getText().trim());
        project.setAffiliatedOrganizationName(currentProject.getAffiliatedOrganizationName());
        project.setIdAffiliatedOrganization(currentProject.getIdAffiliatedOrganization());
        return project;
    }

    private void handleUpdateResult(boolean isUpdated, Project updatedProject) {
        if (!isUpdated) {
            showError(NO_CHANGES_ERROR);
        } else {
            currentProject = updatedProject;
            displayProjectInformation();
            toggleEditMode(false);
            showSuccess(PROJECT_UPDATED_MESSAGE);
            LOGGER.log(Level.INFO, "Proyecto actualizado: {0}", updatedProject.getId());
        }
    }

    private void toggleEditMode(boolean isEditing) {
        setNodeVisibility(labelName, !isEditing);
        setNodeVisibility(labelDescription, !isEditing);
        setNodeVisibility(labelObjective, !isEditing);
        setNodeVisibility(labelCapacity, !isEditing);
        setNodeVisibility(labelMethodology, !isEditing);
        setNodeVisibility(textFieldName, isEditing);
        setNodeVisibility(textAreaDescription, isEditing);
        setNodeVisibility(textFieldObjective, isEditing);
        setNodeVisibility(textFieldCapacity, isEditing);
        setNodeVisibility(textFieldMethodology, isEditing);
        setNodeVisibility(buttonModifyProject, !isEditing);
        setNodeVisibility(buttonSave, isEditing);
    }

    private void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }


    private boolean hasAssignedStudents() {
        boolean hasStudents = !listViewStudent.getItems().isEmpty();
        return hasStudents;
    }

    private boolean confirmInactivation() {
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.setTitle(CONFIRMATION_TITLE);
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText(CONFIRMATION_CONTENT);

        Optional<ButtonType> userChoice = confirmationAlert.showAndWait();
        return userChoice.isPresent() && userChoice.get() == ButtonType.OK;
    }

    private void executeInactivation() {
        try {
            boolean isInactivated = projectDAO.inactivateProject(currentProject);
            handleInactivationResult(isInactivated);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al inactivar el proyecto", e);
            showError(e.getMessage());
        }
    }

    private void handleInactivationResult(boolean isInactivated) {
        if (!isInactivated) {
            showError(INACTIVATION_FAILED_ERROR);
        } else {
            showSuccess(PROJECT_INACTIVATED_MESSAGE);
            buttonInactivateProject.setDisable(true);
            buttonModifyProject.setDisable(true);
            LOGGER.log(Level.INFO, "Proyecto inactivado: {0}", currentProject.getId());
        }
    }

    private void disableInactiveControl(Project project) {
        if (!project.isActive()) {
            buttonInactivateProject.setDisable(true);
            buttonModifyProject.setDisable(true);
        }
    }
}