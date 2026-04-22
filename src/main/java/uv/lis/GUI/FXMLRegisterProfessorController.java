package uv.lis.GUI;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.UserDAO;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FXMLRegisterProfessorController implements Initializable {

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPersonnelNumber;

    @FXML private ComboBox<String> comboBoxCoordinator;
    @FXML private Label labelError;

    private UserDAO userDAO;
    private ProfessorDAO professorDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        professorDAO = new ProfessorDAO();

        comboBoxCoordinator.setItems(
                FXCollections.observableArrayList("Si", "No")
        );
    }

    @FXML
    public void validateFields() {

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String password = txtPassword.getText().trim();
        String personnelNumber = txtPersonnelNumber.getText().trim();

        if (firstName.isEmpty() || firstName.length() > 50) {
            showError("El nombre no puede estar vacío o tener más de 50 caracteres");
            return;
        }

        if (!firstName.matches("[\\p{L}\\s]+")) {
            showError("El nombre solo acepta letras");
            return;
        }

        if (lastName.isEmpty() || lastName.length() > 50) {
            showError("Los apellidos no pueden estar vacíos o tener más de 50 caracteres");
            return;
        }

        if (!lastName.matches("[\\p{L}\\s]+")) {
            showError("Los apellidos solo aceptan letras");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (personnelNumber.isEmpty()) {
            showError("El número de personal es obligatorio");
            return;
        }

        if (comboBoxCoordinator.getValue() == null) {
            showError("Seleccione si es coordinador o no");
            return;
        }

        labelError.setText("");
        registerProfessor();
    }

    private void registerProfessor() {

        Professor professor = buildProfessor();

        try {
            int generatedId = userDAO.registerUser(professor);

            if (generatedId != -1) {
                professor.setId(generatedId);

                boolean result = professorDAO.registerProfessor(professor);

                if (result) {
                    showSuccess("Profesor registrado correctamente");
                    clearFields();
                } else {
                    showError("Error al registrar profesor");
                }
            } else {
                showError("Error registrando usuario");
            }

        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private Professor buildProfessor() {

        Professor professor = new Professor();

        professor.setFirstName(txtFirstName.getText().trim());
        professor.setLastName(txtLastName.getText().trim());
        professor.setPassword(txtPassword.getText().trim());

        professor.setPersonnelNumber(txtPersonnelNumber.getText().trim());

        boolean isCoordinator =
                comboBoxCoordinator.getValue().equals("Si");

        professor.setIsCoordinator(isCoordinator);

        professor.setUserType(isCoordinator ? "Coordinador" : "Maestro");
        professor.setInactive(false);

        return professor;
    }

    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtPassword.clear();
        txtPersonnelNumber.clear();
        comboBoxCoordinator.setValue(null);
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