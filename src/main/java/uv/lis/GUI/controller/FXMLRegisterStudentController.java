package uv.lis.GUI.controller;


import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.GUI.ValidationAbstract;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import java.net.URL;
import java.sql.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import static uv.lis.logic.utils.InputValidator.INVALID_ID;
import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
import static uv.lis.logic.utils.InputValidator.validatePassword;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateEmail;


public class FXMLRegisterStudentController extends ValidationAbstract {

    private static final int USER_TYPE_STUDENT = 1;

    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private TextField textFieldFirstName;
    @FXML private TextField textFieldLastName;
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private TextField textFieldStudentId;
    @FXML private DatePicker datePickerBirthDate;
    @FXML private ComboBox<String> comboBoxGender;
    
    private UserDAO userDAO;
    private StudentDAO studentDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        comboBoxGender.setItems(FXCollections.observableArrayList("Hombre", "Mujer", "Otro"));
        setupControls(labelError, buttonBack);
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerStudent);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(textFieldFirstName.getText().trim(), "El nombre"),
            validateLettersOnly(textFieldLastName.getText().trim(), "Los apellidos"),
            validateEmail(textFieldEmail.getText().trim()),
            validatePassword(passwordFieldPassword.getText().trim()),
            validateExactLength(textFieldStudentId.getText().trim(), STUDENT_ID_LENGTH, "La matrícula"),
            validateBirthDate(),
            validateComboBox(comboBoxGender.getValue(), "un género")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private Optional<String> validateBirthDate() {
        Optional<String> validationResult;
        if (datePickerBirthDate.getValue() == null) {
            validationResult = Optional.of("Seleccione una fecha de nacimiento");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    private void registerStudent() {
        Student student = buildStudent();
        try {
            int generatedUserId = userDAO.registerUser(student);
            if (generatedUserId != INVALID_ID) {
                student.setId(generatedUserId);
                studentDAO.registerStudent(student);
                showSuccess("Estudiante registrado correctamente");
                clearFields();
            } else {
                showError("Error al registrar el usuario");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Student buildStudent() {
        String studentId = textFieldStudentId.getText().trim();
        Student student = new Student();
        student.setFirstName(textFieldFirstName.getText().trim());
        student.setLastName(textFieldLastName.getText().trim());
        student.setEmail(textFieldEmail.getText().trim());
        student.setPassword(passwordFieldPassword.getText().trim());
        student.setIdStudent(studentId);
        student.setBirthDate(Date.valueOf(datePickerBirthDate.getValue()));
        student.setGender(comboBoxGender.getValue());
        student.setRoleId(USER_TYPE_STUDENT);
        student.setActive(true);
        return student;
    }

    @Override
    public void clearFields() {
        textFieldFirstName.clear();
        textFieldLastName.clear();
        textFieldEmail.clear();
        passwordFieldPassword.clear();
        textFieldStudentId.clear();
        datePickerBirthDate.setValue(null);
        comboBoxGender.setValue(null);
    }
}