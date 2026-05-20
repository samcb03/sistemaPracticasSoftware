package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateExactLength;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.logic.dto.Professor;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;


public class FXMLConsultStudentController extends ValidationHandler{
    @FXML private TextField textFieldStudentId;
    @FXML private Button buttonSearch;
    @FXML private GridPane gridPaneStudentInfo;
    @FXML private Label labelStudentId;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelDateBirth;
    @FXML private Label labelGender;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonInactivate;
    @FXML private Button buttonBack;
    @FXML private Label labelMessage;
    @FXML private ContextMenu contextMenuSuggestions;
    @FXML private Label labelSubject;
    @FXML private Label labelProject;
    @FXML private Label labelIsInactive;

    private StudentDAO studentDAO;
    private RequestProjectDAO requestProjectDAO;
    private SubjectDAO subjectDAO;
    private static final Logger LOGGER = Logger.getLogger(FXMLConsultStudentController.class.getName());
    private static final String LABEL_INACTIVE = "Inactivo";
    private static final String LABEL_ACTIVE = "Activo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentDAO = new StudentDAO();
        requestProjectDAO = new RequestProjectDAO();
        subjectDAO = new SubjectDAO();
        setupControls(labelMessage, buttonBack);
        gridPaneStudentInfo.setVisible(true);
        applyRolePermissions();
        setupAutocomplete();
    }

    public void initializeData(String studentId) {
        try {
            Optional<Integer> userIdOptional = studentDAO.getIdUserByStudentId(studentId);
            Optional<Student> studentOptional = studentDAO.getStudentById(userIdOptional.get());

            if (userIdOptional.isEmpty()) {
                showError("No se encontró al alumno");
                
            } else if (studentOptional.isEmpty()) {
                showError("No se encontró al alumno");
            } else {
                displayStudentInformation(studentOptional.get());
                loadStudentAcademicInformation(studentId);
                gridPaneStudentInfo.setVisible(true);
                buttonInactivate.setVisible(true);
            }
        } catch (OperationException operationException) {
            showError("Error al cargar la información del alumno: "
                + operationException.getMessage());
            gridPaneStudentInfo.setVisible(false);
            buttonInactivate.setVisible(false);
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
        String assignedNrc = subjectDAO.getSubjectNRCByStudentID(studentId);
        labelSubject.setText(assignedNrc);
        labelProject.setText(requestProjectDAO.getProjectAssignedToStudent(studentId));
        labelIsInactive.setText(studentDAO.isStudentInactive(studentId) ? "Inactivo" : "Activo");
    }

    @FXML
    private void searchStudent() {
        clearFields();
        String studentId = textFieldStudentId.getText().trim();
        boolean isStudentFound = false;

        Optional<String> validationError = validateExactLength(studentId, STUDENT_ID_LENGTH, "La studentId ");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                Optional<Integer> userIdOptional = studentDAO.getIdUserByStudentId(studentId);

                if (userIdOptional.isPresent()) {
                    int userId = userIdOptional.get();
                    Optional<Student> studentOptional = studentDAO.getStudentById(userId);

                    if (studentOptional.isPresent()) {
                        Student student = studentOptional.get();
                        
                        labelStudentId.setText(student.getIdStudent());
                        labelFirstName.setText(student.getFirstName());
                        labelLastName.setText(student.getLastName());
                        labelDateBirth.setText(student.getBirthDate().toString());
                        labelGender.setText(student.getGender());
                        
                        String assignedNrc = subjectDAO.getSubjectNRCByStudentID(studentId);
                        labelSubject.setText(assignedNrc);
                        
                        labelProject.setText(requestProjectDAO.getProjectAssignedToStudent(studentId));
                        labelIsInactive.setText(studentDAO.isStudentInactive(studentId) ? "Inactivo" : "Activo");
                        
                        isStudentFound = true;
                    }
                }
                
                if (isStudentFound) {
                    gridPaneStudentInfo.setVisible(true);
                    buttonInactivate.setVisible(true);
                } else {
                    showError("No se encontró al alumno");
                    gridPaneStudentInfo.setVisible(false);
                    buttonInactivate.setVisible(false);
                }

            } catch (OperationException e) {
                showError("Error de base de datos: " + e.getMessage());
                gridPaneStudentInfo.setVisible(false);
                buttonInactivate.setVisible(false);
            }
        }
    }

    private void setupAutocomplete() {
        textFieldStudentId.textProperty().addListener((observable, oldValue, newValue) -> {
            contextMenuSuggestions.getItems().clear();

            if (newValue == null || newValue.trim().isEmpty()) {
                contextMenuSuggestions.hide();
            } else {
                try {
                    ArrayList<String> matches = studentDAO.searchStudentIds(newValue.trim());

                    if (matches.isEmpty()) {
                        contextMenuSuggestions.hide();
                    } else {
                        for (String studentId : matches) {
                            MenuItem item = new MenuItem(studentId);
                            item.setOnAction(e -> {
                                textFieldStudentId.setText(studentId);
                                contextMenuSuggestions.hide();
                            });
                            contextMenuSuggestions.getItems().add(item);
                        }
                    }

                    contextMenuSuggestions.show(textFieldStudentId, Side.BOTTOM, 0, 0);

                } catch (OperationException e) {
                    showError(e.getMessage());
                    contextMenuSuggestions.hide();
                }
            }
        });
    }
    
    @FXML
    private void inactivateStudent() {
        String studentId = labelStudentId.getText().trim();
        Optional<String> validationError = validateExactLength(studentId, STUDENT_ID_LENGTH, "La studentId ");

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
                showError(e.getMessage());
            }
        }
    }

    private void confirmAndInactivate(String studentId) throws OperationException {
        boolean confirmed = showConfirmation(
            "Confirmar inactivación",
            "¿Está seguro que desea inactivar al estudiante?"
        );

        if (confirmed) {
            if (studentDAO.hasProjectAssigned(studentId)) {
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
            } else {
                studentDAO.inactivateStudent(studentId);
                showSuccess("El estudiante ha sido inactivado correctamente");
            }
        } else {
            showError("Inactivación cancelada.");
        }
    }

    private boolean showConfirmation(String title, String message) {
        boolean confirmed = false;
        ButtonType yesButton = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Stage owner = (Stage) buttonInactivate.getScene().getWindow();
        alert.initOwner(owner);

        Optional<ButtonType> result = alert.showAndWait();
        confirmed = result.isPresent() && result.get() == yesButton;
        return confirmed;
    }

    private void applyRolePermissions() {
        Professor currentProfessor = SessionManager.getInstance().getCurrentProfessor();

        if (currentProfessor == null) {
            LOGGER.log(Level.WARNING, "No hay profesor en sesión al cargar la vista de consulta de alumno");
            hideCoordinatorOnlyControls();
        } else if (!currentProfessor.getIsCoordinator()) {
            hideCoordinatorOnlyControls();
        }
    }

    private void hideCoordinatorOnlyControls() {
        setNodeHidden(textFieldStudentId);
        setNodeHidden(buttonSearch);
        setNodeHidden(buttonUpdate);
        setNodeHidden(buttonInactivate);
    }

    private void setNodeHidden(javafx.scene.Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    @Override
    protected void clearFields() {
        labelStudentId.setText("");
        labelFirstName.setText("");
        labelLastName.setText("");
        labelDateBirth.setText("");
        labelGender.setText("");
        labelMessage.setText("");
    }
}
