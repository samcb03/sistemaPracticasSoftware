package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLStudentManagementController extends WindowHandler {

    private static final String REGISTER_STUDENT_VIEW = "/uv/lis/GUI/view/FXMLRegisterStudent.fxml";
    private static final String CONSULT_STUDENT_VIEW = "/uv/lis/GUI/view/FXMLConsultStudent.fxml";

    @FXML private Button buttonRegisterStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonBack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* No initialization required for this menu controller */
    }

    @FXML 
    public void goToRegisterStudent() { 
        navigateTo(REGISTER_STUDENT_VIEW); 
    }

    @FXML
    public void goToConsultStudent() { 
        navigateTo(CONSULT_STUDENT_VIEW); 
    }

}
