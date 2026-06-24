package uv.lis.GUI.controller;

import static uv.lis.logic.utils.DateValidator.validateDateWithinPeriod;
import static uv.lis.logic.utils.DateValidator.validateEndDate;
import static uv.lis.logic.utils.DateValidator.validateRecentStartDate;
import static uv.lis.logic.utils.InputValidator.validatePositiveInteger;
import static uv.lis.logic.utils.InputValidator.validateRegister;
import static uv.lis.logic.utils.InputValidator.validateText;
import static uv.lis.logic.utils.InputValidator.validateMaxHoursForDuration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLRegisterActivityController extends ValidationHandler {
    
    private static final String ACTIVITY_NAME_FIELD = "El nombre de la actividad";
    private static final String DESCRIPTION_FIELD = "La descripción";
    private static final String START_DATE_FIELD = "La fecha de inicio";
    private static final String END_DATE_FIELD = "La fecha de finalización";
    private static final String HOURS_FIELD = "Las horas";
    private static final int COUNT_NEXT_DAY = 1;
    private static final int MAX_TOTAL_HOURS = 420;
    private static final String MAX_HOURS_EXCEEDED_MESSAGE =
        "No puedes superar las 420 horas acumuladas de la práctica.";

    @FXML private Button buttonRegister;
    @FXML private Button buttonBack;
    @FXML private Label labelActivityName;
    @FXML private Label labelDescription;
    @FXML private Label labelStartDate;
    @FXML private Label labelFinalDate;
    @FXML private Label labelHours;
    @FXML private Label labelProject;
    @FXML private Label labelError;

    @FXML private TextField textFieldActivity;
    @FXML private TextField textFieldDescription;
    @FXML private TextField textFieldHours;
    @FXML private DatePicker datePickerStartDate;
    @FXML private DatePicker datePickerFinalDate;

    private ActivityDAO activityDAO;
    private ProjectDAO projectDAO;
    private SchoolPeriodDAO schoolPeriodDAO;
    private SchoolPeriod studentPeriod;
    private int currentProjectId;
    private String currentStudentId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activityDAO = new ActivityDAO();
        projectDAO = new ProjectDAO();
        schoolPeriodDAO = new SchoolPeriodDAO();
        setupControls(labelError, buttonBack);
        loadStudentProject();
    }

    @FXML
    public void validateFields() {
        Optional<String> validationError = getFirstValidationError();
        handleValidation(validationError, this::registerIfWithinHourLimit);
    }

    private void registerIfWithinHourLimit() {
        try {
            int newHours = Integer.parseInt(textFieldHours.getText().trim());
            int accumulatedHours = activityDAO.getTotalActivityHoursByStudent(currentStudentId);

            if (accumulatedHours + newHours > MAX_TOTAL_HOURS) {
                showError(MAX_HOURS_EXCEEDED_MESSAGE);
            } else {
                performRegistration();
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Optional<String> getFirstValidationError() {
        long durationInDays = calculateDurationInDays(datePickerStartDate.getValue(), datePickerFinalDate.getValue());
        Stream<Optional<String>> validationStream = Stream.of(
            validateRegister(textFieldActivity.getText(), ACTIVITY_NAME_FIELD),
            validateText(textFieldDescription.getText(), DESCRIPTION_FIELD),
            validateRecentStartDate(datePickerStartDate.getValue(), START_DATE_FIELD),
            validateEndDate(datePickerStartDate.getValue(), datePickerFinalDate.getValue(), END_DATE_FIELD),
            validateDateWithinPeriod(datePickerStartDate.getValue(), studentPeriod, START_DATE_FIELD),
            validateDateWithinPeriod(datePickerFinalDate.getValue(), studentPeriod, END_DATE_FIELD),
            validatePositiveInteger(textFieldHours.getText().trim(), HOURS_FIELD),
            validateMaxHoursForDuration(textFieldHours.getText().trim(), durationInDays, HOURS_FIELD)
        );
        Optional<String> firstError = validationStream
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        return firstError;
    }

    private void loadStudentProject() {
        try {
            Student student = SessionManager.getInstance().getCurrentStudent();
            if (student != null) {
                String studentId = student.getIdStudent();
                this.currentStudentId = studentId;
                Optional<Project> assignedProject = projectDAO.getProjectByStudentId(studentId);

                if (assignedProject.isPresent()) {
                    this.currentProjectId = assignedProject.get().getId();
                    labelProject.setText(assignedProject.get().getName());
                    this.studentPeriod = schoolPeriodDAO.getSchoolPeriodByStudentId(studentId).orElse(null);
                } else {
                    showError("No tienes ningún proyecto asignado para registrar actividades.");
                    disableForm();
                }
            } else {
                showError("No hay una sesión activa. Por favor, inicia sesión.");
                disableForm();
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
            disableForm();
        }
    }

    private void disableForm() {
        buttonRegister.setDisable(true);
        textFieldActivity.setDisable(true);
        textFieldDescription.setDisable(true);
        textFieldHours.setDisable(true);
        datePickerStartDate.setDisable(true);
        datePickerFinalDate.setDisable(true);
    }

    private long calculateDurationInDays(LocalDate startDate, LocalDate endDate) {
        long durationInDays = 0;
        if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            durationInDays = ChronoUnit.DAYS.between(startDate, endDate) + COUNT_NEXT_DAY;
        }
        return durationInDays;
    }

    private void performRegistration() {
        try {
            Activity activity = buildActivity();
            boolean registrationSuccessful = activityDAO.registerActivity(activity);

            if (registrationSuccessful) {
                showSuccess("Actividad registrada correctamente");
                clearFields();
            } else {
                showError("Error al registrar la actividad");
            }
        } catch (OperationException operationException) {
            showError(operationException.getMessage());
        }
    }

    private Activity buildActivity() {
        Activity activity = new Activity();
        activity.setName(textFieldActivity.getText().trim());
        activity.setDescription(textFieldDescription.getText().trim());
        activity.setStartDate(datePickerStartDate.getValue());
        activity.setEndDate(datePickerFinalDate.getValue());
        activity.setHoursReported(Integer.parseInt(textFieldHours.getText().trim()));
        activity.setProjectId(currentProjectId);
        activity.setStudentId(currentStudentId);
        return activity;
    }

    @Override
    protected void clearFields() {
        textFieldActivity.clear();
        textFieldDescription.clear();
        textFieldHours.clear();
        datePickerStartDate.setValue(null);
        datePickerFinalDate.setValue(null);

        textFieldActivity.requestFocus();
    }
}