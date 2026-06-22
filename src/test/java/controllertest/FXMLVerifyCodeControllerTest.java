package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLVerifyCodeController;
import uv.lis.logic.common.EmailCommon;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.exceptions.OperationException;

public class FXMLVerifyCodeControllerTest extends ApplicationTest {

    private static final String VERIFY_CODE_VIEW_FXML = "/uv/lis/GUI/view/FXMLVerifyCode.fxml";
    private static final String USER_DAO_FIELD = "userDAO";
    private static final String EMAIL_COMMON_FIELD = "emailCommon";

    private static final String CODE_FIELD_SELECTOR = "#textFieldCode";
    private static final String CHECKBOX_SELECTOR = "#checkBoxDisableEmailAuthentication";
    private static final String VERIFY_BUTTON_SELECTOR = "#buttonVerify";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_CODE = "123456";
    private static final String INVALID_CODE = "000000";

    private static final int VALID_USER_ID = 1;

    private static final String EXPECTED_EMPTY_CODE_MESSAGE = "El código";
    private static final String EXPECTED_INVALID_CODE_MESSAGE =
        "Código incorrecto o expirado. Intente de nuevo.";
    private static final String EXPECTED_DAO_ERROR_MESSAGE = "Error de operación de prueba";

    private FXMLVerifyCodeController verifyCodeController;
    private UserDAO userDAOMock;
    private EmailCommon emailCommonMock;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VERIFY_CODE_VIEW_FXML));
        Parent root = loader.load();
        verifyCodeController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws Exception {
        userDAOMock = mock(UserDAO.class);
        emailCommonMock = mock(EmailCommon.class);

        injectField(USER_DAO_FIELD, userDAOMock);
        injectField(EMAIL_COMMON_FIELD, emailCommonMock);

        interact(() -> verifyCodeController.initializeData(builderUser(), builderChallenge()));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void handleVerify_emptyCode_showsValidationError() {
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(messageText().contains(EXPECTED_EMPTY_CODE_MESSAGE));
    }

    @Test
    void handleVerify_invalidCode_showsErrorMessage() {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(false);

        clickOn(CODE_FIELD_SELECTOR).write(INVALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_INVALID_CODE_MESSAGE, messageText());
    }

    @Test
    void handleVerify_validCode_setsIsVerifiedTrue() {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(true);

        clickOn(CODE_FIELD_SELECTOR).write(VALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(verifyCodeController.isVerified());
    }

    @Test
    void handleVerify_validCode_closesStage() {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(true);

        Stage stage = (Stage) lookup(VERIFY_BUTTON_SELECTOR).queryAs(
            javafx.scene.control.Button.class).getScene().getWindow();

        clickOn(CODE_FIELD_SELECTOR).write(VALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(stage.isShowing());
    }

    @Test
    void handleVerify_validCodeCheckboxUnchecked_doesNotCallUpdateDAO() throws OperationException {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(true);

        clickOn(CODE_FIELD_SELECTOR).write(VALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        verify(userDAOMock, never()).updateEmailAuthenticationPreference(anyInt(), anyBoolean());
    }

    @Test
    void handleVerify_validCodeCheckboxChecked_callsUpdateDAO() throws OperationException {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(true);

        clickOn(CHECKBOX_SELECTOR);
        clickOn(CODE_FIELD_SELECTOR).write(VALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        verify(userDAOMock).updateEmailAuthenticationPreference(anyInt(), anyBoolean());
    }

    @Test
    void handleVerify_checkboxCheckedDaoThrowsException_doesNotPropagateError() throws OperationException {
        when(emailCommonMock.verifyCode(any(), any())).thenReturn(true);
        OperationException operationException =
            new OperationException(EXPECTED_DAO_ERROR_MESSAGE, null);
        when(userDAOMock.updateEmailAuthenticationPreference(anyInt(), anyBoolean()))
            .thenThrow(operationException);

        clickOn(CHECKBOX_SELECTOR);
        clickOn(CODE_FIELD_SELECTOR).write(VALID_CODE);
        clickOn(VERIFY_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(verifyCodeController.isVerified());
    }

    private User builderUser() {
        User user = new User();
        user.setId(VALID_USER_ID);
        return user;
    }

    private VerificationChallenge builderChallenge() {
        return new VerificationChallenge();
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLVerifyCodeController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(verifyCodeController, value);
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }

}