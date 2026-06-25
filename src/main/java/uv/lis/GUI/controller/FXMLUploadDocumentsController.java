package uv.lis.GUI.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.ActionTableCell;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLUploadDocumentsController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLUploadDocumentsController.class.getName());
    private static final int AUTOEVALUATION_TYPE_ID = 1;
    private static final int PARTIAL_REPORT_TYPE_ID = 4;
    private static final int ACCEPTANCE_LETTER_TYPE_ID = 9;
    private static final int MONTHLY_REPORT_TYPE = 3;
    private static final int ACTIVITY_CRONOGRAM_ID = 11;
    private static final int ORGANIZATION_EVALUATION = 12;
    private static final String NO_STUDENT_MESSAGE = "No hay alumno en sesión.";
    private static final String NO_TYPE_MESSAGE = "Seleccione un tipo de documento.";
    private static final String NO_FILE_MESSAGE = "No se seleccionó ningún archivo.";
    private static final String SUCCESS_MESSAGE = "Documento subido exitosamente.";
    private static final String LOAD_TYPES_ERROR = "Error al cargar documentos";
    private static final String LOAD_DOCUMENTS_ERROR = "Error al cargar los documentos subidos";
    private static final String NO_DOCUMENTS_MESSAGE = "Aún no ha subido documentos.";
    private static final String NO_PROJECT_MESSAGE = 
        "La carta de aceptación solo puede subirse cuando ya tenga un proyecto asignado.";
    private static final String NOT_SAVED_MESSAGE =
        "Este documento solo puede subirse cuando ya esté generado.";
    private static final String ALREADY_VALIDATED_MESSAGE =
        "Este documento ya fue validado y no puede subirse nuevamente.";
    private static final String NO_FINAL_REPORT = "Debe tener validado el reporte final";
    private static final String MONTHLY_DIALOG_TITLE = "Subir reporte mensual";
    private static final String MONTHLY_DIALOG_CONTENT = "Seleccione el mes del reporte a subir:";
    private static final String MONTHLY_FILE_CHOOSER_TITLE = "Seleccionar archivo del reporte mensual";
    private static final String FILE_CHOOSER_TITLE = "Seleccionar archivo";
    private static final String INVALID_URL_ERROR = "El documento no tiene una ruta válida";
    private static final String FILE_NOT_FOUND_ERROR = "El documento no se encontró en la ruta registrada";
    private static final String DESKTOP_NOT_SUPPORTED_ERROR = "La apertura de documentos no está soportada en este sistema";
    private static final String OPEN_FAILED_ERROR = "No se pudo abrir el documento";
    private static final String INVALID_PATH_ERROR = "La ruta del documento no es válida";
    private static final String UNSUPPORTED_ACTION_ERROR = "Esta acción no está soportada en el sistema";
    private static final String INSUFFICIENT_PERMISSIONS_ERROR = "No se cuenta con permisos para abrir el documento";

    @FXML private Button buttonUploadDocument;
    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private ComboBox<String> comboBoxDocuments;
    @FXML private TableView<Expedient> tableViewFiles;
    @FXML private TableColumn<Expedient, String> tableColumnFiles;
    @FXML private TableColumn<Expedient, String> tableColumnType;
    @FXML private TableColumn<Expedient, String> tableColumnState;
    @FXML private TableColumn<Expedient, Void> tableColumnAction;

    private ExpedientDAO expedientDAO;
    private StudentDAO studentDAO;
    private ReportDAO reportDAO;
    private AutoevaluationDAO autoevaluationDAO;
    private Student student;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        expedientDAO = new ExpedientDAO();
        studentDAO = new StudentDAO();
        reportDAO = new ReportDAO();
        autoevaluationDAO = new AutoevaluationDAO();
        setupControls(labelError, buttonBack);
        student = SessionManager.getInstance().getCurrentStudent();
        configureTableColumns();
        loadDocumentTypes();
        loadStudentDocuments();
    }

    @FXML
    public void uploadDocument() {
        if (student == null) {
            showError(NO_STUDENT_MESSAGE);
        } else {
            String selectedDocument = comboBoxDocuments.getSelectionModel().getSelectedItem();
            if (selectedDocument == null) {
                showError(NO_TYPE_MESSAGE);
            } else {
                validateRestrictionAndUpload(selectedDocument);
            }
        }
    }

    @Override
    protected void clearFields() {
        comboBoxDocuments.getSelectionModel().clearSelection();
        comboBoxDocuments.setPromptText("Seleccione un documento");
        labelError.setText("");
        buttonUploadDocument.setDisable(false);
        comboBoxDocuments.buttonCellProperty().set(null);
    }

    private void configureTableColumns() {
        tableColumnFiles.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getName()));
        tableColumnType.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getTypeDocument()));
        tableColumnState.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getStatusName()));
        tableColumnAction.setCellFactory(column
            -> new ActionTableCell(this::openDocument));
    }

    private void loadDocumentTypes() {
        try {
            ArrayList<String> documentTypes = expedientDAO.getAllDocumentsTypes();
            comboBoxDocuments.setItems(FXCollections.observableArrayList(documentTypes));
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los tipos de documento", e);
            showError(LOAD_TYPES_ERROR);
        }
    }

    private void loadStudentDocuments() {
        if (student != null) {
            try {
                List<Expedient> documents = expedientDAO.getDocumentsByStudentId(student.getIdStudent());
                populateTable(documents);
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al cargar los documentos del alumno", e);
                showError(LOAD_DOCUMENTS_ERROR);
            }
        }
    }

    private void populateTable(List<Expedient> documents) {
        ObservableList<Expedient> documentList = FXCollections.observableArrayList(documents);
        tableViewFiles.setItems(documentList);

        if (documents.isEmpty()) {
            showError(NO_DOCUMENTS_MESSAGE);
        }
    }


    private void validateRestrictionAndUpload(String selectedDocument) {
        try {
            Optional<String> restriction = getUploadRestriction(selectedDocument);
            if (restriction.isPresent()) {
                showError(restriction.get());
            } else {
                dispatchUpload(selectedDocument);
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void dispatchUpload(String selectedDocument) throws OperationException {
        Optional<Integer> typeId = expedientDAO.getIdDocumentTypeByName(selectedDocument);

        if (typeId.isPresent() && typeId.get() == MONTHLY_REPORT_TYPE) {
            chooseMonthlyReportAndUpload();
        } else {
            chooseFileAndUpload(selectedDocument);
        }
    }

    private void chooseMonthlyReportAndUpload() throws OperationException {
        List<MonthlyReport> uploadableReports = reportDAO.getUploadableMonthlyReports(student.getIdStudent());

        if (uploadableReports.isEmpty()) {
            showError(NOT_SAVED_MESSAGE);
        } else {
            Optional<MonthlyReport> selectedReport = askMonthlyReportToUpload(uploadableReports);
            selectedReport.ifPresent(this::uploadMonthlyReportFile);
        }
    }

    private Optional<MonthlyReport> askMonthlyReportToUpload(List<MonthlyReport> uploadableReports) {
        List<String> months = new ArrayList<>();

        for (MonthlyReport report : uploadableReports) {
            months.add(report.getMonth());
        }

        ChoiceDialog<String> monthDialog = new ChoiceDialog<>(months.get(0), months);
        monthDialog.setTitle(MONTHLY_DIALOG_TITLE);
        monthDialog.setHeaderText(null);
        monthDialog.setContentText(MONTHLY_DIALOG_CONTENT);
        Optional<String> chosenMonth = monthDialog.showAndWait();
        return chosenMonth.flatMap(month -> findReportByMonth(uploadableReports, month));
    }

    private Optional<MonthlyReport> findReportByMonth(List<MonthlyReport> reports, String month) {
        Optional<MonthlyReport> matchingReport = Optional.empty();
        for (MonthlyReport report : reports) {
            if (report.getMonth().equals(month)) {
                matchingReport = Optional.of(report);
            }
        }
        return matchingReport;
    }

    private void uploadMonthlyReportFile(MonthlyReport report) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(MONTHLY_FILE_CHOOSER_TITLE);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(buttonUploadDocument.getScene().getWindow());
        
        if (selectedFile == null) {
            showError(NO_FILE_MESSAGE);
        } else {
            persistMonthlyReport(report, selectedFile);
        }
    }

    private void persistMonthlyReport(MonthlyReport report, File selectedFile) {
        try {
            expedientDAO.uploadMonthlyReport(student.getIdStudent(), selectedFile, report.getIdReport());
            showSuccess(SUCCESS_MESSAGE);
            clearFields();
            loadStudentDocuments();
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private Optional<String> getUploadRestriction(String selectedDocument) throws OperationException {
        Optional<String> restriction = Optional.empty();
        Optional<Integer> typeId = expedientDAO.getIdDocumentTypeByName(selectedDocument);
        String studentId = student.getIdStudent();

        if (typeId.isPresent()) {
            int idTypeDocument = typeId.get();
            boolean isMonthlyReport = (idTypeDocument == MONTHLY_REPORT_TYPE);

            if (!isMonthlyReport && expedientDAO.isDocumentTypeValidated(studentId, idTypeDocument)) {
                restriction = Optional.of(ALREADY_VALIDATED_MESSAGE);
            } else if (requiresAssignedProject(idTypeDocument) && !studentDAO.hasProjectAssigned(studentId)) {
                restriction = Optional.of(NO_PROJECT_MESSAGE);
            } else if (isGeneratedDocument(idTypeDocument)
                && !generatedDocumentExists(idTypeDocument, studentId)) {
                restriction = Optional.of(NOT_SAVED_MESSAGE);
            } else if (idTypeDocument == ORGANIZATION_EVALUATION && !expedientDAO.isFinalReportValidated(studentId)) {
                restriction = Optional.of(NO_FINAL_REPORT);
            }
        }
        return restriction;
    }

    private boolean requiresAssignedProject(int idTypeDocument) {
        boolean hasRequirement = idTypeDocument == ACCEPTANCE_LETTER_TYPE_ID
            || idTypeDocument == ACTIVITY_CRONOGRAM_ID;
        return hasRequirement;
    }

    private boolean isGeneratedDocument(int idTypeDocument) {
        boolean isGenerated = idTypeDocument >= AUTOEVALUATION_TYPE_ID && idTypeDocument <= PARTIAL_REPORT_TYPE_ID;
        return isGenerated;
    }

    private boolean generatedDocumentExists(int idTypeDocument, String studentId) throws OperationException {
        boolean exists;

        if (idTypeDocument == AUTOEVALUATION_TYPE_ID) {
            exists = autoevaluationDAO.existsByStudent(studentId);
        } else if(idTypeDocument == MONTHLY_REPORT_TYPE) {
            exists = !reportDAO.getUploadableMonthlyReports(studentId).isEmpty();
        } else {
            exists = reportDAO.hasReportOfType(studentId, idTypeDocument);
        }
        return exists;
    }

    private void chooseFileAndUpload(String selectedDocument) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FILE_CHOOSER_TITLE);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(buttonUploadDocument.getScene().getWindow());

        if (selectedFile == null) {
            showError(NO_FILE_MESSAGE);
        } else {
            persistDocument(selectedDocument, selectedFile);
        }
    }

    private void persistDocument(String selectedDocument, File selectedFile) {
        try {
            expedientDAO.uploadDocument(student.getIdStudent(), selectedDocument, selectedFile);
            showSuccess(SUCCESS_MESSAGE);
            clearFields();
            loadStudentDocuments();
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void openDocument(Expedient expedient) {
        if (expedient == null || expedient.getUrl() == null || expedient.getUrl().isBlank()) {
            showError(INVALID_URL_ERROR);
        } else {
            launchDocument(expedient);
        }
    }

    private void launchDocument(Expedient expedient) {
        File documentFile = new File(expedient.getUrl());

        if (!documentFile.exists()) {
            showError(FILE_NOT_FOUND_ERROR);
        } else {
            tryOpenWithDesktop(documentFile);
        }
    }

    private void tryOpenWithDesktop(File documentFile) {
        if (!Desktop.isDesktopSupported()) {
            showError(DESKTOP_NOT_SUPPORTED_ERROR);
        } else {
            executeDesktopOpen(documentFile);
        }
    }

    private void executeDesktopOpen(File documentFile) {
        try {
            Desktop.getDesktop().open(documentFile);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error de E/S al abrir el documento", ioException);
            showError(OPEN_FAILED_ERROR);
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.log(Level.WARNING, "Ruta de documento inválida", illegalArgumentException);
            showError(INVALID_PATH_ERROR);
        } catch (UnsupportedOperationException unsupportedOperationException) {
            LOGGER.log(Level.WARNING, "La acción de abrir documentos no está soportada",
                unsupportedOperationException);
            showError(UNSUPPORTED_ACTION_ERROR);
        } catch (SecurityException securityException) {
            LOGGER.log(Level.WARNING, "Permisos insuficientes para abrir el documento", securityException);
            showError(INSUFFICIENT_PERMISSIONS_ERROR);
        }
    }
}