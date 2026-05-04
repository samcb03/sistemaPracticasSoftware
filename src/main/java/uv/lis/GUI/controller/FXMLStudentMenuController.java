package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.MenuHandler;


public class FXMLStudentMenuController extends MenuHandler {

    @FXML private Button buttonRequestProject;
    @FXML private Button buttonNotifications;
    @FXML private Button buttonLogOut;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void goToRequestProject() {
        navigateTo("/uv/lis/GUI/view/FXMLRequestProject.fxml");
    }

    @FXML
    public void goToNotifications() {
        navigateTo("/uv/lis/GUI/view/FXMLNotifications.fxml");
    }

}