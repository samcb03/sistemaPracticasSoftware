package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import uv.lis.GUI.controller.FXMLUploadDocumentsController;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLUploadDocumentsControllerTest extends ApplicationTest {
    private static final String UPLOAD_DOCUMENTS_VIEW_FXML = "/uv/lis/GUI/view/FXMLUploadDocuments.fxml";


    private static final String EXPEDIENT_DAO_FIELD = "expedientDAO";
    private static final String STUDENT_DAO_FIELD = "studentDAO";
    private static final String STUDENT_FIELD = "student";
    private static final String REPORT_DAO_FIELD = "reportDAO";
    private static final String AUTOEVALUATION_DAO_FIELD = "autoevaluationDAO";
    private static final String LABEL_MESSAGE_ERROR = "#labelError";

    private static final String LOAD_DOCUMENT_TYPES_METHOD = "loadDocumentTypes";
    private static final String LOAD_STUDENT_DOCUMENTS = "loadStudentDocuments";

    private static final String BUTTON_UPLOAD_DOCUMENT_SELECTOR = "#buttonUploadDocument";
    private static final String COMBOBOX_DOCUMENTS = "#comboBoxDocuments";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_STUDENT_NAME = "Denisse";
    private static final String VALID_LAST_NAME = "Reyes";

    private static final String DOCUMENT_TYPE_AUTOEVALUATION = "Autoevaluacion";
    private static final String DOCUMENT_TYPE_MONTHLY_REPORT = "Reporte mensual-Enero";
    private static final String DOCUMENT_TYPE_ACCEPTANCE_LETTER = "Carta de aceptación";

    private static final int DOCUMENT_TYPE_ID_AUTOEVALUATION = 1;
    private static final int DOCUMENT_TYPE_ID_MONTHLY_REPORT = 3;
    private static final int DOCUMENT_TYPE_ID_ACCEPTANCE_LETTER = 9;

    private static final String EXPECTED_NO_TYPE_MESSAGE = "Seleccione un tipo de documento.";
    private static final String EXPECTED_NO_PROJECT_MESSAGE =
        "La carta de aceptación solo puede subirse cuando ya tenga un proyecto asignado.";
    private static final String EXPECTED_NOT_SAVED_MESSAGE = 
        "Este documento solo puede subirse cuando ya esté generado.";
    private static final String EXPECTED_ALREADY_VALIDATED_MESSAGE =
        "Este documento ya fue validado y no puede subirse nuevamente.";
    private static final String EXPECTED_LOAD_TYPES_ERROR = "Error al cargar documentos";
    private static final String EXPECTED_LOAD_DOCUMENTS_ERROR = "Error al cargar los documentos subidos";
    private static final String EXPECTED_OPERATION_ERROR_MESSAGE = "Error de operación de prueba";
    private static final String EXPECTED_NO_DOCUMENTS_MESSAGE = "Aún no ha subido documentos.";

    private Stage primaryStage;
    private FXMLUploadDocumentsController controller;
    private ExpedientDAO expedientDAOMock;
    private StudentDAO studentDAOMock;
    private AutoevaluationDAO autoevaluationDAOMock;
    private ReportDAO reportDAOMock;
    private Student studentMock;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(studentMock);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(UPLOAD_DOCUMENTS_VIEW_FXML));
            Parent root = loader.load();
            controller = loader.getController();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    } 

    @BeforeEach
    void setupMocks() throws Exception {
        expedientDAOMock = mock(ExpedientDAO.class);
        studentDAOMock = mock(StudentDAO.class);
        autoevaluationDAOMock = mock(AutoevaluationDAO.class);
        reportDAOMock = mock(ReportDAO.class);

        when(expedientDAOMock.getAllDocumentsTypes()).thenReturn(new ArrayList<>());
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(new ArrayList<>());

        injectField(EXPEDIENT_DAO_FIELD, expedientDAOMock);
        injectField(STUDENT_DAO_FIELD, studentDAOMock);
        injectField(REPORT_DAO_FIELD, reportDAOMock);
        injectField(AUTOEVALUATION_DAO_FIELD, autoevaluationDAOMock);
        injectField(STUDENT_FIELD, buildStudent());

        interact(() -> clearLabelMessage());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void uploadDocument_noDocumentTypeSelected_showsNoTypeMessage() {
        clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NO_TYPE_MESSAGE, messageText());
    }

    @Test
    void uploadDocument_noDocumentTypeSelected_doesNotCallExpedientDAO() throws OperationException {
            clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
            WaitForAsyncUtils.waitForFxEvents();
            verify(expedientDAOMock, never()).getIdDocumentTypeByName(anyString());
    }

    @Test
    void uploadDocument_acceptanceLetterWithoutProject_showsNoProjectMessage() 
        throws OperationException{
            when(expedientDAOMock.getIdDocumentTypeByName(DOCUMENT_TYPE_ACCEPTANCE_LETTER))
                .thenReturn(Optional.of(DOCUMENT_TYPE_ID_ACCEPTANCE_LETTER));
            when(expedientDAOMock.isDocumentTypeValidated(anyString(), anyInt())).thenReturn(false);
            when(studentDAOMock.hasProjectAssigned(VALID_STUDENT_ID)).thenReturn(false);

            selectDocumentType(DOCUMENT_TYPE_ACCEPTANCE_LETTER);
            clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
            WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NO_PROJECT_MESSAGE, messageText());
    }

    @Test
    void uploadDocument_autoevaluationNotGenerated_showsNotSavedMessage() throws OperationException {
        when(expedientDAOMock.getIdDocumentTypeByName(DOCUMENT_TYPE_AUTOEVALUATION))
            .thenReturn(Optional.of(DOCUMENT_TYPE_ID_AUTOEVALUATION));
        when(expedientDAOMock.isDocumentTypeValidated(anyString(), anyInt())).thenReturn(false);
        when(autoevaluationDAOMock.existsByStudent(VALID_STUDENT_ID)).thenReturn(false);

        selectDocumentType(DOCUMENT_TYPE_AUTOEVALUATION);
        clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NOT_SAVED_MESSAGE, messageText());
    }

    @Test
    void uploadDocument_documentAlreadyValidated_showsAlreadyValidatedMessage() throws OperationException {
        when(expedientDAOMock.getIdDocumentTypeByName(DOCUMENT_TYPE_AUTOEVALUATION))
            .thenReturn(Optional.of(DOCUMENT_TYPE_ID_AUTOEVALUATION));
        when(expedientDAOMock.isDocumentTypeValidated(VALID_STUDENT_ID, DOCUMENT_TYPE_ID_AUTOEVALUATION))
            .thenReturn(true);

        selectDocumentType(DOCUMENT_TYPE_AUTOEVALUATION);
        clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_ALREADY_VALIDATED_MESSAGE, messageText());
    }

    @Test
    void uploadDocument_monthlyReportNoPending_showsNotSavedMessage() throws OperationException {
        when(expedientDAOMock.getIdDocumentTypeByName(DOCUMENT_TYPE_MONTHLY_REPORT))
            .thenReturn(Optional.of(DOCUMENT_TYPE_ID_MONTHLY_REPORT));
        when(expedientDAOMock.isDocumentTypeValidated(anyString(), anyInt())).thenReturn(false);
        when(reportDAOMock.getUploadableMonthlyReports(VALID_STUDENT_ID)).thenReturn(List.of());

        selectDocumentType(DOCUMENT_TYPE_MONTHLY_REPORT);
        clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NOT_SAVED_MESSAGE, messageText());
    }

    @Test
    void uploadDocument_restrictionCheckThrowsOperationException_showsErrorMessage() throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(expedientDAOMock.getIdDocumentTypeByName(anyString())).thenThrow(operationException);

        selectDocumentType(DOCUMENT_TYPE_AUTOEVALUATION);
        clickOn(BUTTON_UPLOAD_DOCUMENT_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void loadDocumentTypes_daoThrowsOperationException_showsLoadTypesError() throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(expedientDAOMock.getAllDocumentsTypes()).thenThrow(operationException);

        invokeMethod(LOAD_DOCUMENT_TYPES_METHOD);

        assertEquals(EXPECTED_LOAD_TYPES_ERROR, messageText());
    }

    @Test
    void loadStudentDocuments_daoThrowsOperationException_showsLoadDocumentsError() throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenThrow(operationException);

        invokeMethod(LOAD_STUDENT_DOCUMENTS);

        assertEquals(EXPECTED_LOAD_DOCUMENTS_ERROR, messageText());
    }

    @Test
    void loadStudentDocuments_emptyList_showsNoDocumentsMessage() throws OperationException {
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(new ArrayList<>());

        invokeMethod(LOAD_STUDENT_DOCUMENTS);

        assertEquals(EXPECTED_NO_DOCUMENTS_MESSAGE, messageText());
    }

    @Test

    private Student buildStudent() {
        Student student = new Student();
        student.setFirstName(VALID_STUDENT_NAME);
        student.setIdStudent(VALID_STUDENT_ID);
        student.setLastName(VALID_LAST_NAME);
        return student;
    }

    private void injectField(String fieldName, Object value) {
        try {
            Field field = FXMLUploadDocumentsController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller,value);

        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

        private void invokeMethod(String methodName) {
        try {
            Method method = FXMLUploadDocumentsController.class
                .getDeclaredMethod(methodName);
            method.setAccessible(true);
            interact(() -> invokeAccessibleMethod(method));
            WaitForAsyncUtils.waitForFxEvents();
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new RuntimeException(noSuchMethodException);
        }
    }

    private void invokeAccessibleMethod(Method method) {
        try {
            method.invoke(controller);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    @SuppressWarnings("unchecked")
    private void selectDocumentType(String documentType) {
        interact(() -> {
            ComboBox<String> comboBox = lookup(COMBOBOX_DOCUMENTS).queryAs(ComboBox.class);
            comboBox.getItems().add(documentType);
            comboBox.setValue(documentType);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clearLabelMessage() {
        lookup(LABEL_MESSAGE_ERROR).queryAs(Label.class).setText("");

    }

    private String messageText() {
        return lookup(LABEL_MESSAGE_ERROR).queryAs(Label.class).getText();
    }

}