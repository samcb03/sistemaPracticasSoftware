package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterStudentController;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterStudentControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterStudent.fxml";
    private static final String STUDENT_DAO_FIELD = "studentDAO";

    private static final String FIRST_NAME_FIELD_SELECTOR = "#textFieldFirstName";
    private static final String LAST_NAME_FIELD_SELECTOR = "#textFieldLastName";
    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String PASSWORD_FIELD_SELECTOR = "#passwordFieldPassword";
    private static final String STUDENT_ID_FIELD_SELECTOR = "#textFieldStudentId";
    private static final String BIRTH_DATE_PICKER_SELECTOR = "#datePickerBirthDate";
    private static final String GENDER_COMBO_SELECTOR = "#comboBoxGender";
    private static final String REGISTER_BUTTON_SELECTOR = "#buttonRegister";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final String VALID_FIRST_NAME = "Juan";
    private static final String VALID_LAST_NAME = "Perez";
    private static final String VALID_EMAIL = "juan.perez@gmail.com";
    private static final String VALID_PASSWORD = "Passw0rd!";
    private static final String VALID_STUDENT_ID = "S12345678";
    private static final String VALID_GENDER = "Hombre";
    private static final LocalDate VALID_BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private static final String INVALID_EMAIL = "juan@gmail";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_STUDENT_DATA =
        VALID_FIRST_NAME + FIELD_SEPARATOR + VALID_LAST_NAME + FIELD_SEPARATOR + VALID_EMAIL
        + FIELD_SEPARATOR + VALID_PASSWORD + FIELD_SEPARATOR + VALID_STUDENT_ID
        + FIELD_SEPARATOR + VALID_GENDER;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Estudiante registrado correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar al estudiante";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE =
        "El correo electrónico no tiene un formato válido";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private FXMLRegisterStudentController registerController;
    private StudentDAO studentDAOMock;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(REGISTER_VIEW_FXML));
        Parent root = loader.load();
        registerController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMock() throws Exception {
        studentDAOMock = mock(StudentDAO.class);
        injectStudentDAO(studentDAOMock);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws Exception {
        when(studentDAOMock.registerStudent(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws Exception {
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        when(studentDAOMock.registerStudent(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        verify(studentDAOMock).registerStudent(studentCaptor.capture());
        assertEquals(EXPECTED_STUDENT_DATA, studentData(studentCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsOperationExceptionMessage() throws Exception {
        when(studentDAOMock.registerStudent(any())).thenReturn(false);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsOperationExceptionMessage() throws Exception {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(studentDAOMock.registerStudent(any())).thenThrow(operationException);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_invalidEmail_showsValidationError() {
        fillForm(INVALID_EMAIL);

        clickRegister();

        assertEquals(EXPECTED_INVALID_EMAIL_MESSAGE, messageText());
    }

    private void injectStudentDAO(StudentDAO studentDAOInstance) throws Exception {
        Field studentDAOField =
            FXMLRegisterStudentController.class.getDeclaredField(STUDENT_DAO_FIELD);
        studentDAOField.setAccessible(true);
        studentDAOField.set(registerController, studentDAOInstance);
    }

    private void fillForm(String email) {
        clickOn(FIRST_NAME_FIELD_SELECTOR).write(VALID_FIRST_NAME);
        clickOn(LAST_NAME_FIELD_SELECTOR).write(VALID_LAST_NAME);
        clickOn(EMAIL_FIELD_SELECTOR).write(email);
        clickOn(PASSWORD_FIELD_SELECTOR).write(VALID_PASSWORD);
        clickOn(STUDENT_ID_FIELD_SELECTOR).write(VALID_STUDENT_ID);
        interact(() -> lookup(BIRTH_DATE_PICKER_SELECTOR).queryAs(DatePicker.class).setValue(VALID_BIRTH_DATE));
        interact(() -> setComboBoxValue(GENDER_COMBO_SELECTOR, VALID_GENDER));
    }

    @SuppressWarnings("unchecked")
    private void setComboBoxValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.setValue(value);
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String studentData(Student student) {
        String data = student.getFirstName() + FIELD_SEPARATOR + student.getLastName() + FIELD_SEPARATOR
            + student.getEmail() + FIELD_SEPARATOR + student.getPassword() + FIELD_SEPARATOR
            + student.getIdStudent() + FIELD_SEPARATOR + student.getGender();
        return data;
    }
}