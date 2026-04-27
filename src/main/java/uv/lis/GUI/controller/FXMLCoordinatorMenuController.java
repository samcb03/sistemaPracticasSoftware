package uv.lis.GUI.controller;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import uv.lis.logic.dto.User;


public class FXMLCoordinatorMenuController implements Initializable{

    @FXML private Button buttonRegisterStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonRegisterOrganization;
    @FXML private Button buttonConsultOrganization;
    @FXML private Button buttonRegisterSupervisor;
    @FXML private Button buttonConsultSupervisor;
    @FXML private Button buttonRegisterProject;
    @FXML private Button buttonConsultProject;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void goToRegisterStudent() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterStudent.fxml");
    }

    @FXML
    public void goToConsultStudent() {
        navigateTo("/uv/lis/GUI/view/FXMLConsultStudent.fxml");
    }

    @FXML
    public void goToRegisterOrganization() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterAffiliatedOrganization.fxml");
    }

    @FXML
    public void goToConsultOrganization() {
        navigateTo("/uv/lis/GUI/view/FXMLConsultAffiliedOrganization.fxml");
    }

    @FXML
    public void goToRegisterSupervisor() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterSupervisor.fxml");
    }

    @FXML
    public void goToConsultSupervisor() {
        navigateTo("/uv/lis/GUI/view/FXMLConsultSupervisor.fxml");
    }

    @FXML
    public void goToRegisterProject() {
        navigateTo("/uv/lis/GUI/view/FXMLRegisterProject.fxml");
    }

    @FXML
    public void goToConsultProject() {
        navigateTo("/uv/lis/GUI/view/FXMLConsultSProject.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
