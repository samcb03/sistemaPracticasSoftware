package uv.lis.GUI.controller;


import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import static uv.lis.logic.utils.InputValidator.validatePassword;


public class FXMLLoginController implements Initializable {

    @FXML private Button buttonLogin;
    @FXML private TextField textFieldIdentification;
    @FXML private PasswordField fieldPassword;
    @FXML private Label labelError;

    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
    }

    @FXML
    public void handleLogin() {
        String identification = textFieldIdentification.getText().trim();
        String password = fieldPassword.getText();
        Optional<String> credentialError = validateCredentials(identification, password);
        if (credentialError.isPresent()) {
            showError(credentialError.get());
        } else {
            authenticateUser(identification, password);
        }
    }

    private Optional<String> validateCredentials(String identification, String password) {
        Optional<String> validationResult;
        if (identification.isEmpty()) {
            validationResult = Optional.of("La identificación no puede estar vacía");
        } else {
            validationResult = validatePassword(password);
        }
        return validationResult;
    }

    private void authenticateUser(String identification, String password) {
        try {
            User authenticatedUser = userDAO.authenticate(identification, password);
            navigateToUserMenu(authenticatedUser.getUserType());
        } catch (AuthenticateException authException) {
            showError(authException.getMessage());
        }
    }

    private void navigateToUserMenu(String userType) {
        Optional<String> fxmlPath = resolveFxmlPath(userType);
        fxmlPath.ifPresent(this::loadScene);
    }

    private Optional<String> resolveFxmlPath(String userType) {
        Optional<String> fxmlPath;
        switch (userType) {
            case "Estudiante":
                fxmlPath = Optional.of("/uv/lis/GUI/view/FXMLStudentMenu.fxml");
                break;
            case "Profesor":
                fxmlPath = Optional.of("/uv/lis/GUI/view/FXMLProfessorMenu.fxml");
                break;
            case "Coordinador":
                fxmlPath = Optional.of("/uv/lis/GUI/view/FXMLCoordinatorMenu.fxml");
                break;
            case "Administrador":
                fxmlPath = Optional.of("/uv/lis/GUI/view/FXMLAdministratorMenu.fxml");
                break;
            default:
                showError("Usuario no reconocido");
                fxmlPath = Optional.empty();
                break;
        }
        return fxmlPath;
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader sceneLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent sceneRoot = sceneLoader.load();
            Stage currentStage = (Stage) buttonLogin.getScene().getWindow();
            currentStage.setScene(new Scene(sceneRoot));
            currentStage.show();
        } catch (IOException ioException) {
            showError("Error al cargar la pantalla.");
        }
    }

    private void showError(String errorMessage) {
        labelError.setText(errorMessage);
        labelError.setStyle("-fx-text-fill: red;");
    }
}