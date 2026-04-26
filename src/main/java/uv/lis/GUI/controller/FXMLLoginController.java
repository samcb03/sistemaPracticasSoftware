package uv.lis.GUI.controller;


import java.io.IOException;
import java.net.URL;
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

public class FXMLLoginController implements Initializable{


    @FXML private Button bLogin;
    @FXML private TextField txtIdentification;
    @FXML private PasswordField fieldPassword;
    @FXML private Label labelError;

    private UserDAO userDAO;
    private User user;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.userDAO = new UserDAO();
    }

    @FXML 
    public void handleLogin() {
        String identification = txtIdentification.getText().trim();
        String password = fieldPassword.getText();

        if (identification.isEmpty()) {
            showError("La identificación no puede estar vacía");
        } else if(password.isEmpty()) {
            showError("La contraseña no puede estar vacía");
        } else {
            try {
                user = userDAO.authenticate(identification, password);
                navigateTO(user.getUserType());
            } catch (AuthenticateException e) {
                showError(e.getMessage());
            }
        }
    }

    private void showError(String message) {
        labelError.setText(message);
        labelError.setStyle("-fx-text-fill: red;");
    }

    private void navigateTO(String userType) {
        String FXML = null;

        switch (userType) {
            case "Estudiante":
                FXML = "/uv/lis/GUI/view/FXMLStudentMenu.fxml";
                break;
            case "Profesor":
                FXML = "/uv/lis/GUI/view/FXMLProffesorMenu.fxml";
                break;
            case "Coordinador":
                FXML = "/uv/lis/GUI/view/FXMLCoordinatorMenu.fxml";
                break;
            case "Administrador":
                FXML = "/uv/lis/GUI/view/FXMLAdministratorMenu.fxml";
                break;
            default:
                showError("Usuario no reconocido");
                break;
        }
        if(FXML != null){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML));
            Parent root = loader.load();
            Stage stage = (Stage) bLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error al cargar la pantalla.");
        }
        }
    }
}

