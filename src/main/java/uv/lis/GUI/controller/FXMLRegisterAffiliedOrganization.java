package uv.lis.GUI.controller;

import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLRegisterAffiliedOrganization implements Initializable {

    private static final int MAX_NAME_LENGTH = 50;
    private static final String LETTERS_ONLY_REGEX = "[\\p{L}\\s]+";
    private static final String EMAIL_REGEX = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_REGEX = "^[0-9]{7,15}$";

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
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
    }

    @FXML
    public void validateFields() {
        Optional<String> error = getFirstValidationError();
        if (error.isPresent()) {
            showError(error.get());
        } else {
            labelError.setText("");
            registerAffiliatedOrganization();
        }
    }

    private Optional<String> getFirstValidationError() {
        return Stream.of(
            validateName(txtName.getText().trim(), "El nombre"),
            validateName(txtCity.getText().trim(), "La ciudad"),
            validateName(txtState.getText().trim(), "El estado"),
            validateRequired(txtSector.getText().trim(), "El sector"),
            validateEmail(txtEmail.getText().trim()),
            validatePhoneNumber(txtPhoneNumber.getText().trim()),
            validatePositiveInteger(txtNumberOfDirectUsers.getText().trim(), "El número de usuarios directos"),
            validatePositiveInteger(txtNumberOfIndirectUsers.getText().trim(), "El número de usuarios indirectos")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateName(String value, String fieldName) {
        if (value.isEmpty() || value.length() > MAX_NAME_LENGTH) {
            return Optional.of(fieldName + " no puede estar vacío o tener más de "
                + MAX_NAME_LENGTH + " caracteres");
        } else if (!value.matches(LETTERS_ONLY_REGEX)) {
            return Optional.of(fieldName + " solo acepta letras");
        }
        return Optional.empty();
    }

    private Optional<String> validateRequired(String value, String fieldName) {
        if (value.isEmpty()) {
            return Optional.of(fieldName + " es obligatorio");
        }
        return Optional.empty();
    }

    private Optional<String> validateEmail(String value) {
        if (value.isEmpty()) {
            return Optional.of("El correo electrónico no puede estar vacío");
        } else if (!value.matches(EMAIL_REGEX)) {
            return Optional.of("El correo electrónico no tiene un formato válido");
        }
        return Optional.empty();
    }

    private Optional<String> validatePhoneNumber(String value) {
        if (value.isEmpty()) {
            return Optional.of("El número de teléfono no puede estar vacío");
        } else if (!value.matches(PHONE_REGEX)) {
            return Optional.of("El número de teléfono solo acepta entre 7 y 15 dígitos");
        }
        return Optional.empty();
    }

    private Optional<String> validatePositiveInteger(String value, String fieldName) {
        if (value.isEmpty()) {
            return Optional.of(fieldName + " no puede estar vacío");
        }
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                return Optional.of(fieldName + " debe ser un número positivo");
            }
        } catch (NumberFormatException e) {
            return Optional.of(fieldName + " debe ser un número entero válido");
        }
        return Optional.empty();
    }

    private void registerAffiliatedOrganization() {
        AffiliatedOrganization organization = buildOrganization();
        try {
            boolean result = affiliatedOrganizationDAO.registerOrganization(organization);
            if (result) {
                showSuccess("Organización registrada correctamente");
                clearFields();
            } else {
                showError("Error al registrar la organización");
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