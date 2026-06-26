package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import uv.lis.GUI.WindowHandler;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultStudentController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultStudentController.class.getName());
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final String MANAGE_STUDENT_DETAIL_VIEW = "/uv/lis/GUI/view/FXMLManageStudent.fxml";
    private static final String ADVANCE_IN_PROGRESS = "En proceso";
    private static final String ADVANCE_FINISHED = "Terminado";

    @FXML private TableView<Student> tableViewStudents;
    @FXML private TableColumn<Student, String> tableColumnIdStudent;
    @FXML private TableColumn<Student, String> tableColumnName;
    @FXML private TableColumn<Student, String> tableColumnAdvance;
    @FXML private ComboBox<String> comboBoxAdvance;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();
    private ArrayList<Student> allStudents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        configureAdvanceComboBox();
        configureAdvanceListener();
        configureRowDoubleClick();
        loadStudents();
    }

    private void configureTableColumns() {
        tableColumnIdStudent.setCellValueFactory(new PropertyValueFactory<>("idStudent"));
        tableColumnName.setCellValueFactory(cellData -> resolveFullName(cellData.getValue()));
        tableColumnAdvance.setCellValueFactory(cellData -> resolveAdvanceLabel(cellData.getValue()));
    }

    private SimpleStringProperty resolveFullName(Student student) {
        SimpleStringProperty fullName = new SimpleStringProperty(student.getFirstName() + " " + student.getLastName());
        return fullName;
    }

    private SimpleStringProperty resolveAdvanceLabel(Student student) {
        SimpleStringProperty advanceLabel = new SimpleStringProperty(studentHasFinished(student) 
            ? ADVANCE_FINISHED : ADVANCE_IN_PROGRESS);
        return advanceLabel;
    }

    private void configureAdvanceComboBox() {
        ObservableList<String> advanceOptions = FXCollections.observableArrayList(
            ADVANCE_IN_PROGRESS,
            ADVANCE_FINISHED
        );
        comboBoxAdvance.setItems(advanceOptions);
    }

    private void configureAdvanceListener() {
        comboBoxAdvance.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> filterStudentsByAdvance(newValue));
    }

    private void configureRowDoubleClick() {
        tableViewStudents.setOnMouseClicked(this::handleStudentRowClicked);
    }

    @FXML
    private void handleStudentRowClicked(MouseEvent mouseEvent) {
        boolean isDoubleClick = mouseEvent.getClickCount() == DOUBLE_CLICK_COUNT;
        Student selectedStudent = tableViewStudents.getSelectionModel().getSelectedItem();

        if (isDoubleClick && selectedStudent != null) {
            navigateToShowStudentDetailView(selectedStudent);
        }
    }

    private void navigateToShowStudentDetailView(Student student) {
        FXMLLoader loader = navigateToWithLoader(MANAGE_STUDENT_DETAIL_VIEW);

        if (loader != null) {
            FXMLManageStudentController controller = loader.getController();
            controller.initializeData(student);
        }
    }

    private void loadStudents() {
        try {
            allStudents = studentDAO.getAllActiveStudents();
            tableViewStudents.setItems(FXCollections.observableArrayList(allStudents));
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar alumnos", e);
            showError(e.getMessage());
        }
    }

    private void filterStudentsByAdvance(String selectedAdvance) {
        if (selectedAdvance == null) {
            tableViewStudents.setItems(FXCollections.observableArrayList(allStudents));
        } else {
            boolean filterFinished = ADVANCE_FINISHED.equals(selectedAdvance);

            List<Student> filteredStudents = allStudents.stream()
                .filter(student -> studentHasFinished(student) == filterFinished)
                .collect(Collectors.toList());

            tableViewStudents.setItems(FXCollections.observableArrayList(filteredStudents));
        }
    }

    private boolean studentHasFinished(Student student) {
        boolean hasFinished = false;
        try {
            hasFinished = practiceDAO.existsByStudent(student.getIdStudent());
        } catch (OperationException e) {
            LOGGER.log(Level.WARNING, "Error al verificar práctica del alumno: {0}", student.getIdStudent());
            e.getMessage();
        }
        return hasFinished;
    }
}