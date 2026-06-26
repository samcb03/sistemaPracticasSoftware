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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import uv.lis.logic.common.EmailCommon;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;
import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.EmailException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.SessionManager;

public class FXMLLoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FXMLLoginController.class.getName());
    private static final int USER_TYPE_STUDENT = 1;
    private static final int USER_TYPE_PROFESSOR = 2;
    private static final int USER_TYPE_COORDINATOR = 3;
    private static final int USER_TYPE_ADMINISTRATOR = 4;
    private static final int MAX_ATTEMPTS = 5;
    private static final int IMAGE_FIT_HEIGHT = 20;
    private static final int AUTOEVALUATION_DOCUMENT_TYPE = 1;
    private static final int EVALUATION_DOCUMENT_TYPE = 12;
    private static final int LIBERATION_LETTER_DOCUMENT_TYPE = 13;

    private static final String FINISH_WINDOW_VIEW = "/uv/lis/GUI/view/FXMLFinishWindow.fxml";
    private static final String MESSAGE_INVALID_CREDENTIALS = "Credenciales inválidas. Intentos restantes: ";
    private static final String MESSAGE_TOO_MANY_ATTEMPTS =  "Demasiados intentos fallidos. Reinicie la aplicación.";
    private static final String MESSAGE_USER_DATA_NOT_FOUND = "No se encontraron los datos.";
    private static final String VERIFY_VIEW_FXML = "/uv/lis/GUI/view/FXMLVerifyCode.fxml";
    private static final String EYE_OPEN_IMAGE = "/uv/lis/GUI/view/images/show-password-icon-eye-symbol" 
        + "-vision-hide-from-watch-icon-secret-view-web-design-element-vector2.png";
    private static final String EYE_CLOSED_IMAGE = "/uv/lis/GUI/view/images/show-password-icon-eye-symbol" 
        + "-vision-hide-from-watch-icon-secret-view-web-design-element-vector1.png";
    private static final String STUDENT_MENU_VIEW = "/uv/lis/GUI/view/FXMLStudentMenu.fxml";
    private static final String PROFESSOR_MENU_VIEW = "/uv/lis/GUI/view/FXMLProfessorMenu.fxml";
    private static final String COORDINATOR_MENU_VIEW = "/uv/lis/GUI/view/FXMLCoordinatorMenu.fxml";
    private static final String ADMINISTRATOR_MENU_VIEW = "/uv/lis/GUI/view/FXMLAdministratorMenu.fxml";
    
    
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private TextField textFieldPasswordVisible;
    @FXML private ToggleButton toggleButtonShowPassword;
    @FXML private Label labelError;
    @FXML private Button buttonLogin;

    private UserDAO userDAO;
    private StudentDAO studentDAO;
    private ProfessorDAO professorDAO;
    private User user;
    private EmailCommon emailCommon;
    private FXMLVerifyCodeController verifyController;
    private Image eyeOpen;
    private Image eyeClosed;
    private int failedAttempts = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.userDAO = new UserDAO();
        this.studentDAO = new StudentDAO();
        this.professorDAO = new ProfessorDAO();
        this.emailCommon = new EmailCommon();
        textFieldPasswordVisible.textProperty().bindBidirectional(passwordFieldPassword.textProperty());
        eyeOpen = new Image(getClass().getResourceAsStream(EYE_OPEN_IMAGE));
        eyeClosed = new Image(getClass().getResourceAsStream(EYE_CLOSED_IMAGE));
        toggleButtonShowPassword.setGraphic(createIcon(eyeClosed));
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
                    startUserSession(user);
                } else {
                    registerFailedAttempt();
                }
            } catch (AuthenticateException e) {
                LOGGER.log(Level.WARNING, "Error al autenticar al usuario: {0}", e.getMessage());
                showError(e.getMessage());
            }
        }
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
        imageView.setFitHeight(IMAGE_FIT_HEIGHT);
        imageView.setFitWidth(IMAGE_FIT_HEIGHT);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        return imageView;
    }
    
    private void registerFailedAttempt() {
        failedAttempts++;

        if (failedAttempts >= MAX_ATTEMPTS) {
            blockLogin();
        } else {
            int remainingAttempts = MAX_ATTEMPTS - failedAttempts;
            showError(MESSAGE_INVALID_CREDENTIALS + remainingAttempts);
        }
    }

    private void blockLogin() {
        buttonLogin.setDisable(true);
        showError(MESSAGE_TOO_MANY_ATTEMPTS);
    }

    private void startUserSession(User user) {
        if (user.isEmailAuthenticationActive()) {
            requestEmailVerification(user);
        } else {
            completeLogin(user);
        }
    }

   private boolean completeLogin(User user) {
        boolean loaded = loadSessionByRole(user);

        if (loaded) {
            navigateToMenus(user.getRoleId());
        }
        return loaded;
    }

    private void requestEmailVerification(User user) {
        try {
            VerificationChallenge challenge = emailCommon.sendVerificationCode(user.getEmail());
            openVerificationWindow(user, challenge);
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Error al enviar el código de verificación", e);
            showError(e.getMessage());
        }
    }

    private void openVerificationWindow(User user, VerificationChallenge challenge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(VERIFY_VIEW_FXML));
            Parent root = loader.load();
            verifyController = loader.getController();
            verifyController.initializeData(user, challenge);
            Stage verifyStage = new Stage();
            verifyStage.setScene(new Scene(root));
            verifyStage.initModality(Modality.APPLICATION_MODAL);
            verifyStage.setOnHidden(this::onVerificationClosed);
            verifyStage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar la pantalla de verificación", e);
            showError("No se pudo abrir la verificación. Intente más tarde");
        }
    }

    private void onVerificationClosed(WindowEvent event) {
        if (verifyController.isVerified()) {
            completeLogin(user);
        }
    }

    private boolean validateInputs(String email, String password) {
        boolean isEmpty = false;
        Optional<String> emailError = InputValidator.validateEmail(email, "El correo electrónico");
        Optional<String> passwordError = InputValidator.validatePassword(password, "La contraseña");
        Optional<String> validationError = emailError.or(() -> passwordError);
        validationError.ifPresent(this::showError);
        isEmpty = validationError.isEmpty();
        return isEmpty;
    }

    private boolean loadSessionByRole(User user) {
        boolean isLoaded = false;
        int roleId = user.getRoleId();
        int userId = user.getId();

        try {
            switch (roleId) {
                case USER_TYPE_STUDENT:
                    isLoaded = loadStudentSession(userId);
                    break;
                case USER_TYPE_PROFESSOR:
                    isLoaded = loadProfessorSession(userId);
                    break;
                case USER_TYPE_COORDINATOR:
                    isLoaded = loadCoordinatorSession(userId);
                    break;
                case USER_TYPE_ADMINISTRATOR:
                    LOGGER.log(Level.INFO, "Inició sesión el administrador");
                    isLoaded = true;
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Rol desconocido: {0}", roleId);
                    break;
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los datos del usuario para la sesión", e);
            showError(e.getMessage());
        }
        return isLoaded;
    }

    private boolean loadStudentSession(int userId) throws OperationException {
        boolean isLoaded = false;
        Optional<Student> validateStudent = studentDAO.getStudentById(userId);

        if (validateStudent.isPresent()) {
            SessionManager.getInstance().setCurrentStudent(validateStudent.get());
            isLoaded = true;
        } else {
            showError(MESSAGE_USER_DATA_NOT_FOUND);
        }

        return isLoaded;
    }

    private boolean loadProfessorSession(int userId) throws OperationException {
        boolean isLoaded = false;
        Optional<Professor> validateProfessor = professorDAO.getProfessorById(userId);

        if (validateProfessor.isPresent()) {
            SessionManager.getInstance().setCurrentProfessor(validateProfessor.get());
            isLoaded = true;
        } else {
            showError(MESSAGE_USER_DATA_NOT_FOUND);
        }

        return isLoaded;
    }

    private boolean loadCoordinatorSession(int userId) throws OperationException {
        boolean isLoaded = false;
        Optional<Professor> validateCoordinator = professorDAO.getProfessorById(userId);

        if (validateCoordinator.isPresent()) {
            SessionManager.getInstance().setCurrentCoordinator(validateCoordinator.get());
            isLoaded = true;
        } else {
            showError(MESSAGE_USER_DATA_NOT_FOUND);
        }

        return isLoaded;
    }

    private void showError(String message) {
        labelError.setText(message);
        labelError.setStyle("-fx-text-fill: red;");
    }

    private void navigateToMenus(int userRoleId) {
        String fxml = null;

        switch (userRoleId) {
            case USER_TYPE_STUDENT:
                Student currentStudent = SessionManager.getInstance().getCurrentStudent();
                fxml = shouldGoToFinishWindow(currentStudent) ? FINISH_WINDOW_VIEW : STUDENT_MENU_VIEW;
                break;
            case USER_TYPE_PROFESSOR:
                fxml = PROFESSOR_MENU_VIEW;
                break;
            case USER_TYPE_COORDINATOR:
                fxml = COORDINATOR_MENU_VIEW;
                break;
            case USER_TYPE_ADMINISTRATOR:
                fxml = ADMINISTRATOR_MENU_VIEW;
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
                LOGGER.log(Level.SEVERE, "Error al cargar la pantalla", e);
                showError("Error al cargar la pantalla.");
            }
        } 
    }

    private boolean shouldGoToFinishWindow(Student student) {
        boolean hasToGo = false;
        try {
            ExpedientDAO expedientDAO = new ExpedientDAO();
            boolean isAutoevaluationValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(), AUTOEVALUATION_DOCUMENT_TYPE);
            boolean isOrganizationEvaluationValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(), EVALUATION_DOCUMENT_TYPE);
            boolean isLiberationLetterValidated = expedientDAO.isDocumentTypeValidated(
                student.getIdStudent(), LIBERATION_LETTER_DOCUMENT_TYPE);
            hasToGo = isAutoevaluationValidated && isOrganizationEvaluationValidated && isLiberationLetterValidated;
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar documentos finales", e);
        }
        return hasToGo;
    }
}