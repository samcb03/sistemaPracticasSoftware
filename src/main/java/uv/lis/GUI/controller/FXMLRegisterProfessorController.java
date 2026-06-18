package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.PROFESSOR_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateEmail;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePassword;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;

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

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateText(textFieldFirstName.getText(), "El nombre"),
            validateText(textFieldLastName.getText(), "El apellido"),
            validateEmail(textFieldEmail.getText().trim(), "El correo electrónico"),
            validatePassword(passwordFieldPassword.getText().trim(), "La contraseña"),
            validateExactLength(textFieldPersonnelNumber.getText().trim(), PROFESSOR_ID_LENGTH,
                "El número de personal"),
            validatePositiveInteger(textFieldPersonnelNumber.getText().trim(), "El número de personal")
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
                showSuccess("Profesor registrado correctamente");
                clearFields();
            } else {
                showError("Error al registrar al profesor");
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

    @Override
    public void clearFields() {
        textFieldFirstName.clear();
        textFieldLastName.clear();
        textFieldEmail.clear();
        passwordFieldPassword.clear();
        textFieldPersonnelNumber.clear();
    }
}