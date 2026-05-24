package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
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
import javafx.scene.control.Button;
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

    private static final int MINIMUM_CAPACITY = 1;
    private static final int MAXIMUM_CAPACITY = 99;

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
        this.currentProject = project;
        displayProjectInformation();
        loadAssignedStudents();
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
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE,
                "Error al cargar los alumnos asignados al proyecto", operationException);
            showError(operationException.getMessage());
        }
    }

    @FXML
    private void enableEditMode() {
        if (currentProject == null) {
            showError("No hay información del proyecto cargada");
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentProject.getName());
        textAreaDescription.setText(currentProject.getDescription());
        textFieldObjective.setText(currentProject.getObjective());
        textFieldCapacity.setText(String.valueOf(currentProject.getCapacity()));
        textFieldMethodology.setText(currentProject.getMethodology());
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

    private Optional<String> validateInputs() {
        return Stream.of(
            validateText(textFieldName.getText(), "El nombre del proyecto"),
            validateText(textAreaDescription.getText(), "La descripción"),
            validateText(textFieldObjective.getText(), "El objetivo"),
            validateText(textFieldMethodology.getText(), "La metodología"),
            validatePositiveInteger(textFieldCapacity.getText(), "El cupo")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> checkCapacityRange(int capacity) {
        Optional<String> validationResult = Optional.empty();
        int assignedStudentsCount = listViewStudent.getItems().size();

        if (capacity < MINIMUM_CAPACITY || capacity > MAXIMUM_CAPACITY) {
            validationResult = Optional.of(
                "El cupo debe estar entre " + MINIMUM_CAPACITY + " y " + MAXIMUM_CAPACITY);
        } else if (capacity < assignedStudentsCount) {
            validationResult = Optional.of(
                "El cupo no puede ser menor a los alumnos ya asignados ("
                + assignedStudentsCount + ")");
        }

        return validationResult;
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
            showError("No se realizaron cambios en el proyecto");
        } else {
            currentProject = updatedProject;
            displayProjectInformation();
            toggleEditMode(false);
            showSuccess("Proyecto actualizado correctamente");
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

    /* This view does not require field clearing because it does not have 
     * a search mechanism. The project information is loaded once via 
     * initializeData() and replaced entirely by displayProjectInformation() 
     * after a successful update. */
    @Override
    protected void clearFields() {
    }
}