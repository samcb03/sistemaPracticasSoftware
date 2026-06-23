package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REJECTED;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.ActionTableCell;
import uv.lis.GUI.cell.DocumentReviewTableCell;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultStudentExpedientController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(FXMLConsultStudentExpedientController.class.getName());

    private static final String NO_DOCUMENTS_MESSAGE = "El alumno no tiene documentos registrados";
    private static final String NO_DOCUMENTS_FOR_FILTER_MESSAGE = "No hay documentos en esta categoría";

    private static final String FILTER_ALL = "Todos";
    private static final String FILTER_REPORTS = "Reportes";
    private static final String FILTER_INITIAL_DOCUMENTS = "Documentos iniciales";

    private static final int LAST_REPORT_TYPE_ID = 4;
    private static final int FIRST_INITIAL_DOCUMENT_TYPE_ID = 5;
    private static final int LAST_INITIAL_DOCUMENT_TYPE_ID = 11;

    private static final String TOTAL_FORMAT = "Total: %d documento(s)";
    private static final String REVIEW_SUCCESS_MESSAGE = "Estatus del documento actualizado correctamente";
    private static final String REVIEW_FAILURE_MESSAGE = "No se pudo actualizar el estatus del documento";
    private static final String CONFIRM_VALIDATE_MESSAGE = "¿Desea validar este documento?";
    private static final String CONFIRM_REJECT_MESSAGE = "¿Desea rechazar este documento?";
    private static final String CONFIRM_TITLE = "Confirmación";
    private static final String ALREADY_VALIDATED_MESSAGE =
        "El documento ya está validado y no puede cambiar de estado.";

    @FXML private Label labelStudentId;
    @FXML private Label labelTotal;
    @FXML private Label labelMessage;
    @FXML private Button buttonBack;
    @FXML private ComboBox<String> comboBoxFilter;
    @FXML private TableView<Expedient> tableViewArchives;
    @FXML private TableColumn<Expedient, String> columnName;
    @FXML private TableColumn<Expedient, String> columnDocumentType;
    @FXML private TableColumn<Expedient, String> columnStatus;
    @FXML private TableColumn<Expedient, Void> columnReview;
    @FXML private TableColumn<Expedient, Void> columnAction;

    private ExpedientDAO expedientDAO;
    private List<Expedient> allDocuments;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        expedientDAO = new ExpedientDAO();
        allDocuments = new ArrayList<>();
        setupControls(labelMessage, buttonBack);
        configureTableColumns();
        configureFilterComboBox();
    }

    public void loadStudentArchives(String studentId) {
        labelStudentId.setText(studentId);

        try {
            allDocuments = expedientDAO.getDocumentsByStudentId(studentId);
            applyCurrentFilter();
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los documentos del alumno", e);
            showError(e.getMessage());
        }
    }

    private void configureFilterComboBox() {
        comboBoxFilter.getItems().addAll(FILTER_ALL, FILTER_REPORTS, FILTER_INITIAL_DOCUMENTS);
        comboBoxFilter.setValue(FILTER_ALL);
        comboBoxFilter.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> applyCurrentFilter());
    }

    private void configureTableColumns() {
        columnName.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getName()));
        columnDocumentType.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getTypeDocument()));
        columnStatus.setCellValueFactory(cellData
            -> new SimpleStringProperty(cellData.getValue().getStatusName()));
        columnReview.setCellFactory(column
            -> new DocumentReviewTableCell(this::reviewDocument));
        columnAction.setCellFactory(column
            -> new ActionTableCell(this::openDocument));
    }

    private void reviewDocument(Expedient expedient, int idStatus) {
        if (expedient.isValidated()) {
            showError(ALREADY_VALIDATED_MESSAGE);
        } else {
            confirmAndApplyReview(expedient, idStatus);
        }
    }

    private void confirmAndApplyReview(Expedient expedient, int idStatus) {
        boolean isRejection = idStatus == STATUS_REJECTED;
        String confirmationMessage = isRejection ? CONFIRM_REJECT_MESSAGE : CONFIRM_VALIDATE_MESSAGE;

        if (showConfirmation(CONFIRM_TITLE, confirmationMessage)) {
            applyReview(expedient, idStatus);
        }
    }

    private void applyReview(Expedient expedient, int idStatus) {
        try {
            boolean isUpdated = expedientDAO.updateDocumentStatus(expedient.getId(), idStatus);

            if (isUpdated) {
                confirmReviewChange(expedient, idStatus);
            } else {
                showError(REVIEW_FAILURE_MESSAGE);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estatus del documento", e);
            showError(e.getMessage());
        }
    }

    private void confirmReviewChange(Expedient expedient, int idStatus) {
        expedient.setIdStatus(idStatus);
        expedient.setStatusName(resolveStatusName(idStatus));
        tableViewArchives.refresh();
        showSuccess(REVIEW_SUCCESS_MESSAGE);
        LOGGER.log(Level.INFO, "Documento con id {0} actualizado al estatus {1}",
            new Object[] { expedient.getId(), idStatus });
    }

    private String resolveStatusName(int idStatus) {
        String statusName = "Rechazado";

        if (idStatus == STATUS_ASSIGNED) {
            statusName = "Validado";
        }
        return statusName;
    }

    private void applyCurrentFilter() {
        String selectedFilter = comboBoxFilter.getValue();
        List<Expedient> filteredDocuments = filterDocuments(selectedFilter);
        populateTable(filteredDocuments);
        updateTotalLabel(filteredDocuments.size());
    }

    private List<Expedient> filterDocuments(String filter) {
        List<Expedient> filtered;

        if (FILTER_REPORTS.equals(filter)) {
            filtered = allDocuments.stream()
                .filter(this::isReport)
                .collect(Collectors.toList());
        } else if (FILTER_INITIAL_DOCUMENTS.equals(filter)) {
            filtered = allDocuments.stream()
                .filter(this::isInitialDocument)
                .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(allDocuments);
        }
        return filtered;
    }

    private boolean isReport(Expedient expedient) {
        boolean isReportType = expedient.getIdTypeDocument() <= LAST_REPORT_TYPE_ID;
        return isReportType;
    }

    private boolean isInitialDocument(Expedient expedient) {
        int idTypeDocument = expedient.getIdTypeDocument();
        boolean isInitialDocumentType = idTypeDocument >= FIRST_INITIAL_DOCUMENT_TYPE_ID
            && idTypeDocument <= LAST_INITIAL_DOCUMENT_TYPE_ID;
        return isInitialDocumentType;
    }

    private void populateTable(List<Expedient> documents) {
        clearMessage();

        if (allDocuments.isEmpty()) {
            showError(NO_DOCUMENTS_MESSAGE);
            tableViewArchives.getItems().clear();
        } else if (documents.isEmpty()) {
            showError(NO_DOCUMENTS_FOR_FILTER_MESSAGE);
            tableViewArchives.getItems().clear();
        } else {
            ObservableList<Expedient> documentList = FXCollections.observableArrayList(documents);
            tableViewArchives.setItems(documentList);
        }
    }

    private void updateTotalLabel(int totalDocuments) {
        labelTotal.setText(String.format(TOTAL_FORMAT, totalDocuments));
    }

    private void clearMessage() {
        labelMessage.setText("");
    }

    private void openDocument(Expedient expedient) {
        if (expedient == null || expedient.getUrl() == null || expedient.getUrl().isBlank()) {
            showError("El documento no tiene una ruta válida");
        } else {
            launchDocument(expedient);
        }
    }

    private void launchDocument(Expedient expedient) {
        File documentFile = new File(expedient.getUrl());

        if (!documentFile.exists()) {
            showError("El documento no se encontró en la ruta registrada");
        } else {
            tryOpenWithDesktop(documentFile);
        }
    }

    private void tryOpenWithDesktop(File documentFile) {
        if (!Desktop.isDesktopSupported()) {
            showError("La apertura de documentos no está soportada en este sistema");
        } else {
            executeDesktopOpen(documentFile);
        }
    }

    private void executeDesktopOpen(File documentFile) {
        try {
            Desktop.getDesktop().open(documentFile);
            showSuccess("Documento abierto correctamente");
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error de E/S al abrir el documento", ioException);
            showError("No se pudo abrir el documento");
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.log(Level.WARNING, "Ruta de documento inválida", illegalArgumentException);
            showError("La ruta del documento no es válida");
        } catch (UnsupportedOperationException unsupportedOperationException) {
            LOGGER.log(Level.WARNING, "La acción de abrir documentos no está soportada",
                unsupportedOperationException);
            showError("Esta acción no está soportada en el sistema");
        } catch (SecurityException securityException) {
            LOGGER.log(Level.WARNING, "Permisos insuficientes para abrir el documento", securityException);
            showError("No se cuenta con permisos para abrir el documento");
        }
    }

    @Override
    protected void clearFields() {
        labelStudentId.setText("");
        labelMessage.setText("");
        labelTotal.setText("");
        tableViewArchives.getItems().clear();
    }
}