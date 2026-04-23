package uv.lis.GUI.controller;
 

import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.UserDAO;
import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 

public class FXMLRegisterStudentController implements Initializable {
 
    @FXML private Button buttonBack;
    @FXML private Button buttonRegister;
    @FXML private Pane panelForm;
    @FXML private Label labelTitle;
    @FXML private Label labelError;
 
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtStudentId;
    @FXML private DatePicker datePickerBirthDate;
    @FXML private ComboBox<String> comboBoxGender;
 
    private UserDAO userDAO;
    private StudentDAO studentDAO;
 
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        comboBoxGender.setItems(FXCollections.observableArrayList("Hombre", "Mujer", "Otro"));
    }
 
    @FXML
    public void validateFields() {
        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String password  = txtPassword.getText().trim();
        String studentId = txtStudentId.getText().trim();
 
        if (firstName.isEmpty() || firstName.length() > 50) {
            showError("El nombre no puede estar vacio o tener mas de 50 caracteres");
            return;
        }
        if (!firstName.matches("[\\p{L}\\s]+")) {
            showError("El nombre solo acepta letras");
            return;
        }
        if (lastName.isEmpty() || lastName.length() > 50) {
            showError("Los apellidos no pueden estar vacios o tener mas de 50 caracteres");
            return;
        }
        if (!lastName.matches("[\\p{L}\\s]+")) {
            showError("Los apellidos solo aceptan letras");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            showError("La contraseña necesita 6 caracteres");
            return;
        }
        if (studentId.isEmpty() || studentId.length() != 9) {
            showError("La matricula debe tener 9 caracteres");
            return;
        }
        if (datePickerBirthDate.getValue() == null) {
            showError("Seleccione fecha de nacimiento");
            return;
        }
        if (comboBoxGender.getValue() == null) {
            showError("Seleccione un genero");
            return;
        }
 
        labelError.setText("");
        registerStudent();
    }
 
    private void registerStudent() {
        Student student = buildStudent();
 
        try {
            int generatedId = userDAO.registerUser(student);
            if (generatedId != -1) {
                student.setId(generatedId);
                studentDAO.registerStudent(student);
                showSuccess("Estudiante registrado correctamente");
                clearFields();
            } else {
                showError("Error registrando al usuario");
            }
        } catch (OperationException e) {
            e.getMessage();
        }
        
    }
 
    private Student buildStudent() {
        Student student = new Student();
        student.setFirstName(txtFirstName.getText().trim());
        student.setLastName(txtLastName.getText().trim());
        student.setPassword(txtPassword.getText().trim());
        student.setIdStudent(txtStudentId.getText().trim());
        student.setBirthDate(Date.valueOf(datePickerBirthDate.getValue()));
        student.setGender(comboBoxGender.getValue());
        student.setUserType("Student");
        student.setInactive(false);
        student.setIdentification(txtStudentId.getText().trim());
        return student;
    }
 
    private void clearFields() {
        txtFirstName.clear();
        txtLastName.clear();
        txtPassword.clear();
        txtStudentId.clear();
        datePickerBirthDate.setValue(null);
        comboBoxGender.setValue(null);
    }
 
    private void showError(String message) {
        labelError.setText(message);
    }
 
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
 
    @FXML
    public void goBack() {
        Stage stage = (Stage) buttonBack.getScene().getWindow();
        stage.close();
    }
}
