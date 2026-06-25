package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePassword;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.PROFESSOR_ID_LENGTH;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProfessorController extends ValidationHandler {

    private static final String FIRST_NAME_FIELD = "El nombre";
    private static final String LAST_NAME_FIELD = "El apellido";
    private static final String PASSWORD_FIELD = "La contraseña";
    private static final String PERSONNEL_NUMER_FIELD = "El número de personal";
    private static final String EMAIL_FIELD = "El correo electrónico";
    private static final String SUCCESSFUL_PROFESSOR_REGISTER_MESSAGE = "Profesor registrado correctamente";
    private static final String ERROR_PROFESSOR_REGISTER_MESSAGE = "Error al registrar la profesor";

    private static final int PROFESSOR_ROLE_ID = 2;

    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private TextField textFieldFirstName;
    @FXML private TextField textFieldLastName;
    @FXML private TextField textFieldEmail;
    @FXML private PasswordField passwordFieldPassword;
    @FXML private TextField textFieldPersonnelNumber;

    private ProfessorDAO professorDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        professorDAO = new ProfessorDAO();
        setupControls(labelMessage, buttonBack);
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::performRegistration);
    }

    @Override
    protected void clearFields() {
        textFieldFirstName.clear();
        textFieldLastName.clear();
        textFieldEmail.clear();
        passwordFieldPassword.clear();
        textFieldPersonnelNumber.clear();
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldFirstName.getText(), FIRST_NAME_FIELD),
            validateText(textFieldLastName.getText(), LAST_NAME_FIELD),
            validateEmail(textFieldEmail.getText().trim(), EMAIL_FIELD),
            validatePassword(passwordFieldPassword.getText().trim(), PASSWORD_FIELD),
            validateExactLength(textFieldPersonnelNumber.getText().trim(), PROFESSOR_ID_LENGTH,
                PERSONNEL_NUMER_FIELD),
            validatePositiveInteger(textFieldPersonnelNumber.getText().trim(), PERSONNEL_NUMER_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void performRegistration() {
        Professor professor = buildProfessor();
        try {
            if (professorDAO.registerProfessor(professor)) {
                showSuccess(SUCCESSFUL_PROFESSOR_REGISTER_MESSAGE);
                clearFields();
            } else {
                showError(ERROR_PROFESSOR_REGISTER_MESSAGE);
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setFirstName(textFieldFirstName.getText().trim());
        professor.setLastName(textFieldLastName.getText().trim());
        professor.setEmail(textFieldEmail.getText().trim());
        professor.setPassword(passwordFieldPassword.getText().trim());
        professor.setPersonnelNumber(textFieldPersonnelNumber.getText().trim());
        professor.setActive(true);
        professor.setRoleId(PROFESSOR_ROLE_ID);
        return professor;
    }
}