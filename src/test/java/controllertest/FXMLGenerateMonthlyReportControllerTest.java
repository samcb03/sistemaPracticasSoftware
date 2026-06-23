package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLGenerateMonthlyReportController;
import uv.lis.logic.common.MonthlyReportCommon;
import uv.lis.logic.dao.AdvanceDAO;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateMonthlyReportControllerTest extends ApplicationTest {

    private static final String MONTHLY_REPORT_VIEW_FXML = "/uv/lis/GUI/view/FXMLGenerateMonthlyReport.fxml";
    private static final String REPORT_CONTEXT_DAO_FIELD = "reportContextDAO";
    private static final String ADVANCE_DAO_FIELD = "advanceDAO";
    private static final String MONTHLY_REPORT_COMMON_FIELD = "monthlyReportCommon";

    private static final String MONTH_COMBO_SELECTOR = "#comboBoxMonth";
    private static final String GENERATE_BUTTON_SELECTOR = "#buttonGenerate";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_STUDENT_NAME = "Denisse";
    private static final String VALID_STUDENT_LASTNAME = "Reyes";
    private static final String SELECTED_MONTH = "Mayo";
    private static final String ACTIVITY_NAME = "Analisis de requerimientos";
    private static final String ACTIVITY_DESCRIPTION = "Levantamiento de requerimientos del sistema";

    private static final String EXPECTED_NO_MONTH_MESSAGE = "Seleccione un mes antes de generar el reporte.";
    private static final String EXPECTED_NO_ACTIVITY_MESSAGE = "El reporte debe contener mínimo una actividad";
    private static final String EXPECTED_ACCUMULATED_LIMIT_MESSAGE =
        "Has alcanzado el límite de 420 horas acumuladas. No es posible generar más reportes";
    private static final String EXPECTED_DUPLICATED_MESSAGE = "Ya se ha generado un reporte para el mes de Mayo";

    private static final int CONTEXT_PROJECT_ID = 7;
    private static final int CONTEXT_REPORT_ID = 11;
    private static final int CONTEXT_REPORT_NUMBER = 1;
    private static final int NORMAL_REPORTED_HOURS = 40;
    private static final int NORMAL_ACCUMULATED_HOURS = 100;
    private static final int EXCEED_LIMIT_ACCUMULATED_HOURS = 421;
    private static final int REPORT_PAGE_WIDTH = 595;
    private static final int REPORT_PAGE_HEIGHT = 842;

    private Stage primaryStage;
    private FXMLGenerateMonthlyReportController monthlyReportController;
    private ReportContextDAO reportContextDAOMock;
    private AdvanceDAO advanceDAOMock;
    private MonthlyReportCommon monthlyReportCommonMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(buildStudent());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(MONTHLY_REPORT_VIEW_FXML));
        Parent root = loader.load();
        monthlyReportController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws OperationException, ReflectiveOperationException {
        reportContextDAOMock = mock(ReportContextDAO.class);
        advanceDAOMock = mock(AdvanceDAO.class);
        monthlyReportCommonMock = mock(MonthlyReportCommon.class);

        when(reportContextDAOMock.getMonthlyReportData(anyString())).thenReturn(buildContext());
        when(reportContextDAOMock.getRecordedActivitiesByMonth(anyInt(), anyInt(), anyInt()))
            .thenReturn(List.of(buildActivity()));
        when(reportContextDAOMock.getSumOfReportedHours(anyInt(), anyInt(), anyInt()))
            .thenReturn(NORMAL_REPORTED_HOURS);
        when(reportContextDAOMock.getTotalReportedHoursByStudentId(anyString()))
            .thenReturn(String.valueOf(NORMAL_ACCUMULATED_HOURS));

        injectField(REPORT_CONTEXT_DAO_FIELD, reportContextDAOMock);
        injectField(ADVANCE_DAO_FIELD, advanceDAOMock);
        injectField(MONTHLY_REPORT_COMMON_FIELD, monthlyReportCommonMock);

        interact(() -> resetMessage());
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
    void validateMonthlyReport_noMonthSelected_showsValidationError() {
        clickGenerate();

        assertEquals(EXPECTED_NO_MONTH_MESSAGE, messageText());
    }

    @Test
    void validateMonthlyReport_noActivities_showsValidationError() throws Exception {
        when(reportContextDAOMock.getRecordedActivitiesByMonth(anyInt(), anyInt(), anyInt()))
            .thenReturn(List.of());

        selectMonth();
        clickGenerate();

        assertEquals(EXPECTED_NO_ACTIVITY_MESSAGE, messageText());
    }

    @Test
    void validateMonthlyReport_accumulatedLimitReached_showsValidationError() throws Exception {
        when(reportContextDAOMock.getTotalReportedHoursByStudentId(anyString()))
            .thenReturn(String.valueOf(EXCEED_LIMIT_ACCUMULATED_HOURS));

        selectMonth();
        clickGenerate();

        assertEquals(EXPECTED_ACCUMULATED_LIMIT_MESSAGE, messageText());
    }

    @Test
    void validateMonthlyReport_duplicatedReport_showsValidationError() throws Exception {
        when(reportContextDAOMock.hasReportAlreadyBeenGenerated(anyString(), anyString())).thenReturn(true);

        selectMonth();
        clickGenerate();

        assertEquals(EXPECTED_DUPLICATED_MESSAGE, messageText());
    }

    @Test
    void validateMonthlyReport_validData_registersAdvance() throws Exception {
        when(reportContextDAOMock.hasReportAlreadyBeenGenerated(anyString(), anyString())).thenReturn(false);
        when(monthlyReportCommonMock.generateMonthlyReport(any())).thenReturn(buildJasperPrint());
        when(advanceDAOMock.existsAdvanceForReport(anyInt())).thenReturn(false);
        when(advanceDAOMock.registerAdvance(any())).thenReturn(true);

        selectMonth();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();

        verify(advanceDAOMock).registerAdvance(any());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLGenerateMonthlyReportController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(monthlyReportController, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_STUDENT_NAME);
        student.setLastName(VALID_STUDENT_LASTNAME);
        return student;
    }

    private MonthlyReport buildContext() {
        MonthlyReport context = new MonthlyReport();
        context.setIdProject(CONTEXT_PROJECT_ID);
        context.setIdReport(CONTEXT_REPORT_ID);
        context.setReportNumber(CONTEXT_REPORT_NUMBER);
        return context;
    }

    private Activity buildActivity() {
        Activity activity = new Activity();
        activity.setName(ACTIVITY_NAME);
        activity.setDescription(ACTIVITY_DESCRIPTION);
        return activity;
    }

    private JasperPrint buildJasperPrint() {
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("ReporteMensualPrueba");
        jasperPrint.setPageWidth(REPORT_PAGE_WIDTH);
        jasperPrint.setPageHeight(REPORT_PAGE_HEIGHT);
        jasperPrint.addPage(new JRBasePrintPage());
        return jasperPrint;
    }

    @SuppressWarnings("unchecked")
    private void selectMonth() {
        interact(() -> {
            ComboBox<String> comboBox = lookup(MONTH_COMBO_SELECTOR).queryAs(ComboBox.class);
            comboBox.getItems().setAll(SELECTED_MONTH);
            comboBox.getSelectionModel().select(SELECTED_MONTH);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void resetMessage() {
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private void clickGenerate() {
        clickOn(GENERATE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }
}