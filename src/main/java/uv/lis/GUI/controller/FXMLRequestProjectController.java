package uv.lis.GUI.controller;


import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;
import uv.lis.logic.dao.RequestProjectDAO;


public class FXMLRequestProjectController extends ValidationHandler {

    private static final int MAX_PROJECTS = 3;

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
            showError("No hay una sesión activa. Por favor inicia sesión.");
            buttonAddProject.setDisable(true);
            buttonSubmit.setDisable(true);
            return;
        }
        
        studentId = student.getIdStudent();
        setupControls(labelError, buttonBack);
        loadProjectNames();
        setupComboBoxListener();
        updateSelectedCount();
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
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadProjectDetails(newValue);
                }
            }
        );
    }

    private void loadProjectDetails(String projectName) {
        try {
            Project project = projectDAO.getProjectByName(projectName);
            labelMethodology.setText(project.getMethodology());
            labelCapacity.setText(String.valueOf(project.getCapacity()));
            labelObjective.setText(project.getObjective());
            labelDescription.setText(project.getDescription());

            AffiliatedOrganization organization = affiliatedOrganizationDAO
                .getOrganizationById(project.getIdAffiliatedOrganization());
            labelOrganizationName.setText(organization.getName());
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void addProject() {
        String selected = comboBoxProjects.getValue();

        if (selectedProjects.containsKey(selected)) {
            showError("Ya agregaste este proyecto");
        } else if (selectedProjects.size() >= MAX_PROJECTS) {
            showError("Solo puedes solicitar " + MAX_PROJECTS + " proyectos");
        } else {
            try {
                Project project = projectDAO.getProjectByName(selected);
                boolean isValid = requestProjectDAO.validateProjectRequest(studentId, project.getId());
                if (isValid) {
                    selectedProjects.put(selected, project.getId());
                    listViewSelectedProjects.getItems().add(selected);
                    updateSelectedCount();
                    labelError.setText("");
                } else {
                    showError("No es posible agregar este proyecto");
                }
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void submitRequests() {
        if (selectedProjects.size() < MAX_PROJECTS) {
            showError("Debes seleccionar exactamente " + MAX_PROJECTS + " proyectos");
        } else {
            try {
                for (Map.Entry<String, Integer> entry : selectedProjects.entrySet()) {
                    requestProjectDAO.requestProject(studentId, entry.getValue());
                }
                showSuccess("Solicitudes registradas correctamente");
                clearFields();
            } catch (OperationException e) {
                showError(e.getMessage());
            }
        }
    }

    private void updateSelectedCount() {
        labelSelectedCount.setText(selectedProjects.size() + "/" + MAX_PROJECTS);
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
}