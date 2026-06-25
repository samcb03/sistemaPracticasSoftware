package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLGeneratePartialReportController;
import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGeneratePartialReportControllerTest extends ApplicationTest {

    private static final String PARTIAL_REPORT_VIEW_FXML = "/uv/lis/GUI/view/FXMLGeneratePartialReport.fxml";
    private static final String ACTIVITY_DAO_FIELD = "activityDAO";
    private static final String REPORT_DAO_FIELD = "reportDAO";
    private static final String PARTIAL_REPORT_COMMON_FIELD = "partialReportCommon";

    private static final String ACTIVITY_1_COMBO_SELECTOR = "#comboBoxActivity1";
    private static final String ACTIVITY_2_COMBO_SELECTOR = "#comboBoxActivity2";
    private static final String ADVANCE_1_SELECTOR = "#textFieldAdvance1";
    private static final String ADVANCE_2_SELECTOR = "#textFieldAdvance2";
    private static final String GENERAL_OBSERVATIONS_SELECTOR = "#textAreaGeneralObservations";
    private static final String RESULTS_SELECTOR = "#textAreaResults";
    private static final String GENERATE_BUTTON_SELECTOR = "#buttonGenerate";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_ACTIVITY_1 = "Analisis de requerimientos";
    private static final String VALID_ACTIVITY_2 = "Diseno de base de datos";
    private static final String VALID_ADVANCE = "80";
    private static final String ABOVE_LIMIT_ADVANCE = "150";
    private static final String VALID_GENERAL_OBSERVATION = "Avance del proyecto satisfactorio";
    private static final String VALID_RESULTS = "Entregable de documentacion tecnica";

    private static final String EXPECTED_NO_ACTIVITY_MESSAGE = "Seleccione una actividad";
    private static final String EXPECTED_EMPTY_ADVANCE_MESSAGE =
        "Porcentaje de Avance de Actividad 1 no puede estar vacío";
    private static final String EXPECTED_DUPLICATE_ACTIVITY_MESSAGE =
        "La actividad ya fue seleccionada. Cada actividad solo puede elegirse una vez.";
    private static final String EXPECTED_ADVANCE_ABOVE_LIMIT_MESSAGE =
        "Porcentaje de Avance de Actividad 2 no puede ser mayor a 100";
    private static final String EXPECTED_OPERATION_ERROR_MESSAGE = "Error de operación de prueba";

    private static final int REPORT_PAGE_WIDTH = 595;
    private static final int REPORT_PAGE_HEIGHT = 842;

    private Stage primaryStage;
    private FXMLGeneratePartialReportController partialReportController;
    private ActivityDAO activityDAOMock;
    private ReportDAO reportDAOMock;
    private PartialReportCommon partialReportCommonMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(buildStudent());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(PARTIAL_REPORT_VIEW_FXML));
        Parent root = loader.load();
        partialReportController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws OperationException, ReflectiveOperationException {
        activityDAOMock = mock(ActivityDAO.class);
        reportDAOMock = mock(ReportDAO.class);
        partialReportCommonMock = mock(PartialReportCommon.class);

        when(activityDAOMock.getActivitiesByStudentId(anyString())).thenReturn(List.of());

        injectField(ACTIVITY_DAO_FIELD, activityDAOMock);
        injectField(REPORT_DAO_FIELD, reportDAOMock);
        injectField(PARTIAL_REPORT_COMMON_FIELD, partialReportCommonMock);

        interact(() -> clearAllFields());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();

        EventQueue.invokeLater(() ->
            Arrays.stream(java.awt.Window.getWindows()).forEach(java.awt.Window::dispose));
    }

    @Test
    void validatorReport_noActivitySelected_showsValidationError() {
        clickGenerate();

        assertEquals(EXPECTED_NO_ACTIVITY_MESSAGE, messageText());
    }

    @Test
    void validatorReport_emptyAdvance_showsValidationError() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));

        clickGenerate();

        assertEquals(EXPECTED_EMPTY_ADVANCE_MESSAGE, messageText());
    }

    @Test
    void validatorReport_duplicateActivities_showsValidationError() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> lookup(ADVANCE_1_SELECTOR).queryAs(TextField.class).setText(VALID_ADVANCE));

        clickGenerate();

        assertEquals(EXPECTED_DUPLICATE_ACTIVITY_MESSAGE, messageText());
    }

    @Test
    void validatorReport_operationFails_showsExceptionMessage() throws Exception {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(partialReportCommonMock.generatePartialReport(any())).thenThrow(operationException);

        fillValidForm();
        clickGenerate();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void validatorReport_validData_registersReport() throws Exception {
        when(partialReportCommonMock.generatePartialReport(any())).thenReturn(buildJasperPrint());
        when(reportDAOMock.registerPartialReport(any())).thenReturn(true);

        fillValidForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        verify(reportDAOMock).registerPartialReport(any());
    }

    @Test
    void validatorReport_selectedActivityAdvanceAboveLimit_showsValidationError() {
        fillValidForm();
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_2_SELECTOR).queryAs(TextField.class).setText(ABOVE_LIMIT_ADVANCE));

        clickGenerate();

        assertEquals(EXPECTED_ADVANCE_ABOVE_LIMIT_MESSAGE, messageText());
    }

    @Test
    void validatorReport_unselectedActivityInvalidAdvance_registersReport() throws Exception {
        when(partialReportCommonMock.generatePartialReport(any())).thenReturn(buildJasperPrint());
        when(reportDAOMock.registerPartialReport(any())).thenReturn(true);

        fillValidForm();
        interact(() -> lookup(ADVANCE_2_SELECTOR).queryAs(TextField.class).setText(ABOVE_LIMIT_ADVANCE));
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        verify(reportDAOMock).registerPartialReport(any());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLGeneratePartialReportController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(partialReportController, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        return student;
    }

    private JasperPrint buildJasperPrint() {
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("ReporteParcialPrueba");
        jasperPrint.setPageWidth(REPORT_PAGE_WIDTH);
        jasperPrint.setPageHeight(REPORT_PAGE_HEIGHT);
        jasperPrint.addPage(new JRBasePrintPage());
        return jasperPrint;
    }

    private void clearAllFields() {
        lookup(ACTIVITY_1_COMBO_SELECTOR).queryAs(ComboBox.class).getItems().clear();
        lookup(ACTIVITY_2_COMBO_SELECTOR).queryAs(ComboBox.class).getItems().clear();
        lookup(ACTIVITY_1_COMBO_SELECTOR).queryAs(ComboBox.class).getSelectionModel().clearSelection();
        lookup(ACTIVITY_2_COMBO_SELECTOR).queryAs(ComboBox.class).getSelectionModel().clearSelection();
        lookup(ADVANCE_1_SELECTOR).queryAs(TextField.class).clear();
        lookup(ADVANCE_2_SELECTOR).queryAs(TextField.class).clear();
        lookup(GENERAL_OBSERVATIONS_SELECTOR).queryAs(TextArea.class).clear();
        lookup(RESULTS_SELECTOR).queryAs(TextArea.class).clear();
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private void fillValidForm() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> lookup(ADVANCE_1_SELECTOR).queryAs(TextField.class).setText(VALID_ADVANCE));
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_GENERAL_OBSERVATION));
        interact(() -> lookup(RESULTS_SELECTOR).queryAs(TextArea.class).setText(VALID_RESULTS));
    }

    @SuppressWarnings("unchecked")
    private void setComboValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.getItems().add(value);
        comboBox.setValue(value);
    }

    private void clickGenerate() {
        clickOn(GENERATE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }
}