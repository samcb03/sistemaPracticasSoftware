package commontest;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.logic.common.MonthlyReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

@ExtendWith(MockitoExtension.class)
class MonthlyReportCommonTest {

    @Mock private ReportContextDAO reportContextDAO;
    @Mock private ReportDAO reportDAO;
    @Mock private SessionManager sessionManager;

    private MonthlyReportCommon monthlyReportCommon;

    @BeforeEach
    void setUp() throws Exception {
        monthlyReportCommon = new MonthlyReportCommon();
        Field contextField = MonthlyReportCommon.class.getDeclaredField("reportContextDAO");
        contextField.setAccessible(true);
        contextField.set(monthlyReportCommon, reportContextDAO);

        Field reportField = MonthlyReportCommon.class.getDeclaredField("reportDAO");
        reportField.setAccessible(true);
        reportField.set(monthlyReportCommon, reportDAO);
    }

    @Test
    void generateMonthlyReport_noStudentInSession_throwsOperationException() {
        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(null);

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_contextDAOFails_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent();

        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);
            when(reportContextDAO.getMonthlyReportData(anyString()))
                .thenThrow(new OperationException("Error al obtener contexto", null));

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_totalHoursNotNumeric_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent();

        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);
            when(reportContextDAO.getMonthlyReportData(anyString()))
                .thenReturn(buildContextMonthlyReport());
            when(reportContextDAO.getTotalReportedHoursByStudentId(anyString()))
                .thenReturn("valorNoNumerico");

            assertThrows(NumberFormatException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    @Test
    void generateMonthlyReport_reportDAOFails_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent();

        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);
            when(reportContextDAO.getMonthlyReportData(anyString()))
                .thenReturn(buildContextMonthlyReport());
            when(reportContextDAO.getTotalReportedHoursByStudentId(anyString()))
                .thenReturn("80");
            when(reportDAO.registerMonthlyReport(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new OperationException("Error al registrar reporte", null));

            assertThrows(OperationException.class,
                () -> monthlyReportCommon.generateMonthlyReport(buildMonthlyReport()));
        }
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent("S23013127");
        student.setFirstName("Ana");
        student.setLastName("Gomez Ramirez");
        return student;
    }

    private MonthlyReport buildMonthlyReport() {
        MonthlyReport report = new MonthlyReport();
        report.setStudentId("S23013127");
        report.setReportedHours(40);
        report.addActivityEntry("Febrero-Julio 2026", "Actividad 1", "Observacion 1");
        return report;
    }

    private MonthlyReport buildContextMonthlyReport() {
        MonthlyReport context = new MonthlyReport();
        context.setStudentName("Ana Gomez Ramirez");
        context.setCoordinatorName("Carla Ruiz Gonzales");
        context.setMonth("Mayo");
        context.setReportNumber(6);
        context.setBlock("A");
        context.setSection("02");
        context.setPeriod("Febrero-Julio 2026");
        context.setProfessorName("Alberto Lopez Hernandez");
        return context;
    }
}