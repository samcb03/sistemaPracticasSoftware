package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.common.EmailCommon;
import uv.lis.logic.utils.InputValidator;

public class FXMLVerifyCodeController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLVerifyCodeController.class.getName());
    private static final String CODE_FIELD_NAME = "El código";
    private static final String INVALID_CODE_MESSAGE = "Código incorrecto o expirado. Intente de nuevo.";

    @FXML private TextField textFieldCode;
    @FXML private CheckBox checkBoxDisableEmailAuthentication;
    @FXML private Label labelMessage;
    @FXML private Button buttonVerify;

    private User user;
    private VerificationChallenge challenge;
    private UserDAO userDAO;
    private EmailCommon emailCommon;
    private boolean isVerified;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.userDAO = new UserDAO();
        this.emailCommon = new EmailCommon();
        this.isVerified = false;
        setupControls(labelMessage, null);
    }

    public void initializeData(User user, VerificationChallenge challenge) {
        this.user = user;
        this.challenge = challenge;
    }

    public boolean isVerified() {
        return isVerified;
    }

    @Override
    protected void clearFields() {
        textFieldCode.clear();
        checkBoxDisableEmailAuthentication.setSelected(false);
    }

    @FXML
    private void handleVerify() {
        String inputCode = textFieldCode.getText().trim();
        Optional<String> codeError = InputValidator.validateNotEmpty(inputCode, CODE_FIELD_NAME);

        if (codeError.isPresent()) {
            showError(codeError.get());
        } else if (emailCommon.verifyCode(challenge, inputCode)) {
            confirmVerification();
        } else {
            showError(INVALID_CODE_MESSAGE);
        }
    }

    @FXML
    private void handleToggleEmailAuthentication() {
        boolean isEmailAuthenticationActive = !checkBoxDisableEmailAuthentication.isSelected();

        try {
            userDAO.updateEmailAuthenticationPreference(user.getId(), isEmailAuthenticationActive);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar la preferencia de autenticación", e);
            showError(e.getMessage());
        }
    }

    private void confirmVerification() {
        isVerified = true;
        Stage stage = (Stage) buttonVerify.getScene().getWindow();
        stage.close();
    }
}