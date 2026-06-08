package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;

public class FXMLStudentManagementController extends WindowHandler {
    @FXML private Button buttonRegisterStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML 
    public void goToRegisterStudent() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterStudent.fxml"); 
    }

    @FXML
    public void goToConsultStudent() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultStudent.fxml"); 
    }

}
