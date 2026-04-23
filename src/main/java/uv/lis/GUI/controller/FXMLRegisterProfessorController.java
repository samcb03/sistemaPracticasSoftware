package uv.lis.GUI.controller;


import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.UserDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class FXMLRegisterProfessorController implements Initializable {

    private static final int MAX_FIRST_NAME_LENGTH = 50;
    private static final int MAX_LAST_NAME_LENGHT = 150;
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";
    private static final String LETTERS_ONLY_REGEX = "[\\p{L}\\s]+";
    private static final String ONLY_NUMBERS = "\\d+";

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPersonnelNumber;
    @FXML private ComboBox<String> comboBoxCoordinator;
    
    @FXML private Label labelMessage;

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
        Optional<String> message = getFirstValidationMessage();
        if (message.isPresent()) {
            showMessage(message.get());
        } else {
            labelMessage.setText("");
            registerProfessor();
        }
    }

   private Optional<String> getFirstValidationMessage() {
        return Stream.of(
            validateFirstName(txtFirstName.getText().trim(), "El nombre"),
            validateLastName(txtLastName.getText().trim(), "El apellido"),
            validatePassword(txtPassword.getText().trim(),"La contraseña"),
            validatePersonnelNumber(txtPersonnelNumber.getText().trim(), "El numero de personal")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateFirstName(String firstName, String field) {
        Optional<String> message = Optional.empty();
        if (firstName.isEmpty() || firstName.length() > MAX_FIRST_NAME_LENGTH) {
            message = Optional.of("El nombre no puede estar vacío o tener más de "
                + MAX_FIRST_NAME_LENGTH + " caracteres");
        } else if(!firstName.matches(LETTERS_ONLY_REGEX)){
            message = Optional.of("El nombre solo pueden contener letras");
        }
        return message;
    }

    private Optional<String> validateLastName(String lastName, String field) {
        Optional<String> message;
        if(lastName.isEmpty() || lastName.length() > MAX_LAST_NAME_LENGHT) {
            message = Optional.of("El apellido no puede estar vacío o tener más de "
                + MAX_LAST_NAME_LENGHT + " caracteres");
        } else {
            message = Optional.empty();
        }
        return message;
    }

    private Optional<String> validatePassword(String password, String field) {
        Optional<String> message = Optional.empty();
        if (password.isEmpty()) {
            message = Optional.of("La contraseña no puede estar vacía");
        } else if(!password.matches(PASSWORD_REGEX)) {
            message = Optional.of("La contraseña no tiene un formato válido");
        } 
        return message;
    }

    private Optional<String> validatePersonnelNumber(String personnelNumber, String field) {
        Optional<String> message = Optional.empty();
        if (personnelNumber.isEmpty()) {
            message = Optional.of("El numero de personal no puede estar vacío");
        } else if (!personnelNumber.matches(ONLY_NUMBERS)){
            message = Optional.of("El numero de personal no tiene un formato válido");
        }
        return message;
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
                    showMessage("message al registrar profesor");
                }
            } else {
                showMessage("message registrando usuario");
            }

        } catch (OperationException e) {
            showMessage(e.getMessage());
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
        if(isCoordinator) {
            professor.setUserType("Coordinador");
        } else {
            professor.setUserType("Profesor");
        }
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

    private void showMessage(String message) {
        labelMessage.setText(message);
        labelMessage.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        labelMessage.setText(message);
        labelMessage.setStyle("-fx-text-fill: green;");

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