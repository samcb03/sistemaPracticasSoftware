package commontest;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import net.sf.jasperreports.engine.JasperPrint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uv.lis.logic.common.MonthlyReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

class MonthlyReportCommonTest {

    private static final String STUDENT_ID = "S23013127";
    private static final String STUDENT_NAME = "Ana";
    private static final String STUDENT_LAST_NAME = "Gomez Ramirez";
    private static final String COORDINATOR_NAME = "Carla Ruiz Gonzales";
    private static final String PROFESSOR_NAME = "Alberto Lopez Hernandez";
    private static final String SCHOOL_PERIOD = "Febrero-Julio 2026";
    private static final String ACTIVITY_NAME = "Actividad 1";
    private static final String OBSERVATION = "Observacion 1";
    private static final String MONTH = "Mayo";
    private static final String BLOCK = "Bloque 1";
    private static final String SECTION = "02";
    private static final String CONTEXT_ERROR = "Error al obtener contexto";
    private static final String REGISTER_ERROR = "Error al registrar reporte";
    private static final int REPORTED_HOURS = 40;
    private static final int REPORT_NUMBER = 6;

    @Mock private ReportContextDAO reportContextDAO;
    @Mock private ReportDAO reportDAO;
    @Mock private SessionManager sessionManager;

    private MonthlyReportCommon monthlyReportCommon;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        monthlyReportCommon = new MonthlyReportCommon();

        Field contextField = MonthlyReportCommon.class.getDeclaredField("reportContextDAO");
        contextField.setAccessible(true);
        contextField.set(monthlyReportCommon, reportContextDAO);

        Field reportField = MonthlyReportCommon.class.getDeclaredField("reportDAO");
        reportField.setAccessible(true);
        reportField.set(monthlyReportCommon, reportDAO);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(STUDENT_ID);
        student.setFirstName(STUDENT_NAME);
        student.setLastName(STUDENT_LAST_NAME);
        return student;
    }

    private MonthlyReport buildMonthlyReport() {
        MonthlyReport report = new MonthlyReport();
        report.setStudentId(STUDENT_ID);
        report.setReportedHours(REPORTED_HOURS);
        report.addActivityEntry(SCHOOL_PERIOD, ACTIVITY_NAME, OBSERVATION);
        return report;
    }

    private MonthlyReport buildContextMonthlyReport() {
        MonthlyReport context = new MonthlyReport();
        context.setStudentName(STUDENT_NAME);
        context.setCoordinatorName(COORDINATOR_NAME);
        context.setMonth(MONTH);
        context.setReportNumber(REPORT_NUMBER);
        context.setBlock(BLOCK);
        context.setSection(SECTION);
        context.setPeriod(SCHOOL_PERIOD);
        context.setProfessorName(PROFESSOR_NAME);
        return context;
    }

    private void mockSession(MockedStatic<SessionManager> mockedSession, Student student) {
        mockedSession.when(SessionManager::getInstance).thenReturn(sessionManager);
        when(sessionManager.getCurrentStudent()).thenReturn(student);
    }

    @Test
    void generateMonthlyReport_noStudentInSession_throwsOperationException() {
        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, null);

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_contextDAOFails_throwsOperationException() throws Exception {
        when(reportContextDAO.getMonthlyReportData(anyString()))
            .thenThrow(new OperationException(CONTEXT_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_reportDAOFails_throwsOperationException() throws Exception {
        when(reportContextDAO.getMonthlyReportData(anyString()))
            .thenReturn(buildContextMonthlyReport());
        when(reportDAO.registerMonthlyReport(any()))
            .thenThrow(new OperationException(REGISTER_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_validData_returnsJasperPrint() throws Exception {
        when(reportContextDAO.getMonthlyReportData(anyString()))
            .thenReturn(buildContextMonthlyReport());

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            JasperPrint result = monthlyReportCommon.generateMonthlyReport(buildMonthlyReport());

            assertNotNull(result);
        }
    }
}