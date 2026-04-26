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
import uv.lis.logic.dto.User;

public class FXMLAdministratorMenuController implements Initializable {

    @FXML private Button buttonRegisterProfessor;
    @FXML private Button buttonModifyProfessor;
    @FXML private Button buttonInactivateProfessor;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void goToRegisterProfessor() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProfessor.fxml");
    }

    @FXML
    public void goToModifyProfessor() {
        navigateTo("/uv/lis/GUI/view/FXMLModifyProfessor.fxml");
    }

    @FXML
    public void goToInactivateProfessor() {
        navigateTo("/uv/lis/GUI/view/FXMLInactivateProfessor.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}