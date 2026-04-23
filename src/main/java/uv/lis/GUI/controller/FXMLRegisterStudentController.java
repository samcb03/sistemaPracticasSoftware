package uv.lis.GUI.controller;


import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import java.net.URL;
import java.sql.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class FXMLRegisterStudentController implements Initializable {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int STUDENT_ID_LENGTH = 9;
    private static final String LETTERS_ONLY_REGEX = "[\\p{L}\\s]+";
    private static final String USER_TYPE_STUDENT = "Student";
    private static final int NO_USER_GENERATED = -1;

    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;
    @FXML private Pane panelForm;
    @FXML private Label labelTitle;
    @FXML private Label labelError;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtStudentId;
    @FXML private DatePicker datePickerBirthDate;
    @FXML private ComboBox<String> comboBoxGender;

    private UserDAO userDAO;
    private StudentDAO studentDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        comboBoxGender.setItems(FXCollections.observableArrayList("Hombre", "Mujer", "Otro"));
    }

    @FXML
    public void validateFields() {
        Optional<String> error = getFirstValidationError();
        if (error.isPresent()) {
            showError(error.get());
        } else {
            labelError.setText("");
            registerStudent();
        }
    }

    private Optional<String> getFirstValidationError() {
        return Stream.of(
            validateFirstName(txtFirstName.getText().trim()),
            validateLastName(txtLastName.getText().trim()),
            validatePassword(txtPassword.getText().trim()),
            validateStudentId(txtStudentId.getText().trim()),
            validateBirthDate(),
            validateGender()
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    private Optional<String> validateFirstName(String firstName) {
        Optional<String> error;
        if (firstName.isEmpty() || firstName.length() > MAX_NAME_LENGTH) {
            error = Optional.of("El nombre no puede estar vacío o tener más de "
                + MAX_NAME_LENGTH + " caracteres");
        } else if (!firstName.matches(LETTERS_ONLY_REGEX)) {
            error = Optional.of("El nombre solo acepta letras");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateLastName(String lastName) {
        Optional<String> error;
        if (lastName.isEmpty() || lastName.length() > MAX_NAME_LENGTH) {
            error = Optional.of("Los apellidos no pueden estar vacíos o tener más de "
                + MAX_NAME_LENGTH + " caracteres");
        } else if (!lastName.matches(LETTERS_ONLY_REGEX)) {
            error = Optional.of("Los apellidos solo aceptan letras");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validatePassword(String password) {
        Optional<String> error;
        if (password.isEmpty() || password.length() < MIN_PASSWORD_LENGTH) {
            error = Optional.of("La contraseña necesita al menos "
                + MIN_PASSWORD_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateStudentId(String studentId) {
        Optional<String> error;
        if (studentId.isEmpty() || studentId.length() != STUDENT_ID_LENGTH) {
            error = Optional.of("La matrícula debe tener exactamente "
                + STUDENT_ID_LENGTH + " caracteres");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateBirthDate() {
        Optional<String> error;
        if (datePickerBirthDate.getValue() == null) {
            error = Optional.of("Seleccione una fecha de nacimiento");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private Optional<String> validateGender() {
        Optional<String> error;
        if (comboBoxGender.getValue() == null) {
            error = Optional.of("Seleccione un género");
        } else {
            error = Optional.empty();
        }
        return error;
    }

    private void registerStudent() {
        Student student = buildStudent();
        try {
            int generatedId = userDAO.registerUser(student);
            handleRegistrationResult(student, generatedId);
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void handleRegistrationResult(Student student, int generatedId) throws OperationException {
        if (generatedId != NO_USER_GENERATED) {
            student.setId(generatedId);
            studentDAO.registerStudent(student);
            showSuccess("Estudiante registrado correctamente");
            clearFields();
        } else {
            showError("Error registrando al usuario");
        }
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setFirstName(txtFirstName.getText().trim());
        student.setLastName(txtLastName.getText().trim());
        student.setPassword(txtPassword.getText().trim());
        student.setIdStudent(txtStudentId.getText().trim());
        student.setBirthDate(Date.valueOf(datePickerBirthDate.getValue()));
        student.setGender(comboBoxGender.getValue());
        student.setUserType(USER_TYPE_STUDENT);
        student.setInactive(false);
        student.setIdentification(txtStudentId.getText().trim());
        return student;
    }

    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtPassword.clear();
        txtStudentId.clear();
        datePickerBirthDate.setValue(null);
        comboBoxGender.setValue(null);
    }

    private void showError(String message) {
        labelError.setText(message);
    }

    private void showSuccess(String message) {
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