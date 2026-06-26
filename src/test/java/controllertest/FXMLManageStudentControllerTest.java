package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLManageStudentController;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLManageStudentControllerTest extends ApplicationTest {

    private static final String MANAGE_VIEW_FXML = "/uv/lis/GUI/view/FXMLManageStudent.fxml";
    private static final String STUDENT_DAO_FIELD = "studentDAO";
    private static final String SUBJECT_DAO_FIELD = "subjectDAO";
    private static final String REQUEST_PROJECT_DAO_FIELD = "requestProjectDAO";
    private static final String ACTIVITY_DAO_FIELD = "activityDAO";
    private static final String REPORT_CONTEXT_DAO_FIELD = "reportContextDAO";

    private static final String NAME_FIELD_SELECTOR = "#textFieldName";
    private static final String UPDATE_BUTTON_SELECTOR = "#buttonUpdate";
    private static final String SAVE_BUTTON_SELECTOR = "#buttonSave";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final int STUDENT_PK_ID = 3;
    private static final String VALID_STUDENT_ID = "S12345678";
    private static final String VALID_FIRST_NAME = "Ana";
    private static final String VALID_LAST_NAME = "Lopez";
    private static final String VALID_GENDER = "Mujer";
    private static final String VALID_BIRTH_DATE = "2000-01-01";
    private static final String EMPTY_VALUE = "";
    private static final String DEFAULT_HOURS = "0";

    private static final String EXPECTED_UPDATE_SUCCESS_MESSAGE = "Alumno actualizado correctamente";
    private static final String EXPECTED_NO_CHANGES_MESSAGE = "No se realizaron cambios en el alumno";
    private static final String EXPECTED_EMPTY_NAME_MESSAGE = "El nombre no puede estar vacío";

    private Stage primaryStage;
    private FXMLManageStudentController manageController;
    private StudentDAO studentDAOMock;
    private SubjectDAO subjectDAOMock;
    private RequestProjectDAO requestProjectDAOMock;
    private ActivityDAO activityDAOMock;
    private ReportContextDAO reportContextDAOMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentProfessor(buildCoordinatorProfessor());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(MANAGE_VIEW_FXML));
        Parent root = loader.load();
        manageController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
    }

    @BeforeEach
    void setUpMocks() throws ReflectiveOperationException {
        studentDAOMock = mock(StudentDAO.class);
        subjectDAOMock = mock(SubjectDAO.class);
        requestProjectDAOMock = mock(RequestProjectDAO.class);
        activityDAOMock = mock(ActivityDAO.class);
        reportContextDAOMock = mock(ReportContextDAO.class);

        injectField(STUDENT_DAO_FIELD, studentDAOMock);
        injectField(SUBJECT_DAO_FIELD, subjectDAOMock);
        injectField(REQUEST_PROJECT_DAO_FIELD, requestProjectDAOMock);
        injectField(ACTIVITY_DAO_FIELD, activityDAOMock);
        injectField(REPORT_CONTEXT_DAO_FIELD, reportContextDAOMock);
    }

    @Test
    void saveStudent_validData_showsSuccessMessage() throws OperationException {
        stubAcademicInfo();
        when(studentDAOMock.modifyStudent(any())).thenReturn(true);

        loadStudentData();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_UPDATE_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void saveStudent_noChangesApplied_showsErrorMessage() throws OperationException {
        stubAcademicInfo();
        when(studentDAOMock.modifyStudent(any())).thenReturn(false);

        loadStudentData();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_NO_CHANGES_MESSAGE, messageText());
    }

    @Test
    void saveStudent_emptyFirstName_showsValidationError() throws OperationException {
        stubAcademicInfo();

        loadStudentData();
        enterEditMode();
        interact(() -> lookup(NAME_FIELD_SELECTOR).queryAs(TextField.class).setText(EMPTY_VALUE));
        clickSave();

        assertEquals(EXPECTED_EMPTY_NAME_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLManageStudentController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manageController, value);
    }

    private Professor buildCoordinatorProfessor() {
        Professor professor = new Professor();
        professor.setIsCoordinator(true);
        return professor;
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setId(STUDENT_PK_ID);
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_FIRST_NAME);
        student.setLastName(VALID_LAST_NAME);
        student.setBirthDate(Date.valueOf(VALID_BIRTH_DATE));
        student.setGender(VALID_GENDER);
        return student;
    }

    private void stubAcademicInfo() throws OperationException {
        when(studentDAOMock.isStudentInactive(anyString())).thenReturn(false);
        when(subjectDAOMock.getSubjectNrcByStudentID(anyString())).thenReturn(EMPTY_VALUE);
        when(requestProjectDAOMock.getProjectAssignedToStudent(anyString())).thenReturn(EMPTY_VALUE);
        when(activityDAOMock.getActivitiesByStudentId(anyString())).thenReturn(new ArrayList<Activity>());
        when(reportContextDAOMock.getTotalReportedHoursByStudentId(anyString())).thenReturn(DEFAULT_HOURS);
    }

    private void loadStudentData() {
        interact(() -> manageController.initializeData(buildStudent()));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void enterEditMode() {
        clickOn(UPDATE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickSave() {
        clickOn(SAVE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }
}