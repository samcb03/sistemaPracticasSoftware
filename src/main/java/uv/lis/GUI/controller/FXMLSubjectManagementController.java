package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;

public class FXMLSubjectManagementController extends WindowHandler{
        @FXML private Button buttonRegisterSubject;
        @FXML private Button buttonAssignStudentSubject;
        @FXML private Button buttonBack;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
        }

        @FXML public void goToRegisterSubject() { 
            navigateTo("/uv/lis/GUI/view/FXMLRegisterSubject.fxml"); 
        }

        @FXML public void goToAssignationStudentSubject() {
        navigateTo("/uv/lis/GUI/view/FXMLAssignStudentSubject.fxml");
        }


}
