package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;

public class FXMLProjectManagementController extends WindowHandler {
    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonConsultProject;
    @FXML private Button buttonAssignationProject;
    @FXML private Button buttonBack;
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML public void goToRegisterProject() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProject.fxml"); 
    }

    @FXML public void goToConsultProject() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultProject.fxml"); 
    }

    @FXML public void goToAssignationProject() {
        navigateTo("/uv/lis/GUI/view/FXMLAssignationProject.fxml");
    }
}
