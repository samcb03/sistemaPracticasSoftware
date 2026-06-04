package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLStudentMenuController extends ValidationHandler {

    private static final String NO_SUBJECT_MESSAGE = "No tiene asignada una experiencia";

    @FXML private Button buttonRequestProject;
    @FXML private Button buttonReports;
    @FXML private Button buttonAutoevaluation;
    @FXML private Button buttonUploadDocuments;
    @FXML private Button buttonLogOut;
    @FXML private Button buttonRegisterActivity;
    @FXML private Label labelMessage;
    @FXML private Button buttonNotifications;

    private Student student;
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private static final int MAX_HOURS = 420;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.student = SessionManager.getInstance().getCurrentStudent();
        try {
            //disableActionsWithoutAssignedSubject();
            //disableActionsWithoutAssignedProject();
            disableAutoevaluation();
        } catch (OperationException e) {
            showError(e.getMessage());
        }
        setupControls(labelMessage, buttonLogOut);
    }

    @FXML
    public void goToRequestProject() {
        navigateTo("/uv/lis/GUI/view/FXMLRequestProject.fxml");
    }

    private void disableActionsWithoutAssignedSubject() throws OperationException {
        String subjectNRC = subjectDAO.getSubjectNRCByStudentID(student.getIdStudent());
        if (NO_SUBJECT_MESSAGE.equals(subjectNRC)) {
            buttonRequestProject.setDisable(true);
            buttonReports.setDisable(true);
        }
    }

    @FXML
    public void goToReports() {
        navigateTo("/uv/lis/GUI/view/FXMLReportsMenu.fxml");
    }

    private void disableActionsWithoutAssignedProject() throws OperationException {
        String studentId = student.getIdStudent();

        if (!studentDAO.hasProjectAssigned(studentId)) {
            buttonReports.setDisable(true);
            buttonRegisterActivity.setDisable(true);
            buttonUploadDocuments.setDisable(true);
        }
    }

    @FXML   
    public void goToAutoevaluation() {
        navigateTo("/uv/lis/GUI/view/FXMLGenerateAutoevaluation.fxml");
    }

    private void disableAutoevaluation() throws OperationException {
        if (student.getCompletedHours() < MAX_HOURS) {
            buttonAutoevaluation.setDisable(true);
        }
    }
 
    @FXML 
    public void goToUploadDocuments() {
        navigateTo("/uv/lis/GUI/view/FXMLUploadDocuments.fxml");
    }

    @FXML 
    public void goToRegisterActivity() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterActivity.fxml");
    }

    @FXML
    public void goToNotifications() {
        navigateTo("/uv/lis/GUI/view/FXMLNotifications.fxml");
    }

    @Override
    protected void clearFields() {
    }

}