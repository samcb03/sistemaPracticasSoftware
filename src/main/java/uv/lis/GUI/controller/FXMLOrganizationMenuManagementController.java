package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLOrganizationMenuManagementController extends WindowHandler{
    @FXML private Button buttonRegisterOrganization;
    @FXML private Button buttonConsultOrganization;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML 
    public void goToRegisterOrganization() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterAffiliatedOrganization.fxml"); 
    }

    @FXML 
    public void goToConsultOrganization() { 
        navigateTo("/uv/lis/GUI/view/FXMLManageAffiliatedOrganization.fxml"); 
    }
}