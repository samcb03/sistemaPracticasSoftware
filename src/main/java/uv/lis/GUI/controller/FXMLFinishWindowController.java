package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.ProjectSummary;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLFinishWindowController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLFinishWindowController.class.getName());

    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay alumno en sesión";
    private static final String NO_PROJECT_ASSIGNED_MESSAGE = "No se encontró un proyecto asignado";
    private static final String LOAD_ERROR_MESSAGE = "Error al cargar el resumen final";
    private static final String EMPTY_TEXT = "";

    @FXML private Label labelStudentName;
    @FXML private Label labelProjectName;
    @FXML private Label labelOrganizationName;
    @FXML private Label labelProfessorName;
    @FXML private Label labelMethodology;
    @FXML private Label labelObjective;
    @FXML private Label labelDescription;
    @FXML private Label labelAccumulatedHours;
    @FXML private Label labelMessage;

    private Student currentStudent;
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ReportContextDAO reportContextDAO = new ReportContextDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonLogOut);
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        loadSummary();
    }

    private void loadSummary() {
        if (currentStudent == null) {
            showError(NO_STUDENT_IN_SESSION_MESSAGE);
        } else {
            try {
                String studentId = currentStudent.getIdStudent();
                labelStudentName.setText(currentStudent.getFirstName() + " " + currentStudent.getLastName());

                Optional<Project> assignedProject = projectDAO.getProjectByStudentId(studentId);
                Optional<ProjectSummary> projectDetails = projectDAO.getProjectDetailsByStudentId(studentId);

                if (assignedProject.isEmpty() || projectDetails.isEmpty()) {
                    showError(NO_PROJECT_ASSIGNED_MESSAGE);
                } else {
                    ProjectSummary details = projectDetails.get();
                    labelProjectName.setText(details.getProjectName());
                    labelOrganizationName.setText(details.getOrganizationName());
                    labelProfessorName.setText(details.getProfessorName());
                    labelMethodology.setText(details.getMethodology());
                    labelObjective.setText(details.getObjective());
                    labelDescription.setText(details.getDescription());

                    String accumulatedHours = reportContextDAO.getTotalReportedHoursByStudentId(studentId);
                    labelAccumulatedHours.setText(accumulatedHours);
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, LOAD_ERROR_MESSAGE, e);
                showError(e.getMessage());
            }
        }
    }

    @Override
    protected void clearFields() {
        labelStudentName.setText(EMPTY_TEXT);
        labelProjectName.setText(EMPTY_TEXT);
        labelOrganizationName.setText(EMPTY_TEXT);
        labelProfessorName.setText(EMPTY_TEXT);
        labelMethodology.setText(EMPTY_TEXT);
        labelObjective.setText(EMPTY_TEXT);
        labelDescription.setText(EMPTY_TEXT);
        labelAccumulatedHours.setText(EMPTY_TEXT);
        labelMessage.setText(EMPTY_TEXT);
    }
}