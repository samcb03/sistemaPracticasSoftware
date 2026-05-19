package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import uv.lis.GUI.WindowHandler;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLProfessorMenuController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLProfessorMenuController.class.getName());

    @FXML private TableView<Subject> tableViewSubjects;
    @FXML private TableColumn<Subject, Integer> tableColumnNrc;
    @FXML private TableColumn<Subject, String> tableColumnSubjectName;
    @FXML private TableColumn<Subject, Integer> tableColumnPeriod;
    @FXML private ComboBox<String> comboBoxSchoolPeriod;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final SchoolPeriodDAO schoolPeriodDAO = new SchoolPeriodDAO();
    private ArrayList<Subject> allSubjects;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        configureSchoolPeriodListener();
        loadSubjects();
        loadSchoolPeriods();
    }

    private void configureTableColumns() {
        tableColumnNrc.setCellValueFactory(new PropertyValueFactory<>("nrc"));
        tableColumnSubjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnPeriod.setCellValueFactory(new PropertyValueFactory<>("schoolPeriodName"));
    }

    private void configureSchoolPeriodListener() {
        comboBoxSchoolPeriod.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> filterSubjectsByPeriod(newValue));
    }

    private void loadSchoolPeriods() {
        try {
            ArrayList<String> schoolPeriods = schoolPeriodDAO.getAllSchoolPeriodsNames();
            ObservableList<String> observableSchoolPeriods
                = FXCollections.observableArrayList(schoolPeriods);
            comboBoxSchoolPeriod.setItems(observableSchoolPeriods);

            if (observableSchoolPeriods.isEmpty()) {
                LOGGER.log(Level.WARNING, "No se encontraron periodos escolares registrados");
            } 
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar periodos escolares", e);
            showError(e.getMessage());
        }
    }

    private void loadSubjects() {
        Professor professor = SessionManager.getInstance().getCurrentProfessor();
        String personnelNumber = professor.getPersonnelNumber();

        try {
            allSubjects = subjectDAO.getSubjectsByProfessor(personnelNumber);
            tableViewSubjects.setItems(FXCollections.observableArrayList(allSubjects));
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al cargar experiencias educativas",
                operationException);
            showError(operationException.getMessage());
        }
    }

    private void filterSubjectsByPeriod(String selectedPeriod) {
        ArrayList<Subject> filteredSubjects = new ArrayList<>();

        for(Subject subject : allSubjects) {
            if(selectedPeriod.equals(subject.getSchoolPeriodName())) {
                filteredSubjects.add(subject);
            }
        }

        tableViewSubjects.setItems(FXCollections.observableArrayList(filteredSubjects));
    }

    public void goToConsultSubject() {
        
    }
}