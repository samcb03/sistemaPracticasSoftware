package uv.lis.GUI.controller;

import static uv.lis.logic.utils.DateValidator.validateBirthDate;
import static uv.lis.logic.utils.InputValidator.STUDENT_ENROLLMENT;
import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
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

    private static final String FIRST_NAME_FIELD = "El nombre";
    private static final String LAST_NAMES_FIELD = "Los apellidos";
    private static final String EMAIL_FIELD = "El correo electrónico";
    private static final String PASSWORD_FIELD = "La contraseña";
    private static final String STUDENT_ID_FIELD = "La matrícula";
    private static final String BIRTHDAY_DATE_FIELD = "La fecha de nacimiento";
    private static final String GENDER_FIELD = "Un género";
    private static final String INVALID_FORMAT_ID_STUDENT = "La matrícula no tiene un formato válido";
    private static final String SUCCESSFUL_STUDENT_REGISTER_MESSAGE = "Estudiante registrado correctamente";
    private static final String ERROR_STUDENT_REGISTER_MESSAGE = "Error al registrar al estudiante";
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
        handleValidation(firstValidationError, this::performRegistration);
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

    private Optional<String> getFirstValidationError() {
        String studentId = textFieldStudentId.getText().trim();
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldFirstName.getText(), FIRST_NAME_FIELD),
            validateText(textFieldLastName.getText(), LAST_NAMES_FIELD),
            validateEmail(textFieldEmail.getText().trim(), EMAIL_FIELD),
            validatePassword(passwordFieldPassword.getText().trim(), PASSWORD_FIELD),
            validateExactLength(textFieldStudentId.getText().trim(), STUDENT_ID_LENGTH, STUDENT_ID_FIELD),
            validateIdStudent(studentId, STUDENT_ID_LENGTH, STUDENT_ID_FIELD),
            validateBirthDate(datePickerBirthDate.getValue(), BIRTHDAY_DATE_FIELD),
            validateComboBox(comboBoxGender.getValue(), GENDER_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        Student student = buildStudent();

        if (!student.getIdStudent().matches(STUDENT_ENROLLMENT)) {
            showError(INVALID_FORMAT_ID_STUDENT);
        } else {
            try {
                if (studentDAO.registerStudent(student)) {
                    showSuccess(SUCCESSFUL_STUDENT_REGISTER_MESSAGE);
                    clearFields();
                } else {
                    showError(ERROR_STUDENT_REGISTER_MESSAGE);
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
}