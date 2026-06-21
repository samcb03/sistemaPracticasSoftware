package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateBirthDate;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;                 
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;              
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Activity;   
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLManageStudentController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLManageStudentController.class.getName());

    private static final String LABEL_INACTIVE = "Inactivo";
    private static final String LABEL_ACTIVE = "Activo";
    private static final String GENDER_MALE = "Hombre";
    private static final String GENDER_FEMALE = "Mujer";
    private static final String GENDER_OTHER = "Otro";

    private static final int TAB_PERSONAL = 0;
    private static final String DEFAULT_HOURS = "0";

    @FXML private TextField textFieldStudentId;
    @FXML private Button buttonSearch;
    @FXML private TabPane tabPaneStudent;       
    @FXML private Label labelStudentId;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelDateBirth;
    @FXML private Label labelGender;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonSave;
    @FXML private Button buttonInactivate;
    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private ContextMenu contextMenuSuggestions;
    @FXML private Label labelSubject;
    @FXML private Label labelProject;
    @FXML private Label labelIsInactive;
    @FXML private TextField textFieldName;
    @FXML private TextField textFieldLastName;
    @FXML private DatePicker datePickerBirthDate;
    @FXML private ComboBox<String> comboBoxGender;
    @FXML private Label labelCompletedHours;
    @FXML private TableView<Activity> tableViewActivityDetails;
    @FXML private TableColumn<Activity, String> tableColumnActivityName;
    @FXML private TableColumn<Activity, String> tableColumnActivityDescription;
    @FXML private TableColumn<Activity, LocalDate> tableColumnActivityStartDate;
    @FXML private TableColumn<Activity, LocalDate> tableColumnActivityEndDate;

    private StudentDAO studentDAO;
    private RequestProjectDAO requestProjectDAO;
    private SubjectDAO subjectDAO;
    private ActivityDAO activityDAO;
    private ReportContextDAO reportContextDAO;
    private Student currentStudent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentDAO = new StudentDAO();
        requestProjectDAO = new RequestProjectDAO();
        subjectDAO = new SubjectDAO();
        activityDAO = new ActivityDAO();
        reportContextDAO = new ReportContextDAO();
        setupControls(labelMessage, buttonBack);
        tabPaneStudent.setVisible(true);
        comboBoxGender.getItems().addAll(GENDER_MALE, GENDER_FEMALE, GENDER_OTHER);
        setupActivityTableColumns();
        applyRolePermissions();
        setupAutocomplete();
    }

    public void loadStudentData(String studentId) {
        try {
            Optional<Integer> userIdOptional = studentDAO.getIdUserByStudentId(studentId);

            if (userIdOptional.isEmpty()) {
                showError("No se encontró al alumno");
            } else {
                Optional<Student> studentOptional = studentDAO.getStudentById(userIdOptional.get());

                if (studentOptional.isEmpty()) {
                    showError("No se encontró al alumno");
                } else {
                    currentStudent = studentOptional.get();
                    displayStudentInformation(currentStudent);
                    loadStudentAcademicInformation(studentId);
                    tabPaneStudent.setVisible(true);
                    buttonInactivate.setVisible(true);
                }
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar la información del alumno", e);
            showError(e.getMessage());
        }
    }

    private void displayStudentInformation(Student student) {
        labelStudentId.setText(student.getIdStudent());
        labelFirstName.setText(student.getFirstName());
        labelLastName.setText(student.getLastName());
        labelDateBirth.setText(student.getBirthDate().toString());
        labelGender.setText(student.getGender());
    }

    private void loadStudentAcademicInformation(String studentId) throws OperationException {
        String assignedNrc = subjectDAO.getSubjectNrcByStudentID(studentId);
        labelSubject.setText(assignedNrc);
        labelProject.setText(requestProjectDAO.getProjectAssignedToStudent(studentId));
        labelIsInactive.setText(studentDAO.isStudentInactive(studentId) ? LABEL_INACTIVE : LABEL_ACTIVE);

        List<Activity> activities = activityDAO.getActivitiesByStudentId(studentId);
        tableViewActivityDetails.setItems(FXCollections.observableArrayList(activities));
        labelCompletedHours.setText(resolveCompletedHours(activities));
    }

    @FXML
    private void searchStudent() {
        clearFields();
        String studentId = textFieldStudentId.getText().trim();
        Optional<String> validationError = validateExactLength(studentId, STUDENT_ID_LENGTH, "La matricula");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                Optional<Integer> userIdOptional = studentDAO.getIdUserByStudentId(studentId);

                if (userIdOptional.isPresent()) {
                    int userId = userIdOptional.get();
                    Optional<Student> studentOptional = studentDAO.getStudentById(userId);

                    if (studentOptional.isPresent()) {
                        currentStudent = studentOptional.get();
                        displayStudentInformation(currentStudent);
                        loadStudentAcademicInformation(studentId);
                        tabPaneStudent.getSelectionModel().select(TAB_PERSONAL);
                    }
                } else {
                    showError("No se encontró al alumno con esa matrícula");
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al consultar al alumno", e);
                showError(e.getMessage());
            }
        }
    }

    @FXML
    private void enableEditMode() {
        if (currentStudent == null) {
            showError("Primero debe buscar un alumno");
        } else {
            loadCurrentDataIntoEditors();
            toggleEditMode(true);
            tabPaneStudent.getSelectionModel().select(TAB_PERSONAL);
        }
    }

    @FXML
    private void saveStudent() {
        Optional<String> validationError = validateInputs();

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                Student updatedStudent = buildUpdatedStudent();
                boolean isUpdated = studentDAO.modifyStudent(updatedStudent);
                handleUpdateResult(isUpdated, updatedStudent);
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al actualizar al alumno", e);
                showError(e.getMessage());
            }
        }
    }

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentStudent.getFirstName());
        textFieldLastName.setText(currentStudent.getLastName());
        datePickerBirthDate.setValue(currentStudent.getBirthDate().toLocalDate());
        comboBoxGender.setValue(currentStudent.getGender());
    }

    private Optional<String> validateInputs() {
        Optional <String> validateErrors = Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldLastName.getText(), "Los apellidos"),
            validateBirthDate(datePickerBirthDate.getValue(), "La fecha de nacimiento"),
            validateComboBox(comboBoxGender, "genero")
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
        return validateErrors;
    }

    private Student buildUpdatedStudent() {
        Student student = new Student();
        student.setId(currentStudent.getId());
        student.setIdStudent(currentStudent.getIdStudent());
        student.setFirstName(textFieldName.getText().trim());
        student.setLastName(textFieldLastName.getText().trim());
        student.setBirthDate(Date.valueOf(datePickerBirthDate.getValue()));
        student.setGender(comboBoxGender.getValue());
        return student;
    }

    private void handleUpdateResult(boolean isUpdated, Student updatedStudent) {
        if (!isUpdated) {
            showError("No se realizaron cambios en el alumno");
        } else {
            currentStudent = updatedStudent;
            displayStudentInformation(updatedStudent);
            toggleEditMode(false);
            showSuccess("Alumno actualizado correctamente");
            LOGGER.log(Level.INFO, "Alumno actualizado: {0}", updatedStudent.getIdStudent());
        }
    }

    private void toggleEditMode(boolean isEditing) {
        setNodeVisibility(labelFirstName, !isEditing);
        setNodeVisibility(labelLastName, !isEditing);
        setNodeVisibility(labelDateBirth, !isEditing);
        setNodeVisibility(labelGender, !isEditing);

        setNodeVisibility(textFieldName, isEditing);
        setNodeVisibility(textFieldLastName, isEditing);
        setNodeVisibility(datePickerBirthDate, isEditing);
        setNodeVisibility(comboBoxGender, isEditing);

        setNodeVisibility(buttonUpdate, !isEditing);
        setNodeVisibility(buttonSave, isEditing);
    }

    private void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    private void setupAutocomplete() {
        textFieldStudentId.textProperty().addListener((observable, oldValue, newValue)
            -> handleAutocompleteChange(newValue));
        textFieldStudentId.focusedProperty().addListener((observable, wasFocused, isFocused)
            -> handleFocusChange(isFocused));
    }

    private void handleFocusChange(boolean isFocused) {
        if (isFocused) {
            handleAutocompleteChange(textFieldStudentId.getText());
        }
    }

    private void handleAutocompleteChange(String newValue) {
        contextMenuSuggestions.getItems().clear();
        String searchValue = newValue == null ? "" : newValue.trim();

        try {
            ArrayList<String> matches = studentDAO.searchStudentIds(searchValue);

            if (matches.isEmpty()) {
                contextMenuSuggestions.hide();
            } else {
                populateSuggestions(matches);
                contextMenuSuggestions.show(textFieldStudentId, Side.BOTTOM, 0, 0);
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.WARNING, "Error al cargar sugerencias", operationException);
            showError(operationException.getMessage());
            contextMenuSuggestions.hide();
        }
    }

    private void populateSuggestions(ArrayList<String> matches) {
        for (String studentId : matches) {
            MenuItem item = new MenuItem(studentId);
            item.setOnAction(event -> {
                textFieldStudentId.setText(studentId);
                contextMenuSuggestions.hide();
            });
            contextMenuSuggestions.getItems().add(item);
        }
    }

    @FXML
    private void inactivateStudent() {
        String studentId = labelStudentId.getText().trim();
        Optional<String> validationError = validateExactLength(studentId, STUDENT_ID_LENGTH, "La matricula");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                if (studentDAO.isStudentInactive(studentId)) {
                    showError("El estudiante ya se encuentra inactivado.");
                } else {
                    confirmAndInactivate(studentId);
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al inactivar al alumno", e);
                showError(e.getMessage());
            }
        }
    }

    private void confirmAndInactivate(String studentId) throws OperationException {
        boolean confirmed = showConfirmation(
            "Confirmar inactivación",
            "¿Está seguro que desea inactivar al estudiante?"
        );

        if (!confirmed) {
            showError("Inactivación cancelada.");
        } else if (studentDAO.hasProjectAssigned(studentId)) {
            handleInactivationWithProject(studentId);
        } else {
            studentDAO.inactivateStudent(studentId);
            showSuccess("El estudiante ha sido inactivado correctamente");
        }
    }

    private void handleInactivationWithProject(String studentId) throws OperationException {
        boolean confirmedAnyway = showConfirmation("Proyecto asignado",
            "El estudiante tiene un proyecto asignado. ¿Desea inactivarlo de todas formas?"
        );

        if (confirmedAnyway) {
            studentDAO.inactivateStudent(studentId);
            requestProjectDAO.unassignStudentFromProject(studentId);
            showSuccess("El estudiante ha sido inactivado correctamente");
        } else {
            showError("Inactivación cancelada");
        }
    }

    private void applyRolePermissions() {
        Professor currentProfessor = SessionManager.getInstance().getCurrentProfessor();

        if (currentProfessor == null) {
            LOGGER.log(Level.WARNING,
                "No hay profesor en sesión al cargar la vista de consulta de alumno");
            hideCoordinatorOnlyControls();
        } else if (!currentProfessor.getIsCoordinator()) {
            hideCoordinatorOnlyControls();
        }
    }

    private void hideCoordinatorOnlyControls() {
        setNodeVisibility(textFieldStudentId, false);
        setNodeVisibility(buttonSearch, false);
        setNodeVisibility(buttonUpdate, false);
        setNodeVisibility(buttonInactivate, false);
    }

    private void setupActivityTableColumns() {
        tableColumnActivityName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnActivityDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableColumnActivityStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        tableColumnActivityEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
    }

    private String resolveCompletedHours(List<Activity> activities) throws OperationException {
        String completedHours = DEFAULT_HOURS;

        if (!activities.isEmpty()) {
            completedHours = reportContextDAO.getTotalReportedHoursByStudentId(currentStudent.getIdStudent());
        }
        return completedHours;
    }

    @Override
    protected void clearFields() {
        labelStudentId.setText("");
        labelFirstName.setText("");
        labelLastName.setText("");
        labelDateBirth.setText("");
        labelGender.setText("");
        labelMessage.setText("");
        labelMessage.setText("");
        labelCompletedHours.setText("");
        tableViewActivityDetails.getItems().clear();
    }
}