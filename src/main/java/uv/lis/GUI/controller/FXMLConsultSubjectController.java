package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import uv.lis.GUI.WindowHandler;
import uv.lis.GUI.cell.StudentListCell;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultSubjectController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultSubjectController.class.getName());
    private static final String CONSULT_STUDENT_VIEW = "/uv/lis/GUI/view/FXMLManageStudent.fxml";
    private static final String STUDENT_ARCHIVES_VIEW = "/uv/lis/GUI/view/FXMLConsultStudentExpedient.fxml";
    private static final String SELECT_STUDENT_MESSAGE = "Debe seleccionar un alumno de la lista";

    private static final String FILTER_ALL = "Todos";
    private static final String FILTER_INITIAL_DOCUMENTS = "Archivos iniciales subidos";
    private static final String FILTER_MONTHLY_REPORT = "Reporte mensual subido";
    private static final String FILTER_PARTIAL_REPORT = "Reporte parcial subido";
    private static final String FILTER_FINAL_REPORT = "Reporte final subido";

    private static final int FINAL_REPORT_TYPE_ID = 2;
    private static final int MONTHLY_REPORT_TYPE_ID = 3;
    private static final int PARTIAL_REPORT_TYPE_ID = 4;

    @FXML private Label labelSubject;
    @FXML private Label labelNrc;
    @FXML private Label labelSchoolPeriod;
    @FXML private ComboBox<String> comboBoxDocumentFilter;
    @FXML private ListView<Student> listViewStudent;
    @FXML private Button buttonConsultStudent;
    @FXML private Button buttonConsultExpedient;
    @FXML private Button buttonBack;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ExpedientDAO expedientDAO = new ExpedientDAO();
    private Subject currentSubject;
    private List<Student> enrolledStudents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enrolledStudents = new ArrayList<>();
        configureStudentListCell();
        configureDocumentFilterComboBox();
    }

    public void initializeData(Subject subject) {
        this.currentSubject = subject;
        displaySubjectInformation();
        loadEnrolledStudents();
    }

    private void configureStudentListCell() {
        listViewStudent.setCellFactory(listView -> new StudentListCell());
    }

    private void configureDocumentFilterComboBox() {
        comboBoxDocumentFilter.getItems().addAll(FILTER_ALL, FILTER_INITIAL_DOCUMENTS,
            FILTER_MONTHLY_REPORT, FILTER_PARTIAL_REPORT, FILTER_FINAL_REPORT);
        comboBoxDocumentFilter.setValue(FILTER_ALL);
        comboBoxDocumentFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> applyDocumentFilter(newValue));
    }

    private void displaySubjectInformation() {
        labelSubject.setText(currentSubject.getName());
        labelNrc.setText(String.valueOf(currentSubject.getNrc()));
        labelSchoolPeriod.setText(currentSubject.getSchoolPeriodName());
    }

    private void loadEnrolledStudents() {
        try {
            enrolledStudents = subjectDAO.getEnrolledStudentsBySubject(currentSubject.getNrc());
            listViewStudent.setItems(FXCollections.observableArrayList(enrolledStudents));

            if (enrolledStudents.isEmpty()) {
                LOGGER.log(Level.INFO, "La experiencia educativa con NRC {0} no tiene alumnos inscritos",
                    currentSubject.getNrc());
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los alumnos inscritos", e);
            showError(e.getMessage());
        }
    }

    private void applyDocumentFilter(String selectedFilter) {
        if (FILTER_ALL.equals(selectedFilter)) {
            listViewStudent.setItems(FXCollections.observableArrayList(enrolledStudents));
        } else {
            showStudentsWithSelectedDocuments(selectedFilter);
        }
    }

    private void showStudentsWithSelectedDocuments(String selectedFilter) {
        try {
            List<String> studentIds = resolveStudentIdsForFilter(selectedFilter);
            List<Student> filteredStudents = filterEnrolledStudentsByIds(studentIds);
            listViewStudent.setItems(FXCollections.observableArrayList(filteredStudents));
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al filtrar los alumnos por documentos subidos", e);
            showError(e.getMessage());
        }
    }

    private List<String> resolveStudentIdsForFilter(String filter) throws OperationException {
        List<String> studentIds;
        int nrc = currentSubject.getNrc();

        if (FILTER_INITIAL_DOCUMENTS.equals(filter)) {
            studentIds = expedientDAO.getStudentIdsWithInitialDocuments(nrc);
        } else if (FILTER_MONTHLY_REPORT.equals(filter)) {
            studentIds = expedientDAO.getStudentIdsWithDocumentType(nrc, MONTHLY_REPORT_TYPE_ID);
        } else if (FILTER_PARTIAL_REPORT.equals(filter)) {
            studentIds = expedientDAO.getStudentIdsWithDocumentType(nrc, PARTIAL_REPORT_TYPE_ID);
        } else {
            studentIds = expedientDAO.getStudentIdsWithDocumentType(nrc, FINAL_REPORT_TYPE_ID);
        }
        return studentIds;
    }

    private List<Student> filterEnrolledStudentsByIds(List<String> studentIds) {
        List<Student> filteredStudents = new ArrayList<>();

        for (Student student : enrolledStudents) {
            if (studentIds.contains(student.getIdStudent())) {
                filteredStudents.add(student);
            }
        }
        return filteredStudents;
    }

    @FXML
    private void handleConsultStudent() {
        Student selectedStudent = listViewStudent.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showError(SELECT_STUDENT_MESSAGE);
        } else {
            openConsultStudentView(selectedStudent);
        }
    }

    private void openConsultStudentView(Student student) {
        FXMLLoader loader = navigateToWithLoader(CONSULT_STUDENT_VIEW);

        if (loader != null) {
            FXMLManageStudentController controller = loader.getController();
            controller.loadStudentData(student.getIdStudent());
        }
    }

    @FXML
    private void handleConsultExpedient() {
        Student selectedStudent = listViewStudent.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showError(SELECT_STUDENT_MESSAGE);
        } else {
            openStudentArchivesView(selectedStudent);
        }
    }

    private void openStudentArchivesView(Student student) {
        FXMLLoader loader = navigateToWithLoader(STUDENT_ARCHIVES_VIEW);
    
        if (loader != null) {
            FXMLConsultStudentExpedientController controller = loader.getController();
            controller.loadStudentArchives(student.getIdStudent());
        }
    }
}