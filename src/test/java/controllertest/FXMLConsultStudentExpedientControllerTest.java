package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLConsultStudentExpedientController;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

public class FXMLConsultStudentExpedientControllerTest extends ApplicationTest {

    private static final String CONSULT_VIEW_FXML = "/uv/lis/GUI/view/FXMLConsultStudentExpedient.fxml";
    private static final String EXPEDIENT_DAO_FIELD = "expedientDAO";

    private static final String FILTER_COMBO_SELECTOR = "#comboBoxFilter";
    private static final String TABLE_SELECTOR = "#tableViewArchives";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String FILTER_REPORTS = "Reportes";
    private static final String FILTER_INITIAL_DOCUMENTS = "Documentos iniciales";

    private static final String VALID_STUDENT_ID = "S12345678";
    private static final String DOCUMENT_NAME = "documento.pdf";
    private static final String DOCUMENT_TYPE = "Reporte";
    private static final String DOCUMENT_URL = "/ruta/documento.pdf";

    private static final int TYPE_REPORT_FINAL = 2;
    private static final int TYPE_REPORT_PARTIAL = 4;
    private static final int TYPE_INITIAL_BIRTH = 6;

    private static final int MIXED_TOTAL = 3;
    private static final int REPORTS_TOTAL = 2;

    private static final String EXPECTED_NO_DOCUMENTS_MESSAGE = "El alumno no tiene documentos registrados";
    private static final String EXPECTED_NO_CATEGORY_MESSAGE = "No hay documentos en esta categoría";
    private static final String DAO_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLConsultStudentExpedientController consultController;
    private ExpedientDAO expedientDAOMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(CONSULT_VIEW_FXML));
        Parent root = loader.load();
        consultController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
    }

    @BeforeEach
    void setUpMocks() throws ReflectiveOperationException {
        expedientDAOMock = mock(ExpedientDAO.class);
        injectField(EXPEDIENT_DAO_FIELD, expedientDAOMock);
    }

    @Test
    void loadStudentArchives_withDocuments_populatesTable() throws OperationException {
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(buildMixedDocuments());

        loadArchives();

        assertEquals(MIXED_TOTAL, tableRowCount());
    }

    @Test
    void loadStudentArchives_noDocuments_showsEmptyMessage() throws OperationException {
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(new ArrayList<>());

        loadArchives();

        assertEquals(EXPECTED_NO_DOCUMENTS_MESSAGE, messageText());
    }

    @Test
    void applyFilter_reports_showsOnlyReportDocuments() throws OperationException {
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(buildMixedDocuments());

        loadArchives();
        selectFilter(FILTER_REPORTS);

        assertEquals(REPORTS_TOTAL, tableRowCount());
    }

    @Test
    void applyFilter_noMatches_showsCategoryMessage() throws OperationException {
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenReturn(buildReportsOnlyDocuments());

        loadArchives();
        selectFilter(FILTER_INITIAL_DOCUMENTS);

        assertEquals(EXPECTED_NO_CATEGORY_MESSAGE, messageText());
    }

    @Test
    void loadStudentArchives_daoError_showsErrorMessage() throws OperationException {
        OperationException operationException = new OperationException(DAO_ERROR_MESSAGE, null);
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenThrow(operationException);

        loadArchives();

        assertEquals(operationException.getMessage(), messageText());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLConsultStudentExpedientController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(consultController, value);
    }

    private Expedient builderExpedient(int idTypeDocument) {
        Expedient expedient = new Expedient(
            DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_URL, VALID_STUDENT_ID, idTypeDocument);
        return expedient;
    }

    private List<Expedient> buildMixedDocuments() {
        List<Expedient> documents = new ArrayList<>();
        documents.add(builderExpedient(TYPE_REPORT_FINAL));
        documents.add(builderExpedient(TYPE_REPORT_PARTIAL));
        documents.add(builderExpedient(TYPE_INITIAL_BIRTH));
        return documents;
    }

    private List<Expedient> buildReportsOnlyDocuments() {
        List<Expedient> documents = new ArrayList<>();
        documents.add(builderExpedient(TYPE_REPORT_FINAL));
        documents.add(builderExpedient(TYPE_REPORT_PARTIAL));
        return documents;
    }

    private void loadArchives() {
        interact(() -> consultController.loadStudentArchives(VALID_STUDENT_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void selectFilter(String filter) {
        interact(() -> setComboBoxValue(FILTER_COMBO_SELECTOR, filter));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private void setComboBoxValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.setValue(value);
    }

    @SuppressWarnings("unchecked")
    private int tableRowCount() {
        TableView<Expedient> tableView = lookup(TABLE_SELECTOR).queryAs(TableView.class);
        return tableView.getItems().size();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }
}