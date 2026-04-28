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
import javafx.stage.Stage;
import uv.lis.GUI.MenuAbstract;
import uv.lis.logic.dto.User;

public class FXMLStudentMenuController extends MenuAbstract implements Initializable {

    @FXML private Button buttonSolicityProject;
    @FXML private Button buttonNotifications;
    @FXML private Button buttonLogOut;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void goToSolicityProject() {
        navigateTo("/uv/lis/GUI/view/FXMLSolicityProject.fxml");
    }

    @FXML
    public void goToNotifications() {
        navigateTo("/uv/lis/GUI/view/FXMLNotifications.fxml");
    }

    @FXML
    public void logOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/uv/lis/GUI/view/FXMLLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonLogOut.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error al cerrar sesión.");
        }
    }
}