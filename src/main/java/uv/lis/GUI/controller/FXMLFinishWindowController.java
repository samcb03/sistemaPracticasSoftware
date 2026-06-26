package uv.lis.GUI.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.ActionTableCell;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.dto.ProjectSummary;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileManager;
import uv.lis.logic.utils.SessionManager;

public class FXMLFinishWindowController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLFinishWindowController.class.getName());
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay alumno en sesión";
    private static final String NO_PROJECT_ASSIGNED_MESSAGE = "No se encontró un proyecto asignado";
    private static final String LOAD_ERROR_MESSAGE = "Error al cargar el resumen final";
    private static final String NO_DOCUMENTS_MESSAGE = "No tienes documentos registrados";
    private static final String FILE_NOT_FOUND_ERROR = "El documento no se encontró en la ruta registrada";
    private static final String FILE_OPEN_ERROR = "No se pudo abrir el documento";
    private static final String LOAD_DOCUMENTS_ERROR = "No se pudieron cargar los documentos";
    private static final String EMPTY_TEXT = "";
    private static final String INVALID_URL_MESSAGE = "El documento no tiene una ruta válida";
    private static final String DESKTOP_NOT_SUPPORTED_MESSAGE = "La apertura de documentos no está soportada en este sistema";

    @FXML private VBox sectionSummary;
    @FXML private VBox sectionDocuments;
    @FXML private Button buttonShowSummary;
    @FXML private Button buttonShowDocuments;
    @FXML private Label labelStudentName;
    @FXML private Label labelProjectName;
    @FXML private Label labelOrganizationName;
    @FXML private Label labelProfessorName;
    @FXML private Label labelMethodology;
    @FXML private Label labelObjective;
    @FXML private Label labelDescription;
    @FXML private Label labelAccumulatedHours;
    @FXML private Label labelGrade;
    @FXML private Label labelMessage;
    @FXML private Button buttonBack;
    @FXML private TableView<Expedient> tableDocuments;
    @FXML private TableColumn<Expedient, String> columnName;
    @FXML private TableColumn<Expedient, String> columnType;
    @FXML private TableColumn<Expedient, Void> columnAction;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final PracticeDAO practiceDAO = new PracticeDAO();
    private final ReportContextDAO reportContextDAO = new ReportContextDAO();
    private final ExpedientDAO expedientDAO = new ExpedientDAO();

    private Student currentStudent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        configureTableColumns();
        configurePlaceholder();
        loadSummary();
        loadDocuments();
        showSummary();
    }

    @Override
    protected void clearFields() {
        labelStudentName.setText(EMPTY_TEXT);
        labelProjectName.setText(EMPTY_TEXT);
        labelOrganizationName.setText(EMPTY_TEXT);
        labelProfessorName.setText(EMPTY_TEXT);
        labelMethodology.setText(EMPTY_TEXT);
        labelObjective.setText(EMPTY_TEXT);
        labelDescription.setText(EMPTY_TEXT);
        labelAccumulatedHours.setText(EMPTY_TEXT);
        labelGrade.setText(EMPTY_TEXT);
        labelMessage.setText(EMPTY_TEXT);
        tableDocuments.getItems().clear();
    }

    @FXML
    private void showSummary() {
        sectionSummary.setVisible(true);
        sectionSummary.setManaged(true);
        sectionDocuments.setVisible(false);
        sectionDocuments.setManaged(false);
    }

    @FXML
    private void showDocuments() {
        sectionDocuments.setVisible(true);
        sectionDocuments.setManaged(true);
        sectionSummary.setVisible(false);
        sectionSummary.setManaged(false);
    }

    private void loadSummary() {
        if (currentStudent == null) {
            showError(NO_STUDENT_IN_SESSION_MESSAGE);
        } else {
            try {
                String studentId = currentStudent.getIdStudent();
                labelStudentName.setText(currentStudent.getFirstName() + " " + currentStudent.getLastName());
                Optional<ProjectSummary> projectDetails = projectDAO.getProjectDetailsByStudentId(studentId);

                if (projectDetails.isEmpty()) {
                    showError(NO_PROJECT_ASSIGNED_MESSAGE);
                } else {
                    String accumulatedHours = reportContextDAO.getTotalReportedHoursByStudentId(studentId);
                    String finalGrade = practiceDAO.getFinalGrade(studentId);
                    populateSummaryLabels(projectDetails.get(), accumulatedHours, finalGrade);
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, LOAD_ERROR_MESSAGE, e);
                showError(e.getMessage());
            }
        }
    }

    private void populateSummaryLabels(ProjectSummary details, String hours, String grade) {
        labelProjectName.setText(details.getProjectName());
        labelOrganizationName.setText(details.getOrganizationName());
        labelProfessorName.setText(details.getProfessorName());
        labelMethodology.setText(details.getMethodology());
        labelObjective.setText(details.getObjective());
        labelDescription.setText(details.getDescription());
        labelAccumulatedHours.setText(hours);
        labelGrade.setText(grade);
    }

    private void configureTableColumns() {
        columnName.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getName()));

        columnType.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getTypeDocument()));

        columnAction.setCellFactory(column
            -> new ActionTableCell(this::openDocument));
    }

    private void configurePlaceholder() {
        tableDocuments.setPlaceholder(new Label(NO_DOCUMENTS_MESSAGE));
    }

    private void loadDocuments() {
        if (currentStudent != null) {
            try {
                List<Expedient> documents = expedientDAO.getDocumentsByStudentId(currentStudent.getIdStudent());
                ObservableList<Expedient> documentList = FXCollections.observableArrayList(documents);
                tableDocuments.setItems(documentList);
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al cargar documentos del alumno", e);
                showError(LOAD_DOCUMENTS_ERROR);
            }
        }
    }

    private void openDocument(Expedient expedient) {
        if (expedient == null || expedient.getUrl() == null || expedient.getUrl().isBlank()) {
            showError(INVALID_URL_MESSAGE);
        } else {
            launchDocument(expedient);
        }
    }

    private void launchDocument(Expedient expedient) {
        try {
            File documentFile = FileManager.resolveFile(expedient.getUrl());

            if (!documentFile.exists()) {
                showError(FILE_NOT_FOUND_ERROR);
            } else {
                tryOpenWithDesktop(documentFile);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al resolver la ruta del documento", e);
            showError(FILE_NOT_FOUND_ERROR);
        }
    }

    private void tryOpenWithDesktop(File documentFile) {
        if (!Desktop.isDesktopSupported()) {
            showError(DESKTOP_NOT_SUPPORTED_MESSAGE);
        } else {
            executeDesktopOpen(documentFile);
        }
    }

    private void executeDesktopOpen(File documentFile) {
        try {
            Desktop.getDesktop().open(documentFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error de E/S al abrir el documento", e);
            showError(FILE_OPEN_ERROR);
        }
    }
}