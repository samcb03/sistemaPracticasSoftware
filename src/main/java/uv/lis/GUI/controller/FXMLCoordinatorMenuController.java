package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLCoordinatorMenuController extends WindowHandler{

    private static final String STUDENT_MANAGEMENT_VIEW = "/uv/lis/GUI/view/FXMLStudentManagement.fxml";
    private static final String PROJECT_MANAGEMENT_VIEW = "/uv/lis/GUI/view/FXMLProjectManagement.fxml";
    private static final String SUPERVISOR_MANAGEMENT_VIEW = "/uv/lis/GUI/view/FXMLSupervisorManagement.fxml";
    private static final String ORGANIZATION_MANAGEMENT_VIEW = "/uv/lis/GUI/view/FXMLOrganizationManagement.fxml";
    private static final String SUBJECT_MANAGEMENT_VIEW = "/uv/lis/GUI/view/FXMLSubjectManagement.fxml";

    @FXML private Button buttonStudentManagement;
    @FXML private Button buttonProjectManagement;
    @FXML private Button buttonSupervisorManagement;
    @FXML private Button buttonOrganizationtManagement;
    @FXML private Button buttonSubjectManagement;
    @FXML private Button buttonLogOut;

    @FXML 
    public void initialize(URL location, ResourceBundle resources) {
        /* There is no data to initialize */
    }

    @FXML 
    public void goToStudentManagement() {
        navigateTo(STUDENT_MANAGEMENT_VIEW);
    }

    @FXML 
    public void goToProjectManagement() {
        navigateTo(PROJECT_MANAGEMENT_VIEW);
    }

    @FXML 
    public void goToSupervisorManagement() {
        navigateTo(SUPERVISOR_MANAGEMENT_VIEW);
    }

    @FXML 
    public void goToOrganizationManagement() {
        navigateTo(ORGANIZATION_MANAGEMENT_VIEW);
    }

    @FXML 
    public void goToSubjectManagement() {
        navigateTo(SUBJECT_MANAGEMENT_VIEW);
    }
}