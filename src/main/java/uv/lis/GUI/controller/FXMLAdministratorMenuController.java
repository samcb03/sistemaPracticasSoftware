package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import uv.lis.GUI.WindowHandler;

public class FXMLAdministratorMenuController extends WindowHandler {

    private static final String REGISTER_PROFESSOR_VIEW = "/uv/lis/GUI/view/FXMLRegisterProfessor.fxml";
    private static final String CONSULT_PROFESSOR_VIEW = "/uv/lis/GUI/view/FXMLManageProfessor.fxml";
    private static final String REGISTER_SCHOOL_PERIOD_VIEW = "/uv/lis/GUI/view/FXMLRegisterSchoolPeriod.fxml";

    @FXML private Button buttonRegisterProfessor;
    @FXML private Button buttonConsultProfessor;
    @FXML private Button buttonRegisterSchoolPeriod;
    @FXML private Button buttonLogOut;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*There is no data to initialize */
    }

    @FXML
    public void goToRegisterProfessor() {
        navigateTo(REGISTER_PROFESSOR_VIEW);
    }

    @FXML
    public void goToConsultProfessor() {
        navigateTo(CONSULT_PROFESSOR_VIEW);
    }

    @FXML
    public void goToRegisterSchoolPeriod() {
        navigateTo(REGISTER_SCHOOL_PERIOD_VIEW);
    }
}