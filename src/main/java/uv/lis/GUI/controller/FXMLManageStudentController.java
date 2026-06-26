package uv.lis.GUI.controller;

import static uv.lis.logic.utils.DateValidator.validateBirthDate;
import static uv.lis.logic.utils.InputValidator.validateComboBox;
import static uv.lis.logic.utils.InputValidator.validateText;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.PropertyValueFactory;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.RequestProjectDAO;
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
    private static final String DEFAULT_HOURS = "0";
    private static final int TAB_PERSONAL = 0;

    @FXML private TabPane tabPaneStudent;
    @FXML private Label labelStudentId;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelDateBirth;
    @FXML private Label labelGender;
    @FXML private Label labelSubject;
    @FXML private Label labelProject;
    @FXML private Label labelIsInactive;
    @FXML private Label labelCompletedHours;
    @FXML private Label labelMessage;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonSave;
    @FXML private Button buttonInactivate;
    @FXML private Button buttonBack;
    @FXML private ComboBox<String> comboBoxGender;
    @FXML private DatePicker datePickerBirthDate;
    @FXML private javafx.scene.control.TextField textFieldName;
    @FXML private javafx.scene.control.TextField textFieldLastName;
    @FXML private TableView<Activity> tableViewActivityDetails;
    @FXML private TableColumn<Activity, String> tableColumnActivityName;
    @FXML private TableColumn<Activity, String> tableColumnActivityDescription;
    @FXML private TableColumn<Activity, LocalDate> tableColumnActivityStartDate;
    @FXML private TableColumn<Activity, LocalDate> tableColumnActivityEndDate;

    private final StudentDAO studentDAO = new StudentDAO();
    private final RequestProjectDAO requestProjectDAO = new RequestProjectDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ReportContextDAO reportContextDAO = new ReportContextDAO();
    private Student currentStudent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        comboBoxGender.getItems().addAll(GENDER_MALE, GENDER_FEMALE, GENDER_OTHER);
        setupActivityTableColumns();
        applyRolePermissions();
    }

    public void initializeData(Student student) {
        currentStudent = student;
        displayStudentInformation(student);

        try {
            loadStudentAcademicInformation(student.getIdStudent());
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar información académica del alumno", e);
            showError(e.getMessage());
        }
    }

    @Override
    protected void clearFields() {
        // No fields to clear in this detail view
    }

    private void displayStudentInformation(Student student) {
        labelStudentId.setText(student.getIdStudent());
        labelFirstName.setText(student.getFirstName());
        labelLastName.setText(student.getLastName());
        labelDateBirth.setText(student.getBirthDate().toString());
        labelGender.setText(student.getGender());
    }

    private void loadStudentAcademicInformation(String studentId) throws OperationException {
        labelSubject.setText(subjectDAO.getSubjectNrcByStudentID(studentId));
        labelProject.setText(requestProjectDAO.getProjectAssignedToStudent(studentId));
        labelIsInactive.setText(studentDAO.isStudentInactive(studentId) ? LABEL_INACTIVE : LABEL_ACTIVE);

        List<Activity> activities = activityDAO.getActivitiesByStudentId(studentId);
        tableViewActivityDetails.setItems(FXCollections.observableArrayList(activities));
        labelCompletedHours.setText(resolveCompletedHours(activities));
    }

    @FXML
    private void enableEditMode() {
        if (currentStudent == null) {
            showError("No hay alumno cargado");
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

    @FXML
    private void inactivateStudent() {
        String studentId = labelStudentId.getText().trim();

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

    private void loadCurrentDataIntoEditors() {
        textFieldName.setText(currentStudent.getFirstName());
        textFieldLastName.setText(currentStudent.getLastName());
        datePickerBirthDate.setValue(currentStudent.getBirthDate().toLocalDate());
        comboBoxGender.setValue(currentStudent.getGender());
    }

    private Optional<String> validateInputs() {
        Optional<String> firstError = Stream.of(
            validateText(textFieldName.getText(), "El nombre"),
            validateText(textFieldLastName.getText(), "Los apellidos"),
            validateBirthDate(datePickerBirthDate.getValue(), "La fecha de nacimiento"),
            validateComboBox(comboBoxGender, "genero"))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
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
        boolean confirmedAnyway = showConfirmation(
            "Proyecto asignado",
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
            LOGGER.log(Level.WARNING, "No hay profesor en sesión al cargar la vista de detalle de alumno");
            hideCoordinatorOnlyControls();
        } else if (!currentProfessor.getIsCoordinator()) {
            hideCoordinatorOnlyControls();
        }
    }

    private void hideCoordinatorOnlyControls() {
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
        String completedHours = "";
        if (activities.isEmpty()) {
            completedHours = DEFAULT_HOURS;
        }
        completedHours = reportContextDAO.getTotalReportedHoursByStudentId(currentStudent.getIdStudent());
        return completedHours;
    }

    @FXML
    private void goToStudentExpedient() {
        if (currentStudent == null) {
            showError("No hay alumno cargado");
        } else {
            FXMLLoader loader = navigateToWithLoader("/uv/lis/GUI/view/FXMLConsultStudentExpedient.fxml");

            if (loader != null) {
                FXMLConsultStudentExpedientController controller = loader.getController();
                controller.loadStudentArchives(currentStudent.getIdStudent());
            }
        }
    }
}