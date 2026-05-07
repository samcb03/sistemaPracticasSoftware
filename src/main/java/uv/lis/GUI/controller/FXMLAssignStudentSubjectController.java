package uv.lis.GUI.controller;


import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.InputValidator;


public class FXMLAssignStudentSubjectController extends ValidationHandler {

    @FXML private Label labelMessage;
    @FXML private ComboBox<String> comboBoxSubjects;
    @FXML private Button buttonAssign;
    @FXML private Button buttonBack;
    @FXML private TableView<Student> tableViewStudents;
    @FXML private TableColumn<Student, String> columnIdStudent;
    @FXML private TableColumn<Student, String> columnName;

    private SubjectDAO subjectDAO;
    private StudentDAO studentDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subjectDAO = new SubjectDAO();
        studentDAO = new StudentDAO();
        setupControls(labelMessage, buttonBack);
        setupTable();
        setupComboBoxListener();
        loadSubjects();
        loadStudents();
    }

    private void loadSubjects() {
        try {
            ArrayList<String> subjects = subjectDAO.getAllSubjectsNRCName();
            comboBoxSubjects.setItems(FXCollections.observableArrayList(subjects));
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void setupTable() {
        columnIdStudent.setCellValueFactory(new PropertyValueFactory<>("idStudent"));
        columnName.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()
        ));
    }

    private void setupComboBoxListener() {
        comboBoxSubjects.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, selectedSubject) -> {
                labelMessage.setText("");
            }
        );
    }

    private void loadStudents() {
        try {
            ArrayList<Student> students = studentDAO.getActiveStudentsNotInSubject();
            if (students.isEmpty()) {
                showError("No hay alumnos disponibles");
            } else {
                tableViewStudents.setItems(FXCollections.observableArrayList(students));
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void validateFields() {
        String selectedSubject = comboBoxSubjects.getSelectionModel().getSelectedItem();
        Student selectedStudent = tableViewStudents.getSelectionModel().getSelectedItem();

        Optional<String> subjectValidation = InputValidator.validateComboBox(
            selectedSubject, "una Experiencia Educativa"
        );
        Optional<String> studentValidation = InputValidator.validateComboBox(
            selectedStudent, "un alumno de la tabla"
        );

        Optional<String> validationError = subjectValidation.or(() -> studentValidation);

        handleValidation(validationError, this::assignStudent);
    }

    private void assignStudent() {
        String selectedSubject = comboBoxSubjects.getSelectionModel().getSelectedItem();
        Student selectedStudent = tableViewStudents.getSelectionModel().getSelectedItem();
        
        try {
            int nrc = Integer.parseInt(selectedSubject.split(" - ")[0]);

            boolean isAssigned = subjectDAO.assignStudentToSubject(
                selectedStudent.getIdStudent(),
                nrc
            );

            if (isAssigned) {
                loadStudents();
                showSuccess("Alumno asignado correctamente.");
            } else {
                showError("No se pudo asignar el alumno. Intente de nuevo.");
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    @Override
    protected void clearFields() {
        comboBoxSubjects.getSelectionModel().clearSelection();
        tableViewStudents.getItems().clear();
        labelMessage.setText("");
    }
}