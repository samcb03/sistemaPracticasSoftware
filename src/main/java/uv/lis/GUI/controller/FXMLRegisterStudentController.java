package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.STUDENT_ENROLLMENT;
import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateBirthDate;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateIdStudent;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePassword;

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

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterStudentController extends ValidationHandler {

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

    private StudentDAO studentDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        String studentId = textFieldStudentId.getText().trim();
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldFirstName.getText(), "El nombre"),
            validateText(textFieldLastName.getText(), "Los apellidos"),
            validateEmail(textFieldEmail.getText().trim(), "El correo electrónico"),
            validatePassword(passwordFieldPassword.getText().trim(), "La contraseña"),
            validateExactLength(textFieldStudentId.getText().trim(), STUDENT_ID_LENGTH, "La matrícula"),
            validateIdStudent(studentId, STUDENT_ID_LENGTH, "La matricula"),
            validateBirthDate(datePickerBirthDate.getValue(), "La fecha de nacimiento"),
            validateComboBox(comboBoxGender.getValue(), "un género")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerStudent() {
        Student student = buildStudent();

        if (!student.getIdStudent().matches(STUDENT_ENROLLMENT)) {
            showError("La matrícula no tiene un formato válido");
        } else {
            try {
                if (studentDAO.registerStudent(student)) {
                    showSuccess("Estudiante registrado correctamente");
                    clearFields();
                } else {
                    showError("Error al registrar al estudiante");
                }
            } catch (OperationException operationException) {
                showError(operationException.getMessage());
            }
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