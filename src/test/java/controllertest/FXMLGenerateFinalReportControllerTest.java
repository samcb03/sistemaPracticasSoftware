package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
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

import uv.lis.GUI.controller.FXMLGenerateFinalReportController;
import uv.lis.logic.common.FinalReportCommon;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateFinalReportControllerTest extends ApplicationTest {

    private static final String FINAL_REPORT_VIEW_FXML = "/uv/lis/GUI/view/FXMLGenerateFinalReport.fxml";
    private static final String ACTIVITY_DAO_FIELD = "activityDAO";
    private static final String REPORT_DAO_FIELD = "reportDAO";
    private static final String FINAL_REPORT_COMMON_FIELD = "finalReportCommon";

    private static final String ACTIVITY_1_COMBO_SELECTOR = "#comboBoxActivity1";
    private static final String ACTIVITY_2_COMBO_SELECTOR = "#comboBoxActivity2";
    private static final String ADVANCE_1_SELECTOR = "#textFieldAdvance1";
    private static final String OBSERVATION_1_SELECTOR = "#textAreaObservation1";
    private static final String ADVANCE_2_SELECTOR = "#textFieldAdvance2";
    private static final String OBSERVATION_2_SELECTOR = "#textAreaObservation2";
    private static final String RESULT_1_SELECTOR = "#textFieldResult1";
    private static final String RESULT_ADVANCE_1_SELECTOR = "#textFieldResultAdvance1";
    private static final String OBSERVATION_RESULT_1_SELECTOR = "#textAreaObservationResult1";
    private static final String RESULT_2_SELECTOR = "#textFieldResult2";
    private static final String RESULT_ADVANCE_2_SELECTOR = "#textFieldResultAdvance2";
    private static final String OBSERVATION_RESULT_2_SELECTOR = "#textAreaObservationResult2";
    private static final String GENERAL_OBSERVATIONS_SELECTOR = "#textAreaGeneralObservations";
    private static final String GENERATE_BUTTON_SELECTOR = "#buttonGenerate";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_ACTIVITY_1 = "Análisis de requerimientos";
    private static final String VALID_ACTIVITY_2 = "Diseño de base de datos";
    private static final String VALID_ADVANCE = "80";
    private static final String VALID_OBSERVATION = "Se completó correctamente";
    private static final String VALID_RESULT = "Documento de requerimientos";
    private static final String VALID_GENERAL_OBSERVATION = "Avance según lo planeado";
    private static final String INVALID_ADVANCE = "abc";
    private static final String NEGATIVE_ADVANCE = "-1";

    private static final String EXPECTED_NO_ACTIVITY_1_MESSAGE = "Seleccione Actividad 1";
    private static final String EXPECTED_INVALID_ADVANCE_1_MESSAGE =
        "Porcentaje de Avance de Actividad 1 debe ser un número entero válido";
    private static final String EXPECTED_NEGATIVE_ADVANCE_1_MESSAGE =
        "Porcentaje de Avance de Actividad 1 debe ser un número positivo";
    private static final String EXPECTED_EMPTY_OBSERVATION_1_MESSAGE =
        "Observación de Actividad 1 no puede estar vacío";
    private static final String EXPECTED_EMPTY_RESULT_1_MESSAGE =
        "Entregable 1 no puede estar vacío";
    private static final String EXPECTED_EMPTY_RESULT_ADVANCE_1_MESSAGE =
        "Porcentaje de Avance de Entregable 1 no puede estar vacío";
    private static final String EXPECTED_EMPTY_GENERAL_OBSERVATIONS_MESSAGE =
        "Observaciones Generales no puede estar vacío";
    private static final String EXPECTED_OPERATION_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLGenerateFinalReportController finalReportController;
    private ActivityDAO activityDAOMock;
    private ReportDAO reportDAOMock;
    private FinalReportCommon finalReportCommonMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(buildStudent());

        activityDAOMock = mock(ActivityDAO.class);
        reportDAOMock = mock(ReportDAO.class);
        finalReportCommonMock = mock(FinalReportCommon.class);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FINAL_REPORT_VIEW_FXML));
        Parent root = loader.load();
        finalReportController = loader.getController();

        try {
            injectField(ACTIVITY_DAO_FIELD, activityDAOMock);
            injectField(REPORT_DAO_FIELD, reportDAOMock);
            injectField(FINAL_REPORT_COMMON_FIELD, finalReportCommonMock);
            when(activityDAOMock.getActivitiesByStudentId(anyString())).thenReturn(List.of());
        } catch (ReflectiveOperationException | OperationException e) {
            throw new IOException(e);
        }

        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();

        JasperViewerTestCleaner.disposeOpenWindows();
    }

    @BeforeEach
    void setUpMocks() throws OperationException {
        when(activityDAOMock.getActivitiesByStudentId(anyString())).thenReturn(List.of());

        interact(() -> clearAllFields());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void validateFields_noActivitySelected_showsValidationError() {
        clickGenerate();

        assertEquals(EXPECTED_NO_ACTIVITY_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_invalidAdvance_showsValidationError() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_1_SELECTOR)
            .queryAs(TextField.class).setText(INVALID_ADVANCE));

        clickGenerate();

        assertEquals(EXPECTED_INVALID_ADVANCE_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_negativeAdvance_showsValidationError() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_1_SELECTOR)
            .queryAs(TextField.class).setText(NEGATIVE_ADVANCE));

        clickGenerate();

        assertEquals(EXPECTED_NEGATIVE_ADVANCE_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_emptyObservation_showsValidationError() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_1_SELECTOR)
            .queryAs(TextField.class).setText(VALID_ADVANCE));

        clickGenerate();

        assertEquals(EXPECTED_EMPTY_OBSERVATION_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_emptyResult_showsValidationError() {
        fillActivitiesBlock();

        clickGenerate();

        assertEquals(EXPECTED_EMPTY_RESULT_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_emptyResultAdvance_showsValidationError() {
        fillActivitiesBlock();
        interact(() -> lookup(RESULT_1_SELECTOR)
            .queryAs(TextField.class).setText(VALID_RESULT));

        clickGenerate();

        assertEquals(EXPECTED_EMPTY_RESULT_ADVANCE_1_MESSAGE, messageText());
    }

    @Test
    void validateFields_emptyGeneralObservations_showsValidationError() {
        fillActivitiesBlock();
        fillFirstDeliverableBlock();
        fillSecondDeliverableBlock();

        clickGenerate();

        assertEquals(EXPECTED_EMPTY_GENERAL_OBSERVATIONS_MESSAGE, messageText());
    }

    @Test
    void validateFields_operationFails_showsExceptionMessage() throws Exception {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(finalReportCommonMock.generateFinalReport(any())).thenThrow(operationException);

        fillCompleteForm();
        clickGenerate();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_registersReport() throws Exception {
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("TestReport");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());

        when(finalReportCommonMock.generateFinalReport(any())).thenReturn(jasperPrint);
        when(reportDAOMock.registerFinalReport(any())).thenReturn(true);

        fillCompleteForm();
        clickGenerate();

        WaitForAsyncUtils.waitForFxEvents();

        verify(reportDAOMock).registerFinalReport(any());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLGenerateFinalReportController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(finalReportController, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        return student;
    }

    private void clearAllFields() {
        lookup(ACTIVITY_1_COMBO_SELECTOR).queryAs(ComboBox.class).getItems().clear();
        lookup(ACTIVITY_2_COMBO_SELECTOR).queryAs(ComboBox.class).getItems().clear();
        lookup(ADVANCE_1_SELECTOR).queryAs(TextField.class).clear();
        lookup(OBSERVATION_1_SELECTOR).queryAs(TextArea.class).clear();
        lookup(ADVANCE_2_SELECTOR).queryAs(TextField.class).clear();
        lookup(OBSERVATION_2_SELECTOR).queryAs(TextArea.class).clear();
        lookup(RESULT_1_SELECTOR).queryAs(TextField.class).clear();
        lookup(RESULT_ADVANCE_1_SELECTOR).queryAs(TextField.class).clear();
        lookup(OBSERVATION_RESULT_1_SELECTOR).queryAs(TextArea.class).clear();
        lookup(RESULT_2_SELECTOR).queryAs(TextField.class).clear();
        lookup(RESULT_ADVANCE_2_SELECTOR).queryAs(TextField.class).clear();
        lookup(OBSERVATION_RESULT_2_SELECTOR).queryAs(TextArea.class).clear();
        lookup(GENERAL_OBSERVATIONS_SELECTOR).queryAs(TextArea.class).clear();
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private void fillActivitiesBlock() {
        interact(() -> setComboValue(ACTIVITY_1_COMBO_SELECTOR, VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_2_COMBO_SELECTOR, VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_1_SELECTOR)
            .queryAs(TextField.class).setText(VALID_ADVANCE));
        interact(() -> lookup(OBSERVATION_1_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATION));
        interact(() -> lookup(ADVANCE_2_SELECTOR)
            .queryAs(TextField.class).setText(VALID_ADVANCE));
        interact(() -> lookup(OBSERVATION_2_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATION));
    }

    private void fillFirstDeliverableBlock() {
        interact(() -> lookup(RESULT_1_SELECTOR)
            .queryAs(TextField.class).setText(VALID_RESULT));
        interact(() -> lookup(RESULT_ADVANCE_1_SELECTOR)
            .queryAs(TextField.class).setText(VALID_ADVANCE));
        interact(() -> lookup(OBSERVATION_RESULT_1_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATION));
    }

    private void fillSecondDeliverableBlock() {
        interact(() -> lookup(RESULT_2_SELECTOR)
            .queryAs(TextField.class).setText(VALID_RESULT));
        interact(() -> lookup(RESULT_ADVANCE_2_SELECTOR)
            .queryAs(TextField.class).setText(VALID_ADVANCE));
        interact(() -> lookup(OBSERVATION_RESULT_2_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATION));
    }

    private void fillCompleteForm() {
        fillActivitiesBlock();
        fillFirstDeliverableBlock();
        fillSecondDeliverableBlock();
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_GENERAL_OBSERVATION));
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