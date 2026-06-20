package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLLoginController;
import uv.lis.logic.common.EmailCommon;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.EmailException;
import uv.lis.logic.exceptions.OperationException;

public class FXMLLoginControllerTest extends ApplicationTest {

    private static final String LOGIN_VIEW_FXML = "/uv/lis/GUI/view/FXMLLogin.fxml";

    private static final String USER_DAO_FIELD = "userDAO";
    private static final String STUDENT_DAO_FIELD = "studentDAO";
    private static final String PROFESSOR_DAO_FIELD = "professorDAO";
    private static final String EMAIL_COMMON_FIELD = "emailCommon";

    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String PASSWORD_FIELD_SELECTOR = "#passwordFieldPassword";
    private static final String PLAIN_PASSWORD_FIELD_SELECTOR = "#textFieldPasswordVisible";
    private static final String SHOW_PASSWORD_TOGGLE_SELECTOR = "#toggleButtonShowPassword";
    private static final String LOGIN_BUTTON_SELECTOR = "#buttonLogin";
    private static final String ERROR_LABEL_SELECTOR = "#labelError";
    private static final String ADMIN_MENU_BUTTON_SELECTOR = "#buttonRegisterProfessor";
    private static final String VERIFY_CODE_FIELD_SELECTOR = "#textFieldCode";

    private static final String VALID_EMAIL = "cbsam575@gmail.com";
    private static final String VALID_PASSWORD = "Leumas_03oal";
    private static final String INVALID_EMAIL = "cbsam575.com";
    private static final String INVALID_PASSWORD = "hola123";
    private static final String EMPTY_VALUE = "";

    private static final int ATTEMPTS_UNTIL_BLOCK = 5;
    private static final int USER_ID = 42;
    private static final int STUDENT_ROLE_ID = 1;
    private static final int PROFESSOR_ROLE_ID = 2;
    private static final int COORDINATOR_ROLE_ID = 3;
    private static final int ADMINISTRATOR_ROLE_ID = 4;

    private static final boolean EMAIL_AUTHENTICATION_ENABLED = true;
    private static final boolean EMAIL_AUTHENTICATION_DISABLED = false;

    private static final String EXPECTED_EMPTY_EMAIL_MESSAGE = "El correo electrónico no puede estar vacío";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE = "El correo electrónico no tiene un formato válido";
    private static final String EXPECTED_EMPTY_PASSWORD_MESSAGE = "La contraseña no puede estar vacía";
    private static final String EXPECTED_INVALID_PASSWORD_MESSAGE =
        "La contraseña debe tener una mayúscula, un carácter especial y un número";
    private static final String EXPECTED_FIRST_REMAINING_MESSAGE = "Credenciales inválidas. Intentos restantes: 4";
    private static final String EXPECTED_TOO_MANY_ATTEMPTS_MESSAGE =
        "Demasiados intentos fallidos. Reinicie la aplicación.";
    private static final String EXPECTED_USER_DATA_NOT_FOUND_MESSAGE = "No se encontraron los datos.";
    private static final String AUTHENTICATION_ERROR_MESSAGE = "Error de autenticación de prueba";
    private static final String OPERATION_ERROR_MESSAGE = "Error de operación de prueba";
    private static final String EMAIL_ERROR_MESSAGE = "Error de envío de correo de prueba";

    private Stage primaryStage;
    private FXMLLoginController loginController;
    private UserDAO userDAOMock;
    private StudentDAO studentDAOMock;
    private ProfessorDAO professorDAOMock;
    private EmailCommon emailCommonMock;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_VIEW_FXML));
        Parent root = loader.load();
        loginController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws Exception {
        userDAOMock = mock(UserDAO.class);
        studentDAOMock = mock(StudentDAO.class);
        professorDAOMock = mock(ProfessorDAO.class);
        emailCommonMock = mock(EmailCommon.class);

        injectField(USER_DAO_FIELD, userDAOMock);
        injectField(STUDENT_DAO_FIELD, studentDAOMock);
        injectField(PROFESSOR_DAO_FIELD, professorDAOMock);
        injectField(EMAIL_COMMON_FIELD, emailCommonMock);
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
    }

    @Test
    void handleLogin_emptyEmail_showsEmailRequiredError() {
        typeCredentials(EMPTY_VALUE, EMPTY_VALUE);

        clickLogin();

        assertEquals(EXPECTED_EMPTY_EMAIL_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_invalidEmailFormat_showsEmailFormatError() {
        typeCredentials(INVALID_EMAIL, VALID_PASSWORD);

        clickLogin();

        assertEquals(EXPECTED_INVALID_EMAIL_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_emptyPassword_showsPasswordRequiredError() {
        typeCredentials(VALID_EMAIL, EMPTY_VALUE);

        clickLogin();

        assertEquals(EXPECTED_EMPTY_PASSWORD_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_invalidPasswordFormat_showsPasswordCriteriaError() {
        typeCredentials(VALID_EMAIL, INVALID_PASSWORD);

        clickLogin();

        assertEquals(EXPECTED_INVALID_PASSWORD_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_wrongCredentials_showsRemainingAttemptsMessage() throws Exception {
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.empty());

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(EXPECTED_FIRST_REMAINING_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_maxFailedAttempts_showsTooManyAttemptsMessage() throws Exception {
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.empty());
        typeCredentials(VALID_EMAIL, VALID_PASSWORD);

        for (int attempt = 0; attempt < ATTEMPTS_UNTIL_BLOCK; attempt++) {
            clickLogin();
        }

        assertEquals(EXPECTED_TOO_MANY_ATTEMPTS_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_maxFailedAttempts_disablesLoginButton() throws Exception {
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.empty());
        typeCredentials(VALID_EMAIL, VALID_PASSWORD);

        for (int attempt = 0; attempt < ATTEMPTS_UNTIL_BLOCK; attempt++) {
            clickLogin();
        }

        assertTrue(isLoginButtonDisabled());
    }

    @Test
    void handleLogin_authenticateException_showsAuthenticateExceptionMessage() throws Exception {
        AuthenticateException authenticateException = new AuthenticateException(AUTHENTICATION_ERROR_MESSAGE);
        when(userDAOMock.authenticate(anyString(), anyString())).thenThrow(authenticateException);

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(authenticateException.getMessage(), errorLabelText());
    }

    @Test
    void handleLogin_studentRecordNotFound_showsStudentNotFoundError() throws Exception {
        User studentUser = buildUserMock(STUDENT_ROLE_ID, EMAIL_AUTHENTICATION_DISABLED);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(studentUser));
        when(studentDAOMock.getStudentById(anyInt())).thenReturn(Optional.empty());

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(EXPECTED_USER_DATA_NOT_FOUND_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_professorRecordNotFound_showsProfessorNotFoundError() throws Exception {
        User professorUser = buildUserMock(PROFESSOR_ROLE_ID, EMAIL_AUTHENTICATION_DISABLED);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(professorUser));
        when(professorDAOMock.getProfessorById(anyInt())).thenReturn(Optional.empty());

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(EXPECTED_USER_DATA_NOT_FOUND_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_coordinatorRecordNotFound_showsCoordinatorNotFoundError() throws Exception {
        User coordinatorUser = buildUserMock(COORDINATOR_ROLE_ID, EMAIL_AUTHENTICATION_DISABLED);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(coordinatorUser));
        when(professorDAOMock.getProfessorById(anyInt())).thenReturn(Optional.empty());

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(EXPECTED_USER_DATA_NOT_FOUND_MESSAGE, errorLabelText());
    }

    @Test
    void handleLogin_sessionLoadThrowsOperationException_showsOperationExceptionMessage() throws Exception {
        User studentUser = buildUserMock(STUDENT_ROLE_ID, EMAIL_AUTHENTICATION_DISABLED);
        OperationException operationException = new OperationException(OPERATION_ERROR_MESSAGE, null);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(studentUser));
        when(studentDAOMock.getStudentById(anyInt())).thenThrow(operationException);

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(operationException.getMessage(), errorLabelText());
    }

    @Test
    void handleLogin_emailVerificationFails_showsEmailErrorMessage() throws Exception {
        User studentUser = buildUserMock(STUDENT_ROLE_ID, EMAIL_AUTHENTICATION_ENABLED);
        EmailException emailException = new EmailException(EMAIL_ERROR_MESSAGE);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(studentUser));
        when(emailCommonMock.sendVerificationCode(anyString())).thenThrow(emailException);

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertEquals(emailException.getMessage(), errorLabelText());
    }

    @Test
    void handleLogin_administratorSuccess_navigatesToAdminMenu() throws Exception {
        User administratorUser = buildUserMock(ADMINISTRATOR_ROLE_ID, EMAIL_AUTHENTICATION_DISABLED);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(administratorUser));

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertTrue(isNodePresent(ADMIN_MENU_BUTTON_SELECTOR));
    }

    @Test
    void handleLogin_emailAuthenticationActive_opensVerificationWindow() throws Exception {
        User studentUser = buildUserMock(STUDENT_ROLE_ID, EMAIL_AUTHENTICATION_ENABLED);
        VerificationChallenge challenge = mock(VerificationChallenge.class);
        when(userDAOMock.authenticate(anyString(), anyString())).thenReturn(Optional.of(studentUser));
        when(emailCommonMock.sendVerificationCode(anyString())).thenReturn(challenge);

        typeCredentials(VALID_EMAIL, VALID_PASSWORD);
        clickLogin();

        assertTrue(isNodePresent(VERIFY_CODE_FIELD_SELECTOR));
    }

    @Test
    void togglePasswordVisibility_firstToggle_showsPlainTextField() {
        clickShowPasswordToggle();

        assertTrue(isPlainPasswordVisible());
    }

    @Test
    void togglePasswordVisibility_secondToggle_hidesPlainTextField() {
        clickShowPasswordToggle();

        clickShowPasswordToggle();

        assertFalse(isPlainPasswordVisible());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLLoginController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(loginController, value);
    }

    private User buildUserMock(int roleId, boolean emailAuthenticationActive) {
        User userMock = mock(User.class);
        when(userMock.getRoleId()).thenReturn(roleId);
        when(userMock.getId()).thenReturn(USER_ID);
        when(userMock.isEmailAuthenticationActive()).thenReturn(emailAuthenticationActive);
        when(userMock.getEmail()).thenReturn(VALID_EMAIL);
        return userMock;
    }

    private void typeCredentials(String email, String password) {
        clickOn(EMAIL_FIELD_SELECTOR).write(email);
        clickOn(PASSWORD_FIELD_SELECTOR).write(password);
    }

    private void clickLogin() {
        clickOn(LOGIN_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickShowPasswordToggle() {
        clickOn(SHOW_PASSWORD_TOGGLE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String errorLabelText() {
        return lookup(ERROR_LABEL_SELECTOR).queryAs(Label.class).getText();
    }

    private boolean isLoginButtonDisabled() {
        return lookup(LOGIN_BUTTON_SELECTOR).queryAs(Button.class).isDisabled();
    }

    private boolean isPlainPasswordVisible() {
        return lookup(PLAIN_PASSWORD_FIELD_SELECTOR).queryAs(TextField.class).isVisible();
    }

    private boolean isNodePresent(String selector) {
        return lookup(selector).tryQuery().isPresent();
    }
}