package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLSupervisorManagementController extends WindowHandler {

    private static final String REGISTER_SUPERVISOR_VIEW = "/uv/lis/GUI/view/FXMLRegisterProjectSupervisor.fxml";
    private static final String CONSULT_SUPERVISOR_VIEW = "/uv/lis/GUI/view/FXMLManageProjectSupervisor.fxml";

    @FXML private Button buttonRegisterProjectSupervisor;
    @FXML private Button buttonConsultSupervisor;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* No initialization required for this menu controller */
    }

    @FXML 
    public void goToRegisterProjectSupervisor() { 
        navigateTo(REGISTER_SUPERVISOR_VIEW); 
    }

    @FXML 
    public void goToConsultSupervisor() { 
        navigateTo(CONSULT_SUPERVISOR_VIEW); 
    }

}
