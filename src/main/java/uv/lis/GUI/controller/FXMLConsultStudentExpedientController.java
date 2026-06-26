package uv.lis.GUI.controller;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REJECTED;
import static uv.lis.logic.utils.InputValidator.validateGrade;

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
import javafx.scene.control.TextField;
import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.ActionTableCell;
import uv.lis.GUI.cell.DocumentReviewTableCell;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.dto.Practice;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLConsultStudentExpedientController extends ValidationHandler {

    private static final Logger LOGGER = Logger.getLogger(
        FXMLConsultStudentExpedientController.class.getName());

    private static final String NO_DOCUMENTS_MESSAGE = "El alumno no tiene documentos registrados";
    private static final String NO_DOCUMENTS_FOR_FILTER_MESSAGE = "No hay documentos en esta categoría";
    private static final String INVALID_URL_MESSAGE = "El documento no tiene una ruta válida";
    private static final String FILE_NOT_FOUND_MESSAGE = "El documento no se encontró en la ruta registrada";
    private static final String DESKTOP_NOT_SUPPORTED_MESSAGE = "La apertura de documentos no está soportada en" 
        + " este sistema";
    private static final String DOCUMENT_OPENED_MESSAGE = "Documento abierto correctamente";
    private static final String REVIEW_SUCCESS_MESSAGE = "Estatus del documento actualizado correctamente";
    private static final String REVIEW_FAILURE_MESSAGE = "No se pudo actualizar el estatus del documento";
    private static final String CONFIRM_VALIDATE_MESSAGE = "¿Desea validar este documento?";
    private static final String CONFIRM_REJECT_MESSAGE = "¿Desea rechazar este documento?";
    private static final String CONFIRM_TITLE = "Confirmación";
    private static final String ALREADY_VALIDATED_MESSAGE = "El documento ya está validado y no puede"  
        + " cambiar de estado.";
    private static final String GRADE_FIELD_NAME = "La calificación";
    private static final String GRADE_REGISTER_SUCCESS = "Calificación registrada correctamente";
    private static final String GRADE_REGISTER_FAILURE = "No se pudo registrar la calificación";
    private static final String STATUS_NAME_VALIDATED = "Validado";
    private static final String STATUS_NAME_REJECTED = "Rechazado";
    private static final String TOTAL_FORMAT = "Total: %d documento(s)";
    private static final String REVIEW_FORBIDDEN_MESSAGE 
        = "No tiene permisos para validar o rechazar documentos de este alumno.";
    private static final String GRADE_FORBIDDEN_MESSAGE
        = "No tiene permisos para registrar la calificación de este alumno.";

    private static final String FILTER_ALL = "Todos";
    private static final String FILTER_REPORTS = "Reportes";
    private static final String FILTER_INITIAL_DOCUMENTS = "Documentos iniciales";
    private static final String FILTER_LIBERATION_LETTER = "Carta de liberación";

    private static final int LAST_REPORT_TYPE_ID = 4;
    private static final int FIRST_INITIAL_DOCUMENT_TYPE_ID = 5;
    private static final int LAST_INITIAL_DOCUMENT_TYPE_ID = 11;
    private static final int LIBERATION_LETTER_TYPE_ID = 13;
    private static final int AUTOEVALUATION_TYPE_ID = 1;
    private static final int OV_EVALUATION_TYPE_ID = 12;

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
    @FXML private TextField textFieldGrade;
    @FXML private Button buttonAcceptGrade;

    private ExpedientDAO expedientDAO;
    private PracticeDAO practiceDAO;
    private SubjectDAO subjectDAO;
    private List<Expedient> allDocuments;
    private boolean canReviewAndGrade;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        expedientDAO = new ExpedientDAO();
        practiceDAO = new PracticeDAO();
        subjectDAO = new SubjectDAO();
        allDocuments = new ArrayList<>();
        setupControls(labelMessage, buttonBack);
        configureTableColumns();
        configureFilterComboBox();
    }

    public void loadStudentArchives(String studentId) {
        labelStudentId.setText(studentId);
        resolveReviewAndGradePermissions(studentId);

        try {
            allDocuments = expedientDAO.getDocumentsByStudentId(studentId);
            applyCurrentFilter();
            checkIfAllFinalDocumentsValidated(studentId);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los documentos del alumno", e);
            showError(e.getMessage());
        }
    }

    @Override
    protected void clearFields() {
        labelStudentId.setText("");
        labelMessage.setText("");
        labelTotal.setText("");
        tableViewArchives.getItems().clear();
    }

   @FXML
    private void addPractice() {
        if (canReviewAndGrade) {
            showError(GRADE_FORBIDDEN_MESSAGE);
        } else {
            String gradeInput = textFieldGrade.getText().trim();
            Optional<String> validationError = validateGrade(gradeInput, GRADE_FIELD_NAME);

            if (validationError.isPresent()) {
                showError(validationError.get());
            } else {
                saveGrade(labelStudentId.getText(), Integer.parseInt(gradeInput));
            }
        }
    }

    private void resolveReviewAndGradePermissions(String studentId) {
        Professor currentProfessor = SessionManager.getInstance().getCurrentProfessor();

        if (currentProfessor == null) {
            LOGGER.log(Level.WARNING, "No hay profesor en sesión al cargar el expediente del alumno");
            canReviewAndGrade = false;
        } else if (!currentProfessor.getIsCoordinator()) {
            canReviewAndGrade = true;
        } else {
            canReviewAndGrade = checkIfCoordinatorTeachesStudent(currentProfessor.getPersonnelNumber(), studentId);
        }

        applyReviewAndGradeRestrictions();
    }

    private boolean checkIfCoordinatorTeachesStudent(String personnelNumber, String studentId) {
        boolean isTeaching = false;

        try {
            isTeaching = subjectDAO.isProfessorTeachingStudent(personnelNumber, studentId);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar si el coordinador imparte clase al alumno", e);
            showError(e.getMessage());
        }
        return isTeaching;
    }

    private void applyReviewAndGradeRestrictions() {
        columnReview.setVisible(canReviewAndGrade);
        textFieldGrade.setVisible(canReviewAndGrade);
        textFieldGrade.setManaged(canReviewAndGrade);
        buttonAcceptGrade.setVisible(canReviewAndGrade);
        buttonAcceptGrade.setManaged(canReviewAndGrade);
    }

    private void configureFilterComboBox() {
        comboBoxFilter.getItems().addAll(FILTER_ALL, FILTER_REPORTS, FILTER_INITIAL_DOCUMENTS);

        if (SessionManager.getInstance().getCurrentCoordinator().isPresent()) {
            comboBoxFilter.getItems().add(FILTER_LIBERATION_LETTER);
        }

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
        } else if (FILTER_LIBERATION_LETTER.equals(filter)) {
            filtered = allDocuments.stream()
                .filter(this::isLiberationLetter)
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

    private boolean isLiberationLetter(Expedient expedient) {
        boolean isLiberationLetterType = expedient.getIdTypeDocument() == LIBERATION_LETTER_TYPE_ID;
        return isLiberationLetterType;
    }

    private void reviewDocument(Expedient expedient, int idStatus) {
        if (canReviewAndGrade) {
            showError(REVIEW_FORBIDDEN_MESSAGE);
        } else if (expedient.isValidated()) {
            showError(ALREADY_VALIDATED_MESSAGE);
        } else {
            confirmAndApplyReview(expedient, idStatus);
        }
    }

    private void confirmAndApplyReview(Expedient expedient, int idStatus) {
        String confirmationMessage = idStatus == STATUS_REJECTED
            ? CONFIRM_REJECT_MESSAGE
            : CONFIRM_VALIDATE_MESSAGE;

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
        String statusName = idStatus == STATUS_ASSIGNED ? STATUS_NAME_VALIDATED : STATUS_NAME_REJECTED;
        expedient.setIdStatus(idStatus);
        expedient.setStatusName(statusName);
        tableViewArchives.refresh();
        showSuccess(REVIEW_SUCCESS_MESSAGE);
        LOGGER.log(Level.INFO, "Documento con id {0} actualizado al estatus {1}",
            new Object[] { expedient.getId(), idStatus });

        if (idStatus == STATUS_ASSIGNED) {
            checkIfAllFinalDocumentsValidated(labelStudentId.getText());
        }
    }

    private void checkIfAllFinalDocumentsValidated(String studentId) {
        boolean allFinalDocsValidated = areFinalDocumentsValidated();

        if (allFinalDocsValidated) {
            enableGradeInputIfNotRegistered(studentId);
        }
    }

    private boolean areFinalDocumentsValidated() {
        boolean autoevaluationValidated = hasValidatedDocumentOfType(AUTOEVALUATION_TYPE_ID);
        boolean ovEvaluationValidated = hasValidatedDocumentOfType(OV_EVALUATION_TYPE_ID);
        boolean liberationLetterValidated = hasValidatedDocumentOfType(LIBERATION_LETTER_TYPE_ID);
        boolean allValidated = autoevaluationValidated && ovEvaluationValidated && liberationLetterValidated;
        return allValidated;
    }

    private boolean hasValidatedDocumentOfType(int typeId) {
        boolean hasValidated = allDocuments.stream()
            .filter(doc -> doc.getIdTypeDocument() == typeId)
            .anyMatch(Expedient::isValidated);
        return hasValidated;
    }

    private void enableGradeInputIfNotRegistered(String studentId) {
        if (!canReviewAndGrade) {
            try {
                boolean alreadyExists = practiceDAO.existsByStudent(studentId);

                if (!alreadyExists) {
                    textFieldGrade.setDisable(false);
                    buttonAcceptGrade.setDisable(false);
                }
            } catch (OperationException e) {
                LOGGER.log(Level.SEVERE, "Error al verificar calificación existente", e);
                showError(e.getMessage());
            }
        }
    }

    private void saveGrade(String studentId, int grade) {
        try {
            Practice practice = new Practice();
            practice.setIdStudent(studentId);
            practice.setCalification(grade);

            boolean isRegistered = practiceDAO.registerPractice(practice);

            if (isRegistered) {
                showSuccess(GRADE_REGISTER_SUCCESS);
                LOGGER.log(Level.INFO, "Calificación {0} registrada para el alumno {1}",
                    new Object[] { grade, studentId });
                textFieldGrade.setDisable(true);
                buttonAcceptGrade.setDisable(true);
            } else {
                showError(GRADE_REGISTER_FAILURE);
            }
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la calificación", e);
            showError(e.getMessage());
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
        File documentFile = new File(expedient.getUrl());

        if (!documentFile.exists()) {
            showError(FILE_NOT_FOUND_MESSAGE);
        } else {
            tryOpenWithDesktop(documentFile);
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
            showSuccess(DOCUMENT_OPENED_MESSAGE);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error de E/S al abrir el documento", ioException);
            showError("No se pudo abrir el documento");
        }
    }

    private void populateTable(List<Expedient> documents) {
        labelMessage.setText("");

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
}