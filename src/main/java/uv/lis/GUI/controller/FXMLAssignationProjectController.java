package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.INVALID_ID;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLAssignationProjectController extends ValidationHandler {

    @FXML private ComboBox<String> comboBoxProjects;
    @FXML private Button buttonAssignProject;
    @FXML private Button buttonBack;
    @FXML private Button buttonAlternativeMode;
    @FXML private Label labelOrganizationName;
    @FXML private Label labelMethodology;
    @FXML private Label labelCapacity;
    @FXML private Label labelObjective;
    @FXML private Label labelDescription;
    @FXML private Label labelError;
    @FXML private Label labelApplicants;
    @FXML private ListView<String> listViewApplicants;
    @FXML private ListView<String> listViewStudentsWithoutProject;
    @FXML private Label labelStudentsWithoutProject;
    @FXML private TextArea textAreaReason;

    private RequestProjectDAO requestProjectDAO;
    private ProjectDAO projectDAO;
    private StudentDAO studentDAO;
    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;
    private NotificationDAO notificationDAO;
    private Professor coordinator;
    private static final String APPLICANT_SEPARATOR = " - ";
    private static final String REASON_FIELD = "El motivo de asignación";
    private static final String ASSIGNMENT_NOTIFICATION_TITLE = "Proyecto asignado";
    private static final String ASSIGNMENT_MESSAGE_PREFIX = "Se te asignó el proyecto: ";
    private static final String ASSIGNMENT_MESSAGE_REASON = ". Motivo: ";
    private static final String ALTERNATIVE_MODE_LABEL = "Asignación alternativa";
    private static final String NORMAL_MODE_LABEL = "Modo normal";
    private boolean isAlternativeMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestProjectDAO = new RequestProjectDAO();
        projectDAO = new ProjectDAO();
        studentDAO = new StudentDAO();
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
        notificationDAO = new NotificationDAO();

        setupControls(labelError, buttonBack);

        coordinator = SessionManager.getInstance().getCurrentProfessor();

        if (coordinator == null || !coordinator.getIsCoordinator()) {
            showError("No hay coordinador en sesión.");
            buttonAssignProject.setDisable(true);
            comboBoxProjects.setDisable(true);
        } else {
            loadProjectNames();
            setupComboBoxListener();
            setNodeVisibility(listViewStudentsWithoutProject, false);
            setNodeVisibility(labelStudentsWithoutProject, false);
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

            if (validateProject.isPresent()) {
                Project project = validateProject.get();
                labelMethodology.setText(project.getMethodology());
                labelCapacity.setText(String.valueOf(project.getCapacity()));
                labelObjective.setText(project.getObjective());
                labelDescription.setText(project.getDescription());
                loadOrganizationName(project.getIdAffiliatedOrganization());

                    if (!isAlternativeMode) {
                        loadApplicantsForProject(project.getId());
                    }

            } else {
                showError("Proyecto no encontrado");
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void loadOrganizationName(int idOrganization) throws OperationException {
        Optional<AffiliatedOrganization> validateOrganization 
            = affiliatedOrganizationDAO.getOrganizationById(idOrganization);

        if (validateOrganization.isPresent()) {
            labelOrganizationName.setText(validateOrganization.get().getName());
        } else {
            labelOrganizationName.setText("No se encontró la organización");
        }
    }

    private void loadApplicantsForProject(int idProject) {
        try {
            List<Student> applicants = requestProjectDAO.getApplicantsByProjectId(idProject);
            List<String> applicantNames = applicants.stream().map(student -> student.getFirstName() 
                + " " + student.getLastName() + APPLICANT_SEPARATOR + student.getIdStudent())
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
        String selectedRow = isAlternativeMode
            ? listViewStudentsWithoutProject.getSelectionModel().getSelectedItem()
            : listViewApplicants.getSelectionModel().getSelectedItem();
        String reason = textAreaReason.getText();

        Optional<String> validationError = validateAssignmentInput(selectedProject, selectedRow, reason);

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            processAssignment(selectedProject, selectedRow, reason);
        }
    }

    private Optional<String> validateAssignmentInput(String project, String applicantRow, String reason) {
        Optional<String> validationError;

        if (project == null || applicantRow == null) {
            validationError = Optional.of("Seleccione un proyecto y un alumno de la lista.");
        } else {
            validationError = InputValidator.validateDescriptiveText(reason, REASON_FIELD);
        }

        return validationError;
    }

    private void processAssignment(String selectedProject, String selectedRow, String reason) {
        try {
            String idStudent = extractStudentId(selectedRow);
            Optional<Project> validateProject = projectDAO.getProjectByName(selectedProject);

            if (validateProject.isPresent()) {
                if (executeAssignment(validateProject.get(), idStudent, reason)) {
                    refreshApplicantsList(selectedRow);
                }
            } else {
                showError("El proyecto no se encontró");
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private boolean executeAssignment(Project project, String idStudent, String reason) throws OperationException {
        boolean isAssigned = false;

        if (studentDAO.hasProjectAssigned(idStudent)) {
            showError("El alumno ya tiene un proyecto asignado.");
        } else if (!requestProjectDAO.hasAvailableCapacity(project.getId())) {
            showError("El proyecto ya no tiene cupo disponible.");
        } else {
            isAssigned = applyAssignment(project, idStudent, reason);
        }

        return isAssigned;
    }

    private boolean applyAssignment(Project project, String idStudent, String reason) throws OperationException {
        boolean isAssigned = isAlternativeMode
            ? requestProjectDAO.assignStudentToProjectAlternative(idStudent, project.getId())
            : requestProjectDAO.assignStudentToProject(idStudent, project.getId());

        if (isAssigned) {
            showSuccess("Asignación exitosa para " + idStudent);
            notifyAssignedStudent(idStudent, project.getName(), reason);
        }

        return isAssigned;
    }

    private void notifyAssignedStudent(String idStudent, String projectName, String reason) {
        try {
            Notification notification = buildAssignmentNotification(idStudent, projectName, reason);
            notificationDAO.registerNotification(notification);
        } catch (OperationException e) {
            showError("Asignación realizada, pero no se pudo enviar la notificación al alumno");
        }
    }

    private Notification buildAssignmentNotification(String idStudent, String projectName, String reason) {
        Notification notification = new Notification();
        notification.setIdStudent(idStudent);
        notification.setTitle(ASSIGNMENT_NOTIFICATION_TITLE);
        notification.setMessage(buildAssignmentMessage(projectName, reason));
        notification.setCreationDate(new Timestamp(System.currentTimeMillis()));
        notification.setRead(false);
        return notification;
    }

    private String buildAssignmentMessage(String projectName, String reason) {
        String message = ASSIGNMENT_MESSAGE_PREFIX + projectName + ASSIGNMENT_MESSAGE_REASON + reason;
        return message;
    }

    private void refreshApplicantsList(String selectedRow) {
        if (isAlternativeMode) {
            listViewStudentsWithoutProject.getItems().remove(selectedRow);
            loadAvailableProjects();
            loadStudentsWithoutProject();
            if (listViewStudentsWithoutProject.getItems().isEmpty()) {
                clearFields();
            }
        } else {
            listViewApplicants.getItems().remove(selectedRow);
            if (listViewApplicants.getItems().isEmpty()) {
                loadProjectNames();
                clearFields();
            }
        }
    }

    private String extractStudentId(String applicantRow) {
        int separatorIndex = applicantRow.lastIndexOf(APPLICANT_SEPARATOR);
        String studentId = "";

        if (separatorIndex >= INVALID_ID) {
            studentId = applicantRow.substring(
                separatorIndex + APPLICANT_SEPARATOR.length()).trim();
        }

        return studentId;
    }

    @FXML
    private void toggleAlternativeMode() {
        isAlternativeMode = !isAlternativeMode;
        clearFields();

        if (isAlternativeMode) {
            buttonAlternativeMode.setText(NORMAL_MODE_LABEL);
            loadAvailableProjects();
            loadStudentsWithoutProject();
        } else {
            buttonAlternativeMode.setText(ALTERNATIVE_MODE_LABEL);
            loadProjectNames();
        }

        setNodeVisibility(listViewApplicants, !isAlternativeMode);
        setNodeVisibility(labelApplicants, !isAlternativeMode);
        setNodeVisibility(listViewStudentsWithoutProject, isAlternativeMode);
        setNodeVisibility(labelStudentsWithoutProject, isAlternativeMode);
    }

    private void loadAvailableProjects() {
        try {
            List<Project> projects = projectDAO.getAllProjectsWithCapacity();
            List<String> projectNames = projects.stream()
                .map(Project::getName)
                .collect(Collectors.toList());
            comboBoxProjects.setItems(FXCollections.observableArrayList(projectNames));
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void loadStudentsWithoutProject() {
        try {
            List<Student> students = requestProjectDAO.getStudentsWithoutAssignedProject();
            List<String> studentNames = students.stream()
                .map(student -> student.getFirstName() + " " + student.getLastName()
                    + APPLICANT_SEPARATOR + student.getIdStudent())
                .collect(Collectors.toList());
            listViewStudentsWithoutProject.setItems(FXCollections.observableArrayList(studentNames));

            if (students.isEmpty()) {
                showError("No hay alumnos sin proyecto asignado.");
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    @Override
    protected void clearFields() {
        listViewApplicants.getItems().clear();
        listViewStudentsWithoutProject.getItems().clear();
        labelOrganizationName.setText("");
        labelMethodology.setText("");
        labelCapacity.setText("");
        labelObjective.setText("");
        labelDescription.setText("");
        textAreaReason.clear();
        comboBoxProjects.getSelectionModel().clearSelection();
    }
}