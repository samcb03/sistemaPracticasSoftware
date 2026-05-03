package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;


public class FXMLAdministratorMenuController extends WindowHandler  {

    @FXML private Button buttonRegisterProfessor;
    @FXML private Button buttonConsultProfessor;
    @FXML private Button buttonLogOut;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

   @FXML
    public void goToRegisterProfessor() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProfessor.fxml");
    }

    @FXML
    public void goToConsultProfessor() {
        navigateTo("/uv/lis/GUI/view/FXMLConsultProfessor.fxml");
    }

}