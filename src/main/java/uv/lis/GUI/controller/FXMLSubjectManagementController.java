package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLSubjectManagementController extends WindowHandler {

    private static final String REGISTER_SUBJECT_VIEW = "/uv/lis/GUI/view/FXMLRegisterSubject.fxml";
    private static final String ASSIGNATION_STUDENT_VIEW = "/uv/lis/GUI/view/FXMLAssignStudentSubject.fxml";
    private static final String PROFESSOR_MENU_VIEW = "/uv/lis/GUI/view/FXMLProfessorMenu.fxml";

    @FXML private Button buttonRegisterSubject;
    @FXML private Button buttonAssignStudentSubject;
    @FXML private Button buttonConsultSubject;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* No initialization required for this menu controller */
    }

    @FXML 
    public void goToRegisterSubject() { 
        navigateTo(REGISTER_SUBJECT_VIEW); 
    }

    @FXML 
    public void goToAssignationStudentSubject() {
        navigateTo(ASSIGNATION_STUDENT_VIEW);
    }

    @FXML 
    public void goToProfessorMenu() {
        navigateTo(PROFESSOR_MENU_VIEW);
    }
}
