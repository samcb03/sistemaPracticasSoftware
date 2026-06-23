package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLGeneratePartialReportController;
import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGeneratePartialReportControllerTest extends ApplicationTest {

    private static final String PARTIAL_REPORT_VIEW_FXML =
        "/uv/lis/GUI/view/FXMLGeneratePartialReport.fxml";

    private static final String ACTIVITY_DAO_FIELD = "activityDAO";
    private static final String REPORT_DAO_FIELD = "reportDAO";
    private static final String PARTIAL_REPORT_COMMON_FIELD = "partialReportCommon";
    private static final String REPORT_CONTEXT_DAO_FIELD = "reportContextDAO";

    private static final String ACTIVITY_COMBO_SELECTOR_PREFIX  = "#comboBoxActivity";
    private static final String ADVANCE_FIELD_SELECTOR_PREFIX = "#textFieldAdvance";
    private static final String GENERAL_OBSERVATIONS_SELECTOR = "#textAreaGeneralObservations";
    private static final String RESULTS_SELECTOR = "#textAreaResults";
    private static final String GENERATE_BUTTON_SELECTOR = "#buttonGenerate";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final int TOTAL_COMBO_FIELDS = 6;

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String STUDENT_NAME = "Denisse Reyes";

    private static final String VALID_ACTIVITY_1 = "Análisis de requerimientos";
    private static final String VALID_ACTIVITY_2 = "Diseño de base de datos";
    private static final String VALID_ADVANCE_1 = "75";
    private static final String VALID_ADVANCE_2 = "60";
    private static final String VALID_OBSERVATIONS = "Se completó correctamente la primera fase";
    private static final String VALID_RESULTS = "Documento de requerimientos aprobado";

    private static final int ACTIVITY_ID_ONE = 1;
    private static final int ACTIVITY_ID_TWO = 2;
    private static final int REPORT_ID = 10;
    private static final LocalDate ACTIVITY_START = LocalDate.of(2025, 1, 6);
    private static final LocalDate ACTIVITY_END = LocalDate.of(2025, 1, 31);
    private static final String ACTIVITY_DESCRIPTION = 
        "Se realizaron entrevistas con los implicados para reunir los requisitos";

    private static final String PROFESSOR_NAME = "Hernández Soto";
    private static final String NRC_SUBJECT = "50522";
    private static final String SCHOOL_PERIOD = "Febrero-Julio 2025";
    private static final String PROJECT_NAME = "Digitalización de procesos administrativos Hospital General";
    private static final String PROJECT_OBJECTIVE = 
        "Implementar un sistema de turnos digital para mejorar la atención al paciente";
    private static final String PROJECT_METHODOLOGY  = "Scrum";
    private static final String AFFILIATED_ORGANIZATION = "Universidad Veracruzana";
    private static final String PROJECT_SUPERVISOR = "Pérez Torres";
    private static final String TOTAL_HOURS = "120";

    private static final String EXPECTED_SUCCESS_MESSAGE = "Reporte generado correctamente.";
    private static final String EXPECTED_EMPTY_RESULT_MESSAGE = "Resultados Obtenidos no puede estar vacío";
    private static final String EXPECTED_EMPTY_OBSSERVATIONS_MESSAGE = "Observaciones Generales no puede estar vacío";
    private static final String EXPECTED_EMPTY_ACTIVITY = "Seleccione una actividad";
    private static final String EXPECTED_NO_NUMERICAL_ADVANCE_MESSAGE  =
        "Porcentaje de Avance de Actividad 1 no puede tener un valor mayor a 100";
    private static final String EXPECTED_INVALID_ADVANCE_MESSAGE = 
        "Porcentaje de Avance de Actividad 1 debe ser un número entero válido";

    private Stage primaryStage;
    private FXMLGeneratePartialReportController partialReportController;
    private ActivityDAO activityDAOMock;
    private ReportDAO reportDAOMock;
    private ReportContextDAO reportContextDAOMock;
    private PartialReportCommon partialReportCommon;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(buildStudent());

        activityDAOMock    = mock(ActivityDAO.class);
        reportDAOMock      = mock(ReportDAO.class);
        reportContextDAOMock = mock(ReportContextDAO.class);
        partialReportCommon  = new PartialReportCommon();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(PARTIAL_REPORT_VIEW_FXML));
        Parent root = loader.load();
        partialReportController = loader.getController();

        try {
            injectControllerField(ACTIVITY_DAO_FIELD, activityDAOMock);
            injectControllerField(REPORT_DAO_FIELD, reportDAOMock);
            injectControllerField(PARTIAL_REPORT_COMMON_FIELD, partialReportCommon);
            injectPartialReportCommonField(REPORT_CONTEXT_DAO_FIELD, reportContextDAOMock);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IOException(
                "Error al inyectar dependencias en el controlador",
                reflectiveOperationException);
        }

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void resetAndPrepare() throws OperationException {
        reset(activityDAOMock, reportDAOMock, reportContextDAOMock);

        when(activityDAOMock.getActivitiesByStudentId(anyString()))
            .thenReturn(buildTwoActivities());
        when(reportDAOMock.registerPartialReport(org.mockito.ArgumentMatchers.any()))
            .thenReturn(true);
        stubReportContext();
        stubActivityLookup();

        interact(() -> clearAllFields());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();

        java.awt.EventQueue.invokeLater(() ->
            Arrays.stream(java.awt.Window.getWindows()).forEach(java.awt.Window::dispose));
    }

    @Test
    void generateReport_activity1Filled_reportContainsActivity1Name() throws Exception {

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(VALID_ACTIVITY_1, capturedReport.getActivityName());
    }

    @Test
    void generateReport_observationsFilled_reportContainsObservations() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(VALID_OBSERVATIONS, capturedReport.getObservations());
    }

    @Test
    void generateReport_resultsFilled_reportContainsResults() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(VALID_RESULTS, capturedReport.getResult());
    }

    @Test
    void generateReport_contextLoaded_reportContainsStudentName() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(STUDENT_NAME, capturedReport.getStudentName());
    }

    @Test
    void generateReport_contextLoaded_reportContainsProfessorName() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(PROFESSOR_NAME, capturedReport.getProfessorName());
    }

    @Test
    void generateReport_contextLoaded_reportContainsProjectName() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        PartialReport capturedReport = captureRegisteredReport();
        assertEquals(PROJECT_NAME, capturedReport.getProjectName());
    }

    @Test
    void generateReport_validForm_showsSuccessMessage() throws Exception {
        stubActivityLookup();

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void generateReport_noActivitiesSelected_showsValidationMessage() {
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATIONS));
        interact(() -> lookup(RESULTS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_RESULTS));

        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_EMPTY_ACTIVITY, messageText());
    }

    @Test
    void generateReport_emptyObservations_showsValidationMessage() {
        stubActivityLookupUnchecked();

        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "1", VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "2", VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "1")
            .queryAs(TextField.class).setText(VALID_ADVANCE_1));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "2")
            .queryAs(TextField.class).setText(VALID_ADVANCE_2));
        interact(() -> lookup(RESULTS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_RESULTS));

        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_EMPTY_OBSSERVATIONS_MESSAGE, messageText());
    }

    @Test
    void generateReport_emptyResults_showsValidationMessage() {
        stubActivityLookupUnchecked();

        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "1", VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "2", VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "1")
            .queryAs(TextField.class).setText(VALID_ADVANCE_1));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "2")
            .queryAs(TextField.class).setText(VALID_ADVANCE_2));
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATIONS));

        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_EMPTY_RESULT_MESSAGE, messageText());
    }

    @Test
    void generateReport_nonNumericAdvance_showsValidationMessage() {
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "1", VALID_ACTIVITY_1));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "1")
            .queryAs(TextField.class).setText("abc"));
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATIONS));
        interact(() -> lookup(RESULTS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_RESULTS));

        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_INVALID_ADVANCE_MESSAGE, messageText());
    }

    @Test
    void generateReport_advanceOver100_showsValidationMessage() {
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "1", VALID_ACTIVITY_1));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "1")
            .queryAs(TextField.class).setText("150"));
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATIONS));
        interact(() -> lookup(RESULTS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_RESULTS));

        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NO_NUMERICAL_ADVANCE_MESSAGE, messageText());
    }

    @Test
    void generateReport_daoThrowsException_showsErrorMessage() throws OperationException {
        stubActivityLookupUnchecked();

        when(reportDAOMock.registerPartialReport(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new OperationException("Error de base de datos",null));

        fillCompleteForm();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        String message = messageText();
        org.junit.jupiter.api.Assertions.assertFalse(message.isBlank(),
            "Se esperaba un mensaje de error pero el label está vacío");
        org.junit.jupiter.api.Assertions.assertNotEquals(EXPECTED_SUCCESS_MESSAGE, message);
    }

    private void injectControllerField(String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = FXMLGeneratePartialReportController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(partialReportController, value);
    }

    private void injectPartialReportCommonField(String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = PartialReportCommon.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(partialReportCommon, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(STUDENT_NAME);
        return student;
    }

    private List<Activity> buildTwoActivities() {
        Activity activityOne = new Activity(
            ACTIVITY_ID_ONE, VALID_ACTIVITY_1, ACTIVITY_DESCRIPTION,
            ACTIVITY_START, ACTIVITY_END, REPORT_ID);
        Activity activityTwo = new Activity(
            ACTIVITY_ID_TWO, VALID_ACTIVITY_2, ACTIVITY_DESCRIPTION,
            ACTIVITY_START, ACTIVITY_END, REPORT_ID);
        return List.of(activityOne, activityTwo);
    }

    private void stubReportContext() throws OperationException {
        PartialReport context = new PartialReport();
        context.setStudentName(STUDENT_NAME);
        context.setProfessorName(PROFESSOR_NAME);
        context.setNrcSubject(NRC_SUBJECT);
        context.setSchoolPeriod(SCHOOL_PERIOD);
        context.setProjectName(PROJECT_NAME);
        context.setProjectObjective(PROJECT_OBJECTIVE);
        context.setProjectMethodology(PROJECT_METHODOLOGY);
        context.setAffiliatedOrganization(AFFILIATED_ORGANIZATION);
        context.setProjectSupervisor(PROJECT_SUPERVISOR);

        when(reportContextDAOMock.getPartialReportContextByStudentId(anyString()))
            .thenReturn(context);
        when(reportContextDAOMock.getTotalReportedHoursByStudentId(anyString()))
            .thenReturn(TOTAL_HOURS);
    }

    private void stubActivityLookup() throws OperationException {
        Activity activityOne = new Activity(
            ACTIVITY_ID_ONE, VALID_ACTIVITY_1, ACTIVITY_DESCRIPTION,
            ACTIVITY_START, ACTIVITY_END, REPORT_ID);
        Activity activityTwo = new Activity(
            ACTIVITY_ID_TWO, VALID_ACTIVITY_2, ACTIVITY_DESCRIPTION,
            ACTIVITY_START, ACTIVITY_END, REPORT_ID);

        when(reportContextDAOMock.getActivityByName(anyString(), eq(VALID_ACTIVITY_1)))
            .thenReturn(activityOne);
        when(reportContextDAOMock.getActivityByName(anyString(), eq(VALID_ACTIVITY_2)))
            .thenReturn(activityTwo);
    }

    private void stubActivityLookupUnchecked() {
        try {
            stubActivityLookup();
        } catch (OperationException operationException) {
            throw new RuntimeException(
                "Error configurando stub de actividades", operationException);
        }
    }

    private PartialReport captureRegisteredReport() throws OperationException {
        ArgumentCaptor<PartialReport> captor = ArgumentCaptor.forClass(PartialReport.class);
        verify(reportDAOMock).registerPartialReport(captor.capture());
        return captor.getValue();
    }

    private void clearAllFields() {
        for (int i = 1; i <= TOTAL_COMBO_FIELDS; i++) {
            lookup(ACTIVITY_COMBO_SELECTOR_PREFIX + i).queryAs(ComboBox.class).getItems().clear();
            lookup(ADVANCE_FIELD_SELECTOR_PREFIX + i).queryAs(TextField.class).clear();
        }
        lookup(GENERAL_OBSERVATIONS_SELECTOR).queryAs(TextArea.class).clear();
        lookup(RESULTS_SELECTOR).queryAs(TextArea.class).clear();
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private void fillCompleteForm() {
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "1", VALID_ACTIVITY_1));
        interact(() -> setComboValue(ACTIVITY_COMBO_SELECTOR_PREFIX + "2", VALID_ACTIVITY_2));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "1")
            .queryAs(TextField.class).setText(VALID_ADVANCE_1));
        interact(() -> lookup(ADVANCE_FIELD_SELECTOR_PREFIX + "2")
            .queryAs(TextField.class).setText(VALID_ADVANCE_2));
        interact(() -> lookup(GENERAL_OBSERVATIONS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_OBSERVATIONS));
        interact(() -> lookup(RESULTS_SELECTOR)
            .queryAs(TextArea.class).setText(VALID_RESULTS));
    }

    @SuppressWarnings("unchecked")
    private void setComboValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        if (!comboBox.getItems().contains(value)) {
            comboBox.getItems().add(value);
        }
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