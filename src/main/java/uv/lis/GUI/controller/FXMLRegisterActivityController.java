package uv.lis.GUI.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uv.lis.GUI.FormValidator;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLRegisterActivityController extends ValidationHandler {

    @FXML private Button buttonRegister;
    @FXML private Button buttonBack;
    @FXML private Label labelActivityName;
    @FXML private Label labelDescription;
    @FXML private Label labelStartDate;
    @FXML private Label labelFinalDate;
    @FXML private Label labelProject;
    @FXML private Label labelError;

    @FXML private TextField textFieldActivity;
    @FXML private TextField textFieldDescription;
    @FXML private DatePicker datePickerStarDate;
    @FXML private DatePicker datePickerFinalDate;

    private ActivityDAO activityDAO;
    private ProjectDAO projectDAO;
    private int currentProjectId; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activityDAO = new ActivityDAO();
        projectDAO = new ProjectDAO();
        setupControls(labelError, buttonBack);
        loadStudentProject(); 
    }

    @FXML
    public void validateFields() {
        Optional<String> validationError = FormValidator.validateActivityForm(
            textFieldActivity.getText(),
            textFieldDescription.getText()
        );
        
        handleValidation(validationError, this::registerActivity);
    }

    private void loadStudentProject() {
        try {
            Student student = SessionManager.getInstance().getCurrentStudent();
            if (student != null) {
                String studentId = student.getIdStudent();
                Optional<Project> assignedProject = projectDAO.getProjectByStudentId(studentId);

                if (assignedProject.isPresent()) {
                    this.currentProjectId = assignedProject.get().getId();
                    labelProject.setText(assignedProject.get().getName());
                } else {
                    showError("No tienes ningún proyecto asignado para registrar actividades.");
                    disableForm();
                }
            } else {
                showError("No hay una sesión activa. Por favor, inicia sesión.");
                disableForm();
            }
            
        } catch (Exception e) { 
            showError("Error crítico al verificar tu proyecto.");
            disableForm(); 
        }
    }

    private void disableForm() {
        buttonRegister.setDisable(true);
        textFieldActivity.setDisable(true);
        textFieldDescription.setDisable(true);
        datePickerStarDate.setDisable(true);
        datePickerFinalDate.setDisable(true);
    }

    private void registerActivity() {
        Activity activity = buildActivity();
        
        try {
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
        activity.setStartDate(datePickerStarDate.getValue());
        activity.setEndDate(datePickerFinalDate.getValue());
        activity.setProjectId(currentProjectId);

        return activity;
    }

    @Override
    protected void clearFields() {
        textFieldActivity.clear();
        textFieldDescription.clear();
        datePickerStarDate.setValue(null);
        datePickerFinalDate.setValue(null);
        
        textFieldActivity.requestFocus(); 
    }
}