package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLCoordinatorMenuController extends WindowHandler{

    @FXML private Button buttonStudentManagement;
    @FXML private Button buttonProjectManagement;
    @FXML private Button buttonSupervisorManagement;
    @FXML private Button buttonOrganizationtManagement;
    @FXML private Button buttonSubjectManagement;
    @FXML private Button buttonLogOut;

    @FXML 
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML 
    public void goToStudentManagement() {
        navigateTo("/uv/lis/GUI/view/FXMLStudentManagement.fxml");
    }

    @FXML 
    public void goToProjectManagement() {
        navigateTo("/uv/lis/GUI/view/FXMLProjectManagement.fxml");
    }

    @FXML 
    public void goToSupervisorManagement() {
        navigateTo("/uv/lis/GUI/view/FXMLSupervisorManagement.fxml");
    }

    @FXML 
    public void goToOrganizationManagement() {
        navigateTo("/uv/lis/GUI/view/FXMLOrganizationManagement.fxml");
    }

    @FXML 
    public void goToSubjectManagement() {
        navigateTo("/uv/lis/GUI/view/FXMLSubjectManagement.fxml");
    }

}
