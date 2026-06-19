package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterProfessorController;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProfessorControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterProfessor.fxml";
    private static final String PROFESSOR_DAO_FIELD = "professorDAO";

    private static final String FIRST_NAME_FIELD_SELECTOR = "#textFieldFirstName";
    private static final String LAST_NAME_FIELD_SELECTOR = "#textFieldLastName";
    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String PASSWORD_FIELD_SELECTOR = "#passwordFieldPassword";
    private static final String PERSONNEL_NUMBER_FIELD_SELECTOR = "#textFieldPersonnelNumber";
    private static final String REGISTER_BUTTON_SELECTOR = "#buttonRegister";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_FIRST_NAME = "Juan";
    private static final String VALID_LAST_NAME = "Perez";
    private static final String VALID_EMAIL = "juan.perez@gmail.com";
    private static final String VALID_PASSWORD = "Passw0rd!";
    private static final String VALID_PERSONNEL_NUMBER = "12345";
    private static final String INVALID_EMAIL = "juan@gmail";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_PROFESSOR_DATA =
        VALID_FIRST_NAME + FIELD_SEPARATOR + VALID_LAST_NAME + FIELD_SEPARATOR + VALID_EMAIL
        + FIELD_SEPARATOR + VALID_PASSWORD + FIELD_SEPARATOR + VALID_PERSONNEL_NUMBER;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Profesor registrado correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar al profesor";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE =
        "El correo electrónico no tiene un formato válido";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private FXMLRegisterProfessorController registerController;
    private ProfessorDAO professorDAOMock;

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
        professorDAOMock = mock(ProfessorDAO.class);
        injectProfessorDAO(professorDAOMock);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws Exception {
        when(professorDAOMock.registerProfessor(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws Exception {
        ArgumentCaptor<Professor> professorCaptor = ArgumentCaptor.forClass(Professor.class);
        when(professorDAOMock.registerProfessor(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        verify(professorDAOMock).registerProfessor(professorCaptor.capture());
        assertEquals(EXPECTED_PROFESSOR_DATA, professorData(professorCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsErrorMessage() throws Exception {
        when(professorDAOMock.registerProfessor(any())).thenReturn(false);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsExceptionMessage() throws Exception {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(professorDAOMock.registerProfessor(any())).thenThrow(operationException);

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

    private void injectProfessorDAO(ProfessorDAO professorDAOInstance) throws Exception {
        Field professorDAOField =
            FXMLRegisterProfessorController.class.getDeclaredField(PROFESSOR_DAO_FIELD);
        professorDAOField.setAccessible(true);
        professorDAOField.set(registerController, professorDAOInstance);
    }

    private void fillForm(String email) {
        clickOn(FIRST_NAME_FIELD_SELECTOR).write(VALID_FIRST_NAME);
        clickOn(LAST_NAME_FIELD_SELECTOR).write(VALID_LAST_NAME);
        clickOn(EMAIL_FIELD_SELECTOR).write(email);
        clickOn(PASSWORD_FIELD_SELECTOR).write(VALID_PASSWORD);
        clickOn(PERSONNEL_NUMBER_FIELD_SELECTOR).write(VALID_PERSONNEL_NUMBER);
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String professorData(Professor professor) {
        String data = professor.getFirstName() + FIELD_SEPARATOR + professor.getLastName() + FIELD_SEPARATOR
            + professor.getEmail() + FIELD_SEPARATOR + professor.getPassword() + FIELD_SEPARATOR
            + professor.getPersonnelNumber();
        return data;
    }
}