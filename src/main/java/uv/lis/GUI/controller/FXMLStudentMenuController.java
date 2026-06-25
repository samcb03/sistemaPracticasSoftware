package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLStudentMenuController extends ValidationHandler {

    private static final String FINISH_WINDOW_VIEW = "/uv/lis/GUI/view/FXMLFinishWindow.fxml";
    private static final String REQUEST_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLRequestProject.fxml";
    private static final String REPORTS_VIEW = "/uv/lis/GUI/view/FXMLReportsMenu.fxml";
    private static final String AUTOEVALUATION_VIEW = "/uv/lis/GUI/view/FXMLGenerateAutoevaluation.fxml";
    private static final String UPLOAD_DOCUMENTS_VIEW = "/uv/lis/GUI/view/FXMLUploadDocuments.fxml";
    private static final String REGISTER_ACTIVITY_VIEW = "/uv/lis/GUI/view/FXMLRegisterActivity.fxml";
    private static final String NOTIFICATIONS_VIEW = "/uv/lis/GUI/view/FXMLNotifications.fxml";

    private static final String NO_SUBJECT_MESSAGE = "No tiene asignada una experiencia";
    private static final String PENDING_DOCUMENTS_MESSAGE = 
        "Debe subir sus documentos iniciales y esperar a que el profesor los valide";
    private static final int AUTOEVALUATION_DOCUMENT_TYPE = 1;
    private static final int EVALUATION_DOCUMENT_TYPE = 12;
    private static final int LIBERATION_LETTER_DOCUMENT_TYPE = 13;

    @FXML private Button buttonRequestProject;
    @FXML private Button buttonReports;
    @FXML private Button buttonAutoevaluation;
    @FXML private Button buttonUploadDocuments;
    @FXML private Button buttonLogOut;
    @FXML private Button buttonRegisterActivity;
    @FXML private Label labelMessage;
    @FXML private Button buttonNotifications;

    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ExpedientDAO expedientDAO = new ExpedientDAO();

    private Student student;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        student = SessionManager.getInstance().getCurrentStudent();
        setupControls(labelMessage, buttonLogOut);
        try {
            goToFinishWindows();
            disableActionsWithoutAssignedSubject();
            disableActionsWithoutAssignedProject();
            checkFinalReportValidation();
            disableActionsWithoutValidatedDocuments();
            disableActionWithAssignedProject();
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void goToRequestProject() {
        navigateTo(REQUEST_PROJECT_VIEW);
    }

    @FXML
    public void goToReports() {
        navigateTo(REPORTS_VIEW);
    }

    @FXML   
    public void goToAutoevaluation() {
        navigateTo(AUTOEVALUATION_VIEW);
    }

    @FXML 
    public void goToUploadDocuments() {
        navigateTo(UPLOAD_DOCUMENTS_VIEW);
    }

    @FXML 
    public void goToRegisterActivity() {
        navigateTo(REGISTER_ACTIVITY_VIEW);
    }

    @FXML
    public void goToNotifications() {
        navigateTo(NOTIFICATIONS_VIEW);
    }

    @FXML 
    public void goToFinishWindows() {
        try {
            boolean isAutoevaluationValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(), AUTOEVALUATION_DOCUMENT_TYPE);
            boolean isOrganizationEvaluationValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(),EVALUATION_DOCUMENT_TYPE);
            boolean isLiberationLetterValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(), LIBERATION_LETTER_DOCUMENT_TYPE);
            if(isAutoevaluationValidated && isOrganizationEvaluationValidated && isLiberationLetterValidated) {
                navigateTo(FINISH_WINDOW_VIEW);
            }
        } catch(OperationException e) {
            showError(e.getMessage());
        }
    }

    @Override
    protected void clearFields() {

    }

    private void disableActionsWithoutAssignedProject() throws OperationException {
        String studentId = student.getIdStudent();

        if (!studentDAO.hasProjectAssigned(studentId)) {
            buttonReports.setDisable(true);
            buttonRegisterActivity.setDisable(true);
        }
    }

    private void disableActionsWithoutAssignedSubject() throws OperationException {
        String subjectNRC = subjectDAO.getSubjectNrcByStudentID(student.getIdStudent());
        if (NO_SUBJECT_MESSAGE.equals(subjectNRC)) {
            buttonRequestProject.setDisable(true);
            buttonReports.setDisable(true);
        }
    }

    private void disableActionWithAssignedProject() throws OperationException {
        String studentId = student.getIdStudent();
        if(studentDAO.hasProjectAssigned(studentId)) {
            buttonRequestProject.setDisable(true);
        }
    }

    private void disableActionsWithoutValidatedDocuments() throws OperationException {
        if (!expedientDAO.areInitialDocumentsValidated(student.getIdStudent())) {
            buttonReports.setDisable(true);
            buttonRegisterActivity.setDisable(true);
            showError(PENDING_DOCUMENTS_MESSAGE);
        }
    }

    private void checkFinalReportValidation() { 
        try {
            boolean isValidated = expedientDAO.isFinalReportValidated(student.getIdStudent());
 
            if (!isValidated) {
                buttonAutoevaluation.setDisable(true);
            }
        } catch (OperationException e) {
            buttonAutoevaluation.setDisable(true);
            showError(e.getMessage());
        }
    }
}