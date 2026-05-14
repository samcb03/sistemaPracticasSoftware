package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;
import uv.lis.logic.dto.Student;
import uv.lis.logic.utils.SessionManager;


public class FXMLStudentMenuController extends WindowHandler {

    @FXML private Button buttonRequestProject;
    @FXML private Button buttonReports;
    @FXML private Button buttonUploadDocuments;
    @FXML private Button buttonLogOut;

    private Student student;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.student = SessionManager.getInstance().getCurrentStudent();
    }

    @FXML
    public void goToRequestProject() {
        navigateTo("/uv/lis/GUI/view/FXMLRequestProject.fxml");
    }

    @FXML
    public void goToReports() {
        navigateTo("/uv/lis/GUI/view/FXMLReports.fxml");
    }

    @FXML void goToUploadDocuments() {
        navigateTo("/uv/lis/GUI/view/FXMLUploadDocuments.fxml");
    }

}