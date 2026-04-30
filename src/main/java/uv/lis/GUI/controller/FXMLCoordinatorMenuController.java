package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.MenuHandler;


public class FXMLCoordinatorMenuController extends MenuHandler  {

    @FXML private Button buttonRegisterStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonRegisterOrganization;
    @FXML private Button buttonConsultOrganization;
    @FXML private Button buttonRegisterSupervisor;
    @FXML private Button buttonConsultSupervisor;
    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonConsultProject;
    @FXML private Button buttonLogOut;


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

    @FXML public void goToRegisterSupervisor() { 
        navigateTo("/uv/lis/GUI/view/FXMLRegisterSupervisor.fxml"); 
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

}