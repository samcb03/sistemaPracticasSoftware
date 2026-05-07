package uv.lis.GUI.controller;

import java.net.URL;
import java.util.List;
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
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLAssignationProjectController extends ValidationHandler {

    @FXML private ComboBox<String> comboBoxProjects;
    @FXML private Button buttonAssignProject;
    @FXML private Button buttonBack;
    @FXML private Label labelOrganizationName;
    @FXML private Label labelMethodology;
    @FXML private Label labelCapacity;
    @FXML private Label labelObjective;
    @FXML private Label labelDescription;
    @FXML private Label labelError;
    @FXML private ListView<String> listViewApplicants; 

    private RequestProjectDAO requestProjectDAO;
    private ProjectDAO projectDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private Professor coordinator;

@Override
    public void initialize(URL location, ResourceBundle resources) {
        requestProjectDAO = new RequestProjectDAO();
        projectDAO = new ProjectDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();

        setupControls(labelError, buttonBack);

        coordinator = SessionManager.getInstance().getCurrentProfessor();

        if(coordinator == null || !coordinator.getIsCoordinator()) {
            showError("No hay coordinador en sesión.");
            buttonAssignProject.setDisable(true);
            comboBoxProjects.setDisable(true); 
        } else {
            loadProjectNames();
            setupComboBoxListener();
        }
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

            loadApplicantsForProject(project.getId());

        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void loadApplicantsForProject(int idProject) {
        try {
            List<String> applicants = requestProjectDAO.getApplicantsByProjectId(idProject);
            listViewApplicants.setItems(FXCollections.observableArrayList(applicants));
            
            if(applicants.isEmpty()) {
                showError("No hay solicitudes para este proyecto.");
            } else {
                labelError.setText(""); 
            }
        } catch (OperationException e) {
             showError(e.getMessage());
        }
    }

@FXML
public void assignStudent() {
    String selectedProject = comboBoxProjects.getValue();
    String selectedRow = listViewApplicants.getSelectionModel().getSelectedItem(); 

    if(selectedProject == null || selectedRow == null) {
        showError("Seleccione un proyecto y un alumno de la lista.");
        return;
    }

    try {
        // 1. Extraer matrícula de "Nombre Apellido (S23013127)"
        String matricula = selectedRow.substring(selectedRow.lastIndexOf("(") + 1, selectedRow.lastIndexOf(")"));
        
        Project project = projectDAO.getProjectByName(selectedProject);

        // 2. Ejecutar asignación
        if(requestProjectDAO.assignStudentToProject(matricula, project.getId())) {
            
            // 3. Mostrar éxito (Feedback visual)
            labelError.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            labelError.setText("¡Asignación exitosa para " + matricula + "!");
            
            // 4. Actualizar interfaz
            listViewApplicants.getItems().remove(selectedRow);
            
            // Si el proyecto se llenó, refrescar el ComboBox para que ya no aparezca
            if (requestProjectDAO.getAssignedCount(project.getId()) >= project.getCapacity()) {
                loadProjectNames(); 
                clearFields();
                labelError.setText("Proyecto completado.");
            }
        }
    } catch(OperationException e) {
        showError("No se pudo asignar: " + e.getMessage());
    }
}

    @Override
    protected void clearFields() {
        listViewApplicants.getItems().clear();
        labelOrganizationName.setText("");
        labelMethodology.setText("");
        labelCapacity.setText("");
        labelObjective.setText("");
        labelDescription.setText("");
        comboBoxProjects.getSelectionModel().clearSelection();
    }
}