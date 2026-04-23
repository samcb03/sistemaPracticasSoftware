package uv.lis.GUI.controller;

import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.InputValidator;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLRegisterAffiliedOrganization implements Initializable {

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;

    @FXML private TextField txtName;
    @FXML private TextField txtCity;
    @FXML private TextField txtState;
    @FXML private TextField txtSector;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhoneNumber;
    @FXML private TextField txtNumberOfDirectUsers;
    @FXML private TextField txtNumberOfIndirectUsers;

    @FXML private Label labelError;

    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        affiliatedOrganizationDAO= new AffiliatedOrganizationDAO();

    }

    @FXML
    public void validateFields() {
        String error;

        error = InputValidator.validateName(txtName, "El nombre", 50);
        if (error != null) { showError(error); return; }

        error = InputValidator.validateName(txtCity, "La ciudad", 50);
        if (error != null) { showError(error); return; }

        error = InputValidator.validateName(txtState, "El estado", 50);
        if (error != null) { showError(error); return; }

        error = InputValidator.validateRequired(txtSector, "El sector");
        if (error != null) { showError(error); return; }

        error = InputValidator.validateEmail(txtEmail);
        if (error != null) { showError(error); return; }

        error = InputValidator.validatePhoneNumber(txtPhoneNumber);
        if (error != null) { showError(error); return; }

        error = InputValidator.validatePositiveInteger(txtNumberOfDirectUsers, "El número de usuarios directos");
        if (error != null) { showError(error); return; }

        error = InputValidator.validatePositiveInteger(txtNumberOfIndirectUsers, "El número de usuarios indirectos");
        if (error != null) { showError(error); return; }

        labelError.setText("");
        registerAffiliedOrganization();
    }


    private void registerAffiliedOrganization() {

        AffiliatedOrganization organization = buildOrganization();

        try {
            boolean result = affiliatedOrganizationDAO.registerOrganization(organization);
                if (result) {
                    showSuccess("Organizacion registrada correctamente");
                    clearFields();
                } else {
                    showError("Error al registrar Organizacion");
                }

        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private AffiliatedOrganization buildOrganization() {

        AffiliatedOrganization organization = new AffiliatedOrganization();

        organization.setName(txtName.getText().trim());
        organization.setCity(txtCity.getText().trim());
        organization.setState(txtState.getText().trim());
        organization.setSector(txtSector.getText().trim());
        organization.setEmail(txtEmail.getText().trim());
        organization.setPhoneNumber(txtPhoneNumber.getText().trim());
        organization.setNumberOfDirectUsers(Integer.parseInt(txtNumberOfDirectUsers.getText().trim()));
        organization.setNumberOfIndirectUsers(Integer.parseInt(txtNumberOfIndirectUsers.getText().trim()));

        return organization;
    }

    private void clearFields() {
        txtName.clear();
        txtCity.clear();
        txtState.clear();
        txtSector.clear();
        txtEmail.clear();
        txtPhoneNumber.clear();
        txtNumberOfDirectUsers.clear();
        txtNumberOfIndirectUsers.clear();
    }

    private void showError(String message) {
        labelError.setText(message);
        labelError.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        labelError.setText(message);
        labelError.setStyle("-fx-text-fill: green;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack() {
        Stage stage = (Stage) buttonBack.getScene().getWindow();
        stage.close();
    }
}

