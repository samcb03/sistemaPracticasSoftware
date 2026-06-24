package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLAssignStudentSubjectController;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public class FXMLAssignStudentSubjectControllerTest extends ApplicationTest {

    private static final String ASSIGN_VIEW_FXML = "/uv/lis/GUI/view/FXMLAssignStudentSubject.fxml";
    private static final String SUBJECT_DAO_FIELD = "subjectDAO";
    private static final String STUDENT_DAO_FIELD = "studentDAO";

    private static final String SUBJECT_COMBO_SELECTOR = "#comboBoxSubjects";
    private static final String STUDENT_TABLE_SELECTOR = "#tableViewStudents";
    private static final String ASSIGN_BUTTON_SELECTOR = "#buttonAssign";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final int VALID_NRC = 50563;
    private static final String VALID_STUDENT_ID = "S24013305";
    private static final String VALID_FIRST_NAME = "Ana";
    private static final String VALID_LAST_NAME = "Lopez";
    private static final int VALID_PERIOD_ID = 1;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Alumno asignado correctamente.";
    private static final String EXPECTED_FAILURE_MESSAGE = "No se pudo asignar el alumno. Intente de nuevo.";
    private static final String EXPECTED_NO_SUBJECT_MESSAGE = "Seleccione una Experiencia Educativa";
    private static final String DAO_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLAssignStudentSubjectController assignController;
    private SubjectDAO subjectDAOMock;
    private StudentDAO studentDAOMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ASSIGN_VIEW_FXML));
        Parent root = loader.load();
        assignController = loader.getController();

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
        subjectDAOMock = mock(SubjectDAO.class);
        studentDAOMock = mock(StudentDAO.class);

        injectField(SUBJECT_DAO_FIELD, subjectDAOMock);
        injectField(STUDENT_DAO_FIELD, studentDAOMock);
    }

    @Test
    void validateFields_validSelection_showsSuccessMessage() throws OperationException {
        when(subjectDAOMock.assignStudentToSubject(anyString(), anyInt() ,anyInt())).thenReturn(true);
        when(studentDAOMock.getActiveStudentsNotInSubject()).thenReturn(new ArrayList<>());

        selectSubject();
        selectStudent();
        clickAssign();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validSelection_sendsDataToDAO() throws OperationException {
        when(subjectDAOMock.assignStudentToSubject(anyString(), anyInt(),anyInt())).thenReturn(true);
        when(studentDAOMock.getActiveStudentsNotInSubject()).thenReturn(new ArrayList<>());

        selectSubject();
        selectStudent();
        clickAssign();

        verify(subjectDAOMock).assignStudentToSubject(VALID_STUDENT_ID, VALID_NRC,VALID_PERIOD_ID);
    }

    @Test
    void validateFields_assignmentFails_showsErrorMessage() throws OperationException {
        when(subjectDAOMock.assignStudentToSubject(anyString(), anyInt(), anyInt())).thenReturn(false);

        selectSubject();
        selectStudent();
        clickAssign();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_daoError_showsExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DAO_ERROR_MESSAGE, null);
        when(subjectDAOMock.assignStudentToSubject(anyString(), anyInt(), anyInt())).thenThrow(operationException);

        selectSubject();
        selectStudent();
        clickAssign();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_noSubjectSelected_showsValidationError() {
        selectStudent();

        clickAssign();

        assertEquals(EXPECTED_NO_SUBJECT_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLAssignStudentSubjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(assignController, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_FIRST_NAME);
        student.setLastName(VALID_LAST_NAME);
        return student;
    }

    @SuppressWarnings("unchecked")
    private void selectSubject() {
        interact(() -> {
            Subject subject = new Subject();
            subject.setNrc(VALID_NRC);
            subject.setName("Practicas Profesionales");
            subject.setSchoolPeriodName("Febrero-Julio 2026");
            subject.setSchoolPeriodId(VALID_PERIOD_ID);

            ComboBox<Subject> comboBox = lookup(SUBJECT_COMBO_SELECTOR).queryAs(ComboBox.class);
            comboBox.getItems().setAll(subject);
            comboBox.getSelectionModel().select(subject);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private void selectStudent() {
        interact(() -> {
            TableView<Student> tableView = lookup(STUDENT_TABLE_SELECTOR).queryAs(TableView.class);
            tableView.getItems().setAll(buildStudent());
            tableView.getSelectionModel().select(0);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickAssign() {
        clickOn(ASSIGN_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }
}