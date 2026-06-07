package uv.lis.GUI.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLLoginController implements Initializable {

    private static final int USER_TYPE_STUDENT = 1;
    private static final int USER_TYPE_PROFESSOR = 2;
    private static final int USER_TYPE_COORDINATOR = 3;
    private static final int USER_TYPE_ADMINISTRATOR = 4;

    private static final Logger LOGGER = Logger.getLogger(FXMLLoginController.class.getName());
    
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private TextField textFieldPasswordVisible;
    @FXML private ToggleButton toggleButtonShowPassword;
    @FXML private Label labelError;
    @FXML private Button buttonLogin;

    private UserDAO userDAO;
    private User user;

    private Image eyeOpen;
    private Image eyeClosed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.userDAO = new UserDAO();
        textFieldPasswordVisible.textProperty().bindBidirectional(passwordFieldPassword.textProperty());
        eyeOpen = new Image(getClass().getResourceAsStream("/uv/lis/GUI/view/images/show-password-icon-eye-symbol" 
            + "-vision-hide-from-watch-icon-secret-view-web-design-element-vector2.png"));
        eyeClosed = new Image(getClass().getResourceAsStream("/uv/lis/GUI/view/images/show-password-icon-eye-symbol" 
            + "-vision-hide-from-watch-icon-secret-view-web-design-element-vector1.png"));

        toggleButtonShowPassword.setGraphic(createIcon(eyeClosed));
    }

    @FXML
    private void togglePasswordVisibility() {
        if (toggleButtonShowPassword.isSelected()) {
            passwordFieldPassword.setVisible(false);
            textFieldPasswordVisible.setVisible(true);
            toggleButtonShowPassword.setGraphic(createIcon(eyeOpen));
        } else {
            textFieldPasswordVisible.setVisible(false);
            passwordFieldPassword.setVisible(true);
            toggleButtonShowPassword.setGraphic(createIcon(eyeClosed));
        }
    }

    private ImageView createIcon(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20.0);
        imageView.setFitWidth(20.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    @FXML
    public void handleLogin() {
        String email = textFieldEmail.getText().trim();
        String password = passwordFieldPassword.getText();

        if (validateInputs(email, password)) {
            try {
                Optional<User> optionalUser = userDAO.authenticate(email, password);

                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                    loadSessionByRole(user);
                    navigateToMenus(user.getRoleId());
                } else {
                    showError("Credenciales inválidas. Intente de nuevo.");
                }

            } catch (AuthenticateException e) {
                LOGGER.log(Level.WARNING, "Error al autenticar al usuario: {0}", e.getMessage());
                showError(e.getMessage());
            }
        }
    }

    private boolean validateInputs(String email, String password) {
        Optional<String> emailError = InputValidator.validateEmail(email, "El correo electrónico");
        Optional<String> passwordError = InputValidator.validatePassword(password, "La contraseña");

        Optional<String> validationError = emailError.or(() -> passwordError);

        validationError.ifPresent(this::showError);

        return validationError.isEmpty();
    }

    private void loadSessionByRole(User user) {
        int roleId = user.getRoleId();
        int userId = user.getId();
            try {
                switch (roleId) {
                    case USER_TYPE_STUDENT -> {
                        Optional<Student> validateStudent = new StudentDAO().getStudentById(userId);
                        if (validateStudent.isPresent()) {
                            SessionManager.getInstance().setCurrentStudent(validateStudent.get());
                        } else {
                            showError("No se encontraron los datos del estudiante en la base de datos.");
                        }
                    }
                    case USER_TYPE_PROFESSOR -> {
                        Optional<Professor> validateProfessor = new ProfessorDAO().getProfessorById(userId);
                        if (validateProfessor.isPresent()) {
                            SessionManager.getInstance().setCurrentProfessor(validateProfessor.get());
                        } else {
                            showError("No se encontraron los datos del profesor en la base de datos.");
                        }
                    }
                    case USER_TYPE_COORDINATOR -> {
                        Optional<Professor> validateCoordinator = new ProfessorDAO().getProfessorById(userId);
                        if (validateCoordinator.isPresent()) {
                            SessionManager.getInstance().setCurrentCoordinator(validateCoordinator.get());
                        } else {
                            showError("No se encontraron los datos del coordinador en la base de datos.");
                        }
                    }
                    case USER_TYPE_ADMINISTRATOR -> {
                        LOGGER.log(Level.INFO, "Inició sesión el administrador");
                    }
                    default -> LOGGER.log(Level.WARNING, "Rol desconocido: {0}", roleId);
                }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los datos del usuario para la sesión", e);
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        labelError.setText(message);
        labelError.setStyle("-fx-text-fill: red;");
    }

    private void navigateToMenus(int userRoleId) {
        String fxml = null;

        switch (userRoleId) {
            case USER_TYPE_STUDENT:
                fxml = "/uv/lis/GUI/view/FXMLStudentMenu.fxml";
                break;
            case USER_TYPE_PROFESSOR:
                fxml = "/uv/lis/GUI/view/FXMLProfessorMenu.fxml";
                break;
            case USER_TYPE_COORDINATOR:
                fxml = "/uv/lis/GUI/view/FXMLCoordinatorMenu.fxml";
                break;
            case USER_TYPE_ADMINISTRATOR:
                fxml = "/uv/lis/GUI/view/FXMLAdministratorMenu.fxml";
                break;
            default:
                showError("Usuario no reconocido");
                break;
        }

        if (fxml != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                Stage stage = (Stage) buttonLogin.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error al cargar la pantalla: " + fxml, e);
                showError("Error al cargar la pantalla.");
            }
        } 

        
    }
}