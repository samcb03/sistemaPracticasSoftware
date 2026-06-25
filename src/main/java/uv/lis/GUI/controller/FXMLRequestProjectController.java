package uv.lis.GUI.controller;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLRequestProjectController extends ValidationHandler {

    private static final int MAX_PROJECTS = 3;
    private static final String WITHOUT_ACTIVE_SESSION = "No hay una sesión activa. Por favor inicia sesión";
    private static final String MAX_PROJECTS_ERROR = "Solo puedes solicitar " + MAX_PROJECTS + " proyectos";
    private static final String EXACT_PROJECTS_ERROR = "Debes seleccionar exactamente " + MAX_PROJECTS + " proyectos";
    private static final String NO_PROJECT_SELECTED_ERROR = "Por favor, selecciona un proyecto de la lista.";
    private static final String PROJECT_ALREADY_ADDED_ERROR = "Ya agregaste este proyecto";
    private static final String PROJECT_NOT_FOUND_ERROR = "El proyecto seleccionado no se encontró en la base de datos.";
    private static final String PROJECT_DETAILS_NOT_FOUND_ERROR = "No se encontraron los detalles del proyecto seleccionado.";
    private static final String ORGANIZATION_NOT_FOUND = "Organización no encontrada";
    private static final String SUCCESSFUL_REQUEST_MESSAGE = "Solicitudes registradas correctamente";

    @FXML private ComboBox<String> comboBoxProjects;
    @FXML private Button buttonBack;
    @FXML private Button buttonAddProject;
    @FXML private Button buttonSubmit;
    @FXML private Label labelOrganizationName;
    @FXML private Label labelMethodology;
    @FXML private Label labelCapacity;
    @FXML private Label labelObjective;
    @FXML private Label labelDescription;
    @FXML private Label labelError;
    @FXML private Label labelSelectedCount;
    @FXML private ListView<String> listViewSelectedProjects;

    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private RequestProjectDAO requestProjectDAO;
    private Student student;
    private LinkedHashMap<String, Integer> selectedProjects = new LinkedHashMap<>();
    private String studentId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        requestProjectDAO = new RequestProjectDAO();
        student = SessionManager.getInstance().getCurrentStudent();

        if (student == null) {
            showError(WITHOUT_ACTIVE_SESSION);
            buttonAddProject.setDisable(true);
            buttonSubmit.setDisable(true);
        } else {
            studentId = student.getIdStudent();
            setupControls(labelError, buttonBack);
            loadProjectNames();
            setupComboBoxListener();
            updateSelectedCount();
        }
    }

    @FXML
    public void addProject() {
        String projectSelected = comboBoxProjects.getValue();

        if (projectSelected == null || projectSelected.trim().isEmpty()) {
            showError(NO_PROJECT_SELECTED_ERROR);
        } else if (selectedProjects.containsKey(projectSelected)) {
            showError(PROJECT_ALREADY_ADDED_ERROR);
        } else if (selectedProjects.size() >= MAX_PROJECTS) {
            showError(MAX_PROJECTS_ERROR);
        } else {
            addValidatedProject(projectSelected);
        }
    }

    @FXML
    public void submitRequests() {
        if (selectedProjects.size() < MAX_PROJECTS) {
            showError(EXACT_PROJECTS_ERROR);
        } else {
            try {
                for (Map.Entry<String, Integer> entry : selectedProjects.entrySet()) {
                    requestProjectDAO.requestProject(studentId, entry.getValue());
                }
                showSuccess(SUCCESSFUL_REQUEST_MESSAGE);
                clearFields();
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    @Override
    protected void clearFields() {
        comboBoxProjects.getSelectionModel().clearSelection();
        selectedProjects.clear();
        listViewSelectedProjects.getItems().clear();
        labelOrganizationName.setText("");
        labelMethodology.setText("");
        labelCapacity.setText("");
        labelObjective.setText("");
        labelDescription.setText("");
        updateSelectedCount();
    }

    private void addValidatedProject(String projectSelected) {
        try {
            Optional<Project> validateProject = projectDAO.getProjectByName(projectSelected);

            if(validateProject.isPresent()) {
                Project project = validateProject.get();
                Optional<String> validationError = requestProjectDAO.validateProjectRequest(studentId, project.getId());
                handleValidation(validationError, () -> registerSelectedProject(projectSelected, project.getId()));
            } else {
                showError(PROJECT_NOT_FOUND_ERROR);
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void registerSelectedProject(String projectSelected, int projectId) {
        selectedProjects.put(projectSelected, projectId);
        listViewSelectedProjects.getItems().add(projectSelected);
        updateSelectedCount();
        labelError.setText("");
    }

    private void loadProjectNames() {
        try {
            List<Project> availableProjects = requestProjectDAO.getAvailableProjects();
            List<String> projectNames = availableProjects.stream()
                .map(Project::getName)
                .collect(Collectors.toList());
            comboBoxProjects.setItems(FXCollections.observableArrayList(projectNames));
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void setupComboBoxListener() {
        comboBoxProjects.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleProjectSelection(newValue)
        );
    }

    private void handleProjectSelection(String newValue) {
        if(newValue != null) {
            loadProjectDetails(newValue);
        }
    }

    private void loadProjectDetails(String projectName) {
        try {
            Optional<Project> validateProject = projectDAO.getProjectByName(projectName);

            if (validateProject.isPresent()) {
                Project project = validateProject.get();
                labelMethodology.setText(project.getMethodology());
                labelCapacity.setText(String.valueOf(project.getCapacity()));
                labelObjective.setText(project.getObjective());
                labelDescription.setText(project.getDescription());
                Optional<AffiliatedOrganization> validateOrganization = affiliatedOrganizationDAO
                    .getOrganizationById(project.getIdAffiliatedOrganization());
                
                if (validateOrganization.isPresent()) {
                    labelOrganizationName.setText(validateOrganization.get().getName());
                } else {
                    labelOrganizationName.setText(ORGANIZATION_NOT_FOUND);
                }
            } else {
                showError(PROJECT_DETAILS_NOT_FOUND_ERROR);
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void updateSelectedCount() {
        labelSelectedCount.setText(selectedProjects.size() + "/" + MAX_PROJECTS);
    }
}