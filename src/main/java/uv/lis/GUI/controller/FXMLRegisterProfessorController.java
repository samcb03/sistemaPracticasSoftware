package uv.lis.GUI.controller;


import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.GUI.ValidationAbstract;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.UserDAO;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import static uv.lis.logic.utils.InputValidator.INVALID_ID;
import static uv.lis.logic.utils.InputValidator.validateLettersOnly;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validatePassword;
import static uv.lis.logic.utils.InputValidator.validateComboBox;


public class FXMLRegisterProfessorController extends ValidationAbstract {

    private static final String COORDINATOR_OPTION = "Si";
    private static final int COORDINATOR_USER_TYPE = 3;
    private static final int PROFESSOR_USER_TYPE = 2;

    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private TextField textFieldFirstName;
    @FXML private TextField textFieldLastName;
    @FXML private PasswordField textFieldPassword;
    @FXML private TextField textFieldPersonnelNumber;
    @FXML private ComboBox<String> comboBoxCoordinator;

    private UserDAO userDAO;
    private ProfessorDAO professorDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        professorDAO = new ProfessorDAO();
        comboBoxCoordinator.setItems(FXCollections.observableArrayList("Si", "No"));
        setupControls(labelMessage, buttonBack);
    }

    @FXML
    public void validateFields() {
        Optional<String> firstValidationError = getFirstValidationError();
        handleValidation(firstValidationError, this::registerProfessor);
    }

    private Optional<String> getFirstValidationError() {
        Stream<Optional<String>> validationStream = Stream.of(
            validateLettersOnly(textFieldFirstName.getText().trim(), "El nombre"),
            validateLettersOnly(textFieldLastName.getText().trim(), "El apellido"),
            validatePassword(textFieldPassword.getText().trim()),
            validateText(textFieldPersonnelNumber.getText().trim(), "El número de personal"),
            validateComboBox(comboBoxCoordinator.getValue(), "una opción de coordinador")
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void registerProfessor() {
        Professor professor = buildProfessor();
        try {
            int generatedUserId = userDAO.registerUser(professor);
            if (generatedUserId != INVALID_ID) {
                professor.setId(generatedUserId);
                boolean professorRegistered = professorDAO.registerProfessor(professor);
                if (professorRegistered) {
                    showSuccess("Profesor registrado correctamente");
                    clearFields();
                } else {
                    showError("Error al registrar al profesor");
                }
            } else {
                showError("Error al registrar el usuario");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Professor buildProfessor() {
        boolean isCoordinator = COORDINATOR_OPTION.equals(comboBoxCoordinator.getValue());
        Professor professor = new Professor();
        professor.setFirstName(textFieldFirstName.getText().trim());
        professor.setLastName(textFieldLastName.getText().trim());
        professor.setPassword(textFieldPassword.getText().trim());
        professor.setPersonnelNumber(textFieldPersonnelNumber.getText().trim());
        professor.setIsCoordinator(isCoordinator);
        professor.setRoleId(isCoordinator ? COORDINATOR_USER_TYPE : PROFESSOR_USER_TYPE);
        professor.setInactive(false);
        return professor;
    }

    @Override
    public void clearFields() {
        textFieldFirstName.clear();
        textFieldLastName.clear();
        textFieldPassword.clear();
        textFieldPersonnelNumber.clear();
        comboBoxCoordinator.setValue(null);
    }
}