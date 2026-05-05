package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.EMAIL_REGEX;
import java.io.IOException;
import java.net.URL;
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
import javafx.stage.Stage;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;


public class FXMLLoginController implements Initializable {

    private static final int USER_TYPE_STUDENT = 1;
    private static final int USER_TYPE_PROFESSOR = 2;
    private static final int USER_TYPE_COORDINATOR = 3;
    private static final int USER_TYPE_ADMINISTRATOR = 4;

    private static final Logger LOGGER = Logger.getLogger(FXMLLoginController.class.getName());
    
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private Label labelError;
    @FXML private Button buttonLogin;

    private UserDAO userDAO;
    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.userDAO = new UserDAO();
    }

    @FXML
    public void handleLogin() {
        String email = textFieldEmail.getText().trim();
        String password = passwordFieldPassword.getText();

        if (email.isEmpty()) {
            showError("El correo electrónico no puede estar vacío");
        }else if (!email.matches(EMAIL_REGEX)) {
            showError("El email debe tener un @ y un . ");
        } else if (password.isEmpty()) {
            showError("La contraseña no puede estar vacía");
        } else {
            try {
                user = userDAO.authenticate(email, password);

                if (user.getRoleId() == USER_TYPE_STUDENT) {
                    StudentDAO studentDAO = new StudentDAO();
                    Student student = studentDAO.getStudentById(user.getId());
                    SessionManager.getInstance().setCurrentStudent(student);
               }
                navigateToMenus(user.getRoleId());
            } catch (AuthenticateException e) {
                showError(e.getMessage());
            }  catch (OperationException e) {
                showError(e.getMessage());
            }
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