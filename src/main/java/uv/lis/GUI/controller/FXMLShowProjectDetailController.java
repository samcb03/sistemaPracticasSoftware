package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import uv.lis.GUI.WindowHandler;
import uv.lis.GUI.cell.StudentListCell;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLShowProjectDetailController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLShowProjectDetailController.class.getName());

    @FXML private Label labelName;
    @FXML private Label labelDescription;
    @FXML private Label labelObjective;
    @FXML private Label labelOrganization;
    @FXML private Label labelCapacity;
    @FXML private Label labelMessage;
    @FXML private ListView<Student> listViewStudent;
    @FXML private Button buttonModifyProject;
    @FXML private Button buttonInactivateProject;
    @FXML private Button buttonBack;

    private final RequestProjectDAO requestProjectDAO = new RequestProjectDAO();
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureStudentListCellFactory();
        configureActionButtons();
    }

    public void initializeData(Project project) {
        this.currentProject = project;
        displayProjectInformation();
        loadAssignedStudents();
    }

    private void configureStudentListCellFactory() {
        listViewStudent.setCellFactory(listView -> new StudentListCell());
    }

    private void configureActionButtons() {
        buttonModifyProject.setOnAction(this::handleModifyProject);
        buttonInactivateProject.setOnAction(this::handleInactivateProject);
    }

    private void displayProjectInformation() {
        labelName.setText(currentProject.getName());
        labelDescription.setText(currentProject.getDescription());
        labelObjective.setText(currentProject.getObjective());
        labelOrganization.setText(currentProject.getAffiliatedOrganizationName());
        labelCapacity.setText(String.valueOf(currentProject.getCapacity()));
    }

    private void loadAssignedStudents() {
        try {
            ArrayList<Student> assignedStudents
                = requestProjectDAO.getAssignedStudentsByProjectId(currentProject.getId());
            listViewStudent.setItems(FXCollections.observableArrayList(assignedStudents));

            if (assignedStudents.isEmpty()) {
                LOGGER.log(Level.INFO,
                    "El proyecto con ID {0} no tiene alumnos asignados",
                    currentProject.getId());
            }
        } catch (OperationException operationException) {
            LOGGER.log(Level.SEVERE, "Error al cargar los alumnos asignados al proyecto", operationException);
            showError(operationException.getMessage());
        }
    }

    @FXML
    private void handleModifyProject(ActionEvent actionEvent) {
        
    }

    @FXML
    private void handleInactivateProject(ActionEvent actionEvent) {
        
    }
}