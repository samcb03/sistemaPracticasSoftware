package uv.lis.GUI.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.MenuHandler;


public class FXMLProfessorMenuController extends MenuHandler  {

    @FXML Button buttonRegisterNotifications;
    @FXML Button buttonEvaluateReports; 
    @FXML Button buttonLogOut;


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
    }

    @FXML
    public void goToRegisterNotifications() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterNotifications.fxml");
    }

    @FXML
    public void goToEvaluateReports() {
        navigateTo("/uv/lis/GUI/view/FXMLEvaluateReports.fxml");
    }

}
