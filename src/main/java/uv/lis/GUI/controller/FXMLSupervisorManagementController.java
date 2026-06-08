package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;

public class FXMLSupervisorManagementController extends WindowHandler{
    @FXML private Button buttonRegisterProjectSupervisor;
    @FXML private Button buttonConsultSupervisor;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML 
    public void goToRegisterProjectSupervisor() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProjectSupervisor.fxml"); 
    }

    @FXML 
    public void goToConsultSupervisor() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultProjectSupervisor.fxml"); 
    }

}
