package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.INVALID_ID;

import java.net.URL;
import java.util.List;
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
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
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
    private static final String APPLICANT_SEPARATOR = " - ";

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
            Optional<Project> validateProject = projectDAO.getProjectByName(projectName);

            if(validateProject.isPresent()) {
                Project project = validateProject.get();
                labelMethodology.setText(project.getMethodology());
                labelCapacity.setText(String.valueOf(project.getCapacity()));
                labelObjective.setText(project.getObjective());
                labelDescription.setText(project.getDescription());

            Optional<AffiliatedOrganization> validateOrganization = affiliatedOrganizationDAO.getOrganizationById(project.getIdAffiliatedOrganization());

            if (validateOrganization.isPresent()) {
                labelOrganizationName.setText(validateOrganization.get().getName());
            } else {
                labelOrganizationName.setText("No se encontró la organización");
            }
            loadApplicantsForProject(project.getId());
            } else {
                showError("Proyecto no encontrado");
            }

        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void loadApplicantsForProject(int idProject) {
        try {
            List<Student> applicants = requestProjectDAO.getApplicantsByProjectId(idProject);
            List<String> applicantNames = applicants.stream().
                map(student -> student.getFirstName() + " " + student.getLastName()
                    + APPLICANT_SEPARATOR + student.getIdStudent())
                .collect(Collectors.toList());
            
            listViewApplicants.setItems(FXCollections.observableArrayList(applicantNames));
            
            if (applicants.isEmpty()) {
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

        if (selectedProject == null || selectedRow == null) {
            showError("Seleccione un proyecto y un alumno de la lista.");
        } else {
            try {
                String matricula = extractStudentId(selectedRow);

                Optional<Project> validateProject = projectDAO.getProjectByName(selectedProject);

                if(validateProject.isPresent()) {
                    Project project = validateProject.get();

                if (requestProjectDAO.assignStudentToProject(matricula, project.getId())) {
                    showSuccess("Asignación exitosa para " + matricula);
                    listViewApplicants.getItems().remove(selectedRow);

                    if (listViewApplicants.getItems().isEmpty()) {
                        loadProjectNames();
                        clearFields();
                    }
                }
        } else {
            showError("El proyecto no se encontró");
        }
            } catch (OperationException e) {
                showError("No se pudo asignar: " + e.getMessage());
            }
        }
    }

    private String extractStudentId(String applicantRow) {
        int separatorIndex = applicantRow.lastIndexOf(APPLICANT_SEPARATOR);
        String studentId = "";

        if (separatorIndex >= INVALID_ID) {
            studentId = applicantRow.substring(separatorIndex + APPLICANT_SEPARATOR.length()).trim();
        }
        return studentId;
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