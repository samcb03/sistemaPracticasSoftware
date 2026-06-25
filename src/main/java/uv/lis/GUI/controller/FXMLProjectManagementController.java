package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLProjectManagementController extends WindowHandler {

    private static final String REGISTER_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLRegisterProject.fxml";
    private static final String CONSULT_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLConsultProject.fxml";
    private static final String ASSIGNATION_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLAssignationProject.fxml";

    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonConsultProject;
    @FXML private Button buttonAssignationProject;
    @FXML private Button buttonBack;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* No initialization required for this menu controller */
    }

    @FXML public void goToRegisterProject() { 
        navigateTo(REGISTER_PROJECT_VIEW); 
    }

    @FXML public void goToConsultProject() { 
        navigateTo(CONSULT_PROJECT_VIEW); 
    }

    @FXML public void goToAssignationProject() {
        navigateTo(ASSIGNATION_PROJECT_VIEW);
    }
}
