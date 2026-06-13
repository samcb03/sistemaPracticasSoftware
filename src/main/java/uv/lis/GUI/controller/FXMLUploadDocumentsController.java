package uv.lis.GUI.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
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
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLUploadDocumentsController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLUploadDocumentsController.class.getName());

    @FXML private Button buttonUpLoadDocumet;
    @FXML private Button buttonBack;
    @FXML private Label labelError;
    @FXML private ComboBox<String> comboBoxDocuments;

    private ExpedientDAO expedientDAO;
    private Student student;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.expedientDAO = new ExpedientDAO();
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
            showError("Error al cargar documentos");
        }
    }

    @FXML
    public void uploadDocument(ActionEvent event) {
        if (student == null) {
            showError("No hay alumno en sesión.");
        } else {
            String selectedDocument = comboBoxDocuments.getSelectionModel().getSelectedItem();
            if (selectedDocument == null) {
                showError("Seleccione un tipo de documento.");
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Seleccionar archivo para " + selectedDocument);
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
                );

                File selectedFile = fileChooser.showOpenDialog(buttonUpLoadDocumet.getScene().getWindow());
                if (selectedFile == null) {
                    showError("No se seleccionó ningún archivo.");
                } else {
                    try {
                        expedientDAO.uploadDocument(student.getIdStudent(), selectedDocument, selectedFile);
                        showSuccess("Documento subido exitosamente.");
                        clearFields();
                    } catch (OperationException e) {
                        showError(e.getMessage());
                    }
                }
            }
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