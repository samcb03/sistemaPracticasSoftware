package uv.lis.GUI.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import uv.lis.GUI.ValidationAbstract;
import uv.lis.logic.dto.User;

public class FXMLProfessorMenuController extends ValidationAbstract implements Initializable {

    @FXML Button buttonRegisterNotifications;
    @FXML Button buttonEvaluateReports; 
    @FXML Button buttonLogOut;

    private User user;


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
    }

    @FXML
    public void goToRegisterNotifications() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterNotifications.fxml");
    }

    @FXML
    public void goToEvaluateReports() {
        navigateTo("/uv/lis/GUI/view/FXMLEvaluateReports.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error al cargar la pantalla.");
        }
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
            showError("Error al cerrar sesión");
                
        }
}

    @Override
    protected void clearFields() {
    }
}
