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
import static uv.lis.logic.utils.InputValidator.MAX_TEXT_LENGTH;
import static uv.lis.logic.utils.InputValidator.MIN_POSITIVE_INTEGER;
import static uv.lis.logic.utils.InputValidator.LETTERS_ONLY_REGEX;
import static uv.lis.logic.utils.InputValidator.EMAIL_REGEX;
import static uv.lis.logic.utils.InputValidator.PHONE_REGEX;


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
            validatePositiveInteger(txtNumberOfDirectUsers.getText().trim(), 
                "El número de usuarios directos"),
            validatePositiveInteger(txtNumberOfIndirectUsers.getText().trim(), 
                "El número de usuarios indirectos")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateName(String value, String fieldName) {
        Optional<String> message;
        if (value.isEmpty() || value.length() > MAX_TEXT_LENGTH) {
            message = Optional.of(fieldName + " no puede estar vacío o tener más de "
                + MAX_TEXT_LENGTH + " caracteres");
        } else if (!value.matches(LETTERS_ONLY_REGEX)) {
            message = Optional.of(fieldName + " solo acepta letras");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validateRequired(String value, String fieldName) {
        Optional<String> message;
        if (value.isEmpty()) {
            message = Optional.of(fieldName + " es obligatorio");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validateEmail(String value) {
        Optional<String> message;
        if (value.isEmpty()) {
            message = Optional.of("El correo electrónico no puede estar vacío");
        } else if (!value.matches(EMAIL_REGEX)) {
            message = Optional.of("El correo electrónico no tiene un formato válido");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validatePhoneNumber(String value) {
        Optional<String> message;
        if (value.isEmpty()) {
            message = Optional.of("El número de teléfono no puede estar vacío");
        } else if (!value.matches(PHONE_REGEX)) {
            message = Optional.of("El número de teléfono solo acepta entre 7 y 15 dígitos");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validatePositiveInteger(String value, String fieldName) {
        Optional<String> message = Optional.empty();
        if (value.isEmpty()) {
            message = Optional.of(fieldName + " no puede estar vacío");
        }
        try {
            int number = Integer.parseInt(value);
            if (number < MIN_POSITIVE_INTEGER) {
                message = Optional.of(fieldName + " debe ser un número positivo");
            }
        } catch (NumberFormatException e) {
            message = Optional.of(fieldName + " debe ser un número entero válido");
        }
        return message;
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