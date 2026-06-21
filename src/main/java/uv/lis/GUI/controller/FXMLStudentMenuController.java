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

    private static final String NO_SUBJECT_MESSAGE = "No tiene asignada una experiencia";
    private static final String PENDING_DOCUMENTS_MESSAGE = 
        "Debe subir sus documentos iniciales y esperar a que el profesor los valide";
    private static final int AUTOEVALUATION_DOCUMENT_TYPE = 1;

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
    private final ExpedientDAO expedientDAO = new ExpedientDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.student = SessionManager.getInstance().getCurrentStudent();
        setupControls(labelMessage, buttonLogOut);
        try {
            boolean isAutoevaluationValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(),AUTOEVALUATION_DOCUMENT_TYPE);
            
            if(isAutoevaluationValidated) {
                navigateTo("/uv/lis/GUI/view/FXMLFinishWindow.fxml");
            } else {
            disableActionsWithoutAssignedSubject();
            disableActionsWithoutAssignedProject();
            checkFinalReportValidation();
            disableActionsWithoutValidatedDocuments();
            }
        } catch (OperationException e) {
            showError("No se pudo inicializar");
        }
    }

    @FXML
    public void goToRequestProject() {
        navigateTo("/uv/lis/GUI/view/FXMLRequestProject.fxml");
    }

    private void disableActionsWithoutAssignedSubject() throws OperationException {
        String subjectNRC = subjectDAO.getSubjectNrcByStudentID(student.getIdStudent());
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
        }
    }

    @FXML   
    public void goToAutoevaluation() {
        navigateTo("/uv/lis/GUI/view/FXMLGenerateAutoevaluation.fxml");
    }

    private void checkFinalReportValidation() { 
        try {
            boolean isValidated = expedientDAO.isFinalReportValidated(student.getIdStudent());
 
            if (!isValidated) {
                buttonAutoevaluation.setDisable(true);
            }
        } catch (OperationException e) {
            buttonAutoevaluation.setDisable(true);
            showError("No se pudo verificar el estado del reporte final. Intente más tarde.");
        }
    }

    private void disableActionsWithoutValidatedDocuments() throws OperationException {
        if (!expedientDAO.areInitialDocumentsValidated(student.getIdStudent())) {
            buttonReports.setDisable(true);
            buttonRegisterActivity.setDisable(true);
            showError(PENDING_DOCUMENTS_MESSAGE);
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

    @FXML 
    public void goToFinishWindows() {
        try {
            boolean validatedAutoevaluation = expedientDAO.isDocumentTypeValidated(student.getIdStudent(), AUTOEVALUATION_DOCUMENT_TYPE);
            if(validatedAutoevaluation) {
                navigateTo("/uv/lis/GUI/view/FXMLFinishWindow.fxml");
            } else {
                showError("Debe completar y validar la autoevaluación");
            }
        } catch(OperationException e) {
            showError("No se pudo cargar la pantalla final");
        }
    }

    @Override
    protected void clearFields() {
    }
}