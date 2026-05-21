package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultProjectController extends WindowHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultProjectController.class.getName());
    private static final int DOUBLE_CLICK_COUNT = 2;
    private static final String CONSULT_PROJECT_VIEW = "/uv/lis/GUI/view/FXMLConsultProject.fxml";

    @FXML private TableView<Project> tableViewProjects;
    @FXML private TableColumn<Project, String> tableColumnProjectName;
    @FXML private TableColumn<Project, String> tableColumnOrganization;
    @FXML private ComboBox<String> comboBoxOrganization;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final AffiliatedOrganizationDAO AffiliatedOrganizationDAO = new AffiliatedOrganizationDAO();
    private ArrayList<Project> allProjects;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        configureOrganizationListener();
        configureRowDoubleClick();
        loadProjects();
        loadOrganizations();
    }

    private void configureTableColumns() {
        tableColumnProjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnOrganization.setCellValueFactory(new PropertyValueFactory<>
            ("affiliatedOrganizationName"));
    }

    private void configureOrganizationListener() {
        comboBoxOrganization.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> filterProjectsByOrganization(newValue));
    }

    private void configureRowDoubleClick() {
        tableViewProjects.setOnMouseClicked(this::handleProjectRowClicked);
    }

    @FXML
    private void handleProjectRowClicked(MouseEvent mouseEvent) {
        boolean isDoubleClick = mouseEvent.getClickCount() == DOUBLE_CLICK_COUNT;
        Project selectedProject = tableViewProjects.getSelectionModel().getSelectedItem();

        if (isDoubleClick && selectedProject != null) {
            navigateToConsultProjectView(selectedProject);
        }
    }

    private void navigateToConsultProjectView(Project project) {
        FXMLLoader loader = navigateToWithLoader(CONSULT_PROJECT_VIEW);

        if (loader != null) {
            FXMLShowProjectDetailController controller = loader.getController();
            controller.initializeData(project);
        }
    }

    private void loadOrganizations() {
        try {
            ArrayList<String> Organizations = AffiliatedOrganizationDAO.getAllOrganizationNames();
            ObservableList<String> observableOrganizations
                = FXCollections.observableArrayList(Organizations);
            comboBoxOrganization.setItems(observableOrganizations);

            if (observableOrganizations.isEmpty()) {
                LOGGER.log(Level.WARNING, "No se encontraron organizaciones vinculadas registradas");
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar organizaciones vinculadas", e);
            showError(e.getMessage());
        }
    }

    private void loadProjects() {
        try {
            allProjects = projectDAO.getAllProjects();
            tableViewProjects.setItems(FXCollections.observableArrayList(allProjects));
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos", e);
            showError(e.getMessage());
        }
    }

    private void filterProjectsByOrganization(String selectedOrganization) {
        if (selectedOrganization == null) {
            tableViewProjects.setItems(FXCollections.observableArrayList(allProjects));
        } else {
            List<Project> filteredProjects = allProjects.stream()
            .filter(project -> selectedOrganization.equals(project.getAffiliatedOrganizationName()))
            .collect(Collectors.toList());

            tableViewProjects.setItems(FXCollections.observableArrayList(filteredProjects));
        }    
    }
}