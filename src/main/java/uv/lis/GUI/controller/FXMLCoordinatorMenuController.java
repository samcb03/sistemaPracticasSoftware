package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;


public class FXMLCoordinatorMenuController extends WindowHandler  {

    @FXML private Button buttonRegisterStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonRegisterOrganization;
    @FXML private Button buttonConsultOrganization;
    @FXML private Button buttonRegisterProjectSupervisor;
    @FXML private Button buttonConsultSupervisor;
    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonConsultProject;
    @FXML private Button buttonLogOut;
    @FXML private Button buttonRegisterSubject;
    @FXML private Button buttonAssignationProject;
    @FXML private Button buttonAssignStudentSubject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML public void goToRegisterStudent() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterStudent.fxml"); 
    }

    @FXML public void goToConsultStudent() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultStudent.fxml"); 
    }

    @FXML public void goToRegisterOrganization() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterAffiliatedOrganization.fxml"); 
    }

    @FXML public void goToConsultOrganization() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultAffiliatedOrganization.fxml"); 
    }

    @FXML public void goToRegisterProjectSupervisor() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProjectSupervisor.fxml"); 
    }

    @FXML public void goToConsultSupervisor() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultSupervisor.fxml"); 
    }

    @FXML public void goToRegisterProject() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProject.fxml"); 
    }

    @FXML public void goToConsultProject() { 
        navigateTo("/uv/lis/GUI/view/FXMLConsultSProject.fxml"); 
    }

    @FXML public void goToRegisterSubject() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterSubject.fxml"); 
    }

    @FXML public void goToAssignationStudentSubject() {
        navigateTo("/uv/lis/GUI/view/FXMLAssignStudentSubject.fxml");
    }

    @FXML public void goToAssignationProject() {
        navigateTo("/uv/lis/GUI/view/FXMLAssignationProject.fxml");
    }

}