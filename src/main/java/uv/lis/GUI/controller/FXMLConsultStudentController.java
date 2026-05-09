package uv.lis.GUI.controller;


import static uv.lis.logic.utils.InputValidator.STUDENT_ID_LENGTH;
import static uv.lis.logic.utils.InputValidator.validateExactLength;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;


public class FXMLConsultStudentController extends ValidationHandler{
    @FXML TextField textFieldStudentId;
    @FXML Button buttonSearch;
    @FXML GridPane gridPaneStudentInfo;
    @FXML Label labelStudentId;
    @FXML Label labelFirstName;
    @FXML Label labelLastName;
    @FXML Label labelDateBirth;
    @FXML Label labelGender;
    @FXML Button buttonUpdate;
    @FXML Button buttonInactivate;
    @FXML Button buttonBack;
    @FXML Label labelMessage;
    @FXML ContextMenu contextMenuSuggestions;
    @FXML Label labelSubject;
    @FXML Label labelProject;

    private StudentDAO studentDAO;
    private RequestProjectDAO requestProjectDAO;
    private SubjectDAO subjectDAO;
    private Subject subject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentDAO = new StudentDAO();
        requestProjectDAO = new RequestProjectDAO();
        subjectDAO = new SubjectDAO();
        subject = new Subject();
        setupControls(labelMessage, buttonBack);
        gridPaneStudentInfo.setVisible(true);
        setupAutocomplete();
    }

    @FXML
    private void searchStudent() {
        clearFields();
        String studentId = textFieldStudentId.getText().trim();

        Optional<String> validationError = validateExactLength(studentId, STUDENT_ID_LENGTH, "La matricula ");

        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            try {
                int userId = studentDAO.getIdUserByStudentId(studentId);
                Student student = studentDAO.getStudentById(userId);

                labelStudentId.setText(student.getIdStudent());
                labelFirstName.setText(student.getFirstName());
                labelLastName.setText(student.getLastName());
                labelDateBirth.setText(student.getBirthDate().toString());
                labelGender.setText(student.getGender());
                labelSubject.setText(subjectDAO.getSubjectNRCByStudentID(studentId) + " - " 
                    + subject.getSUBJECT_NAME());
                labelProject.setText(requestProjectDAO.getProjectAssignedToStudent(studentId));
            } catch (OperationException e) {
                showError(e.getMessage());
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
                        for (String matricula : matches) {
                            MenuItem item = new MenuItem(matricula);
                            item.setOnAction(e -> {
                                textFieldStudentId.setText(matricula);
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
