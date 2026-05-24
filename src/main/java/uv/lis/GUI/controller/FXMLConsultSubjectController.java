package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import uv.lis.GUI.WindowHandler;
import uv.lis.GUI.cell.StudentListCell;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultSubjectController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultSubjectController.class.getName());
    private static final String CONSULT_STUDENT_VIEW = "/uv/lis/GUI/view/FXMLConsultStudent.fxml";

    @FXML private Label labelSubject;
    @FXML private Label labelNrc;
    @FXML private Label labelSchoolPeriod;
    @FXML private ListView<Student> listViewStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonConsultExpedient;
    @FXML private Button buttonBack;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private Subject currentSubject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureStudentListCell();
    }

    public void initializeData(Subject subject) {
        this.currentSubject = subject;
        displaySubjectInformation();
        loadEnrolledStudents();
    }

    private void configureStudentListCell() {
        listViewStudent.setCellFactory(listView -> new StudentListCell());
    }

    private void displaySubjectInformation() {
        labelSubject.setText(currentSubject.getName());
        labelNrc.setText(String.valueOf(currentSubject.getNrc()));
        labelSchoolPeriod.setText(currentSubject.getSchoolPeriodName());
    }

    private void loadEnrolledStudents() {
        try {
            ArrayList<Student> enrolledStudents
                = subjectDAO.getEnrolledStudentsBySubject(currentSubject.getNrc());
            listViewStudent.setItems(FXCollections.observableArrayList(enrolledStudents));

            if (enrolledStudents.isEmpty()) {
                LOGGER.log(Level.INFO,
                    "La experiencia educativa con NRC {0} no tiene alumnos inscritos",
                    currentSubject.getNrc());
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al cargar los alumnos inscritos", operationException);
            showError(operationException.getMessage());
        }
    }

    @FXML
    private void handleConsultStudent() {
        Student selectedStudent = listViewStudent.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showError("Debe seleccionar un alumno de la lista");
        } else {
            openConsultStudentView(selectedStudent);
        }
    }

    private void openConsultStudentView(Student student) {
        FXMLLoader loader = navigateToWithLoader(CONSULT_STUDENT_VIEW);

        if (loader != null) {
            FXMLConsultStudentController controller = loader.getController();
            controller.loadStudentData(student.getIdStudent());
        }
    }

    @FXML
    private void handleConsultExpedient() {

    }
}