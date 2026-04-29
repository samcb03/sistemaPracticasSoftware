package uv.lis.GUI.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.MenuAbstract;

public class FXMLProfessorMenuController extends MenuAbstract  {

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


    @FXML
    public void logOut(Button buttonLogOut) {
        navigateToLogOut(buttonLogOut);
    }

}
