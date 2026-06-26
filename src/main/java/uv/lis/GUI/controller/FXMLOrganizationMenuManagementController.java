package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLOrganizationMenuManagementController extends WindowHandler {

    private static final String REGISTER_ORGANIZATION_VIEW = 
    "/uv/lis/GUI/view/FXMLRegisterAffiliatedOrganization.fxml";
    private static final String CONSULT_ORGANIZATION_VIEW = 
    "/uv/lis/GUI/view/FXMLManageAffiliatedOrganization.fxml";

    @FXML private Button buttonRegisterOrganization;
    @FXML private Button buttonConsultOrganization;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* There is no data to initialize */
    }

    @FXML 
    public void goToRegisterOrganization() { 
        navigateTo(REGISTER_ORGANIZATION_VIEW); 
    }

    @FXML 
    public void goToConsultOrganization() { 
        navigateTo(CONSULT_ORGANIZATION_VIEW); 
    }
}