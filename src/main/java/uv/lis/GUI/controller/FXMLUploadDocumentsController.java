package uv.lis.GUI.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLUploadDocumentsController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLUploadDocumentsController.class.getName());

    private static final int AUTOEVALUATION_TYPE_ID = 1;
    private static final int PARTIAL_REPORT_TYPE_ID = 4;
    private static final int ACCEPTANCE_LETTER_TYPE_ID = 9;

    private static final String NO_STUDENT_MESSAGE = "No hay alumno en sesión.";
    private static final String NO_TYPE_MESSAGE = "Seleccione un tipo de documento.";
    private static final String NO_FILE_MESSAGE = "No se seleccionó ningún archivo.";
    private static final String SUCCESS_MESSAGE = "Documento subido exitosamente.";
    private static final String LOAD_TYPES_ERROR = "Error al cargar documentos";
    private static final String NO_PROJECT_MESSAGE = 
        "La carta de aceptación solo puede subirse cuando ya tenga un proyecto asignado.";
    private static final String NOT_SAVED_MESSAGE =
        "Este documento solo puede subirse cuando ya esté generado.";

    @FXML private Button buttonUpLoadDocumet;
    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private ComboBox<String> comboBoxDocuments;

    private ExpedientDAO expedientDAO;
    private StudentDAO studentDAO;
    private ReportDAO reportDAO;
    private AutoevaluationDAO autoevaluationDAO;
    private Student student;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.expedientDAO = new ExpedientDAO();
        this.studentDAO = new StudentDAO();
        this.reportDAO = new ReportDAO();
        this.autoevaluationDAO = new AutoevaluationDAO();
        setupControls(labelError, buttonBack);
        this.student = SessionManager.getInstance().getCurrentStudent();
        loadDocumentTypes();
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

    @FXML
    public void uploadDocument(ActionEvent event) {
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

    private void validateRestrictionAndUpload(String selectedDocument) {
        try {
            Optional<String> restriction = getUploadRestriction(selectedDocument);
            if (restriction.isPresent()) {
                showError(restriction.get());
            } else {
                chooseFileAndUpload(selectedDocument);
            }
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
            if (idTypeDocument == ACCEPTANCE_LETTER_TYPE_ID
                && !studentDAO.hasProjectAssigned(studentId)) {
                restriction = Optional.of(NO_PROJECT_MESSAGE);
            } else if (isGeneratedDocument(idTypeDocument)
                && !generatedDocumentExists(idTypeDocument, studentId)) {
                restriction = Optional.of(NOT_SAVED_MESSAGE);
            }
        }
        return restriction;
    }

    private boolean isGeneratedDocument(int idTypeDocument) {
        boolean isGenerated = idTypeDocument >= AUTOEVALUATION_TYPE_ID && idTypeDocument <= PARTIAL_REPORT_TYPE_ID;
        return isGenerated;
    }

    private boolean generatedDocumentExists(int idTypeDocument, String studentId) throws OperationException {
        boolean exists;

        if (idTypeDocument == AUTOEVALUATION_TYPE_ID) {
            exists = autoevaluationDAO.existsByStudent(studentId);
        } else {
            exists = reportDAO.hasReportOfType(studentId, idTypeDocument);
        }
        return exists;
    }

    private void chooseFileAndUpload(String selectedDocument) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo para " + selectedDocument);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(buttonUpLoadDocumet.getScene().getWindow());
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
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    @Override
    protected void clearFields() {
        comboBoxDocuments.getSelectionModel().clearSelection();
        comboBoxDocuments.setPromptText("Seleccione un documento");
        labelError.setText("");
        labelError.setVisible(false);
        buttonUpLoadDocumet.setDisable(false);
        comboBoxDocuments.buttonCellProperty().set(null);
    }
}