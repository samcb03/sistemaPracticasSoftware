package commontest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import net.sf.jasperreports.engine.JasperPrint;

import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;
import uv.lis.logic.utils.WorkProgressCalculator;

class PartialReportCommonTest {

    private static final String STUDENT_ID    = "S23013127";
    private static final String ACTIVITY_NAME = "Desarrollo de modulo";
    private static final String STUDENT_NAME  = "Ana Gomez Ramirez";
    private static final String PROFESSOR_NAME = "Alberto Lopez Hernandez";
    private static final String SUBJECT_NRC   = "12345";
    private static final String SCHOOL_PERIOD = "Febrero-Julio 2026";
    private static final String PROJECT_NAME  = "Sistema de Practicas";
    private static final String TOTAL_HOURS   = "120";
    private static final String CONTEXT_ERROR = "Error al obtener el contexto";
    private static final int    PLANNED_ADVANCE = 5;

    @Mock private ReportContextDAO reportContextDAO;
    @Mock private Student currentStudent;
    @Mock private Activity activity;
    @Mock private JasperPrint jasperPrint;

    private PartialReportCommon partialReportCommon;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        partialReportCommon = new PartialReportCommon();
        Field field = PartialReportCommon.class.getDeclaredField("reportContextDAO");
        field.setAccessible(true);
        field.set(partialReportCommon, reportContextDAO);
    }

    private PartialReport buildContext() {
        PartialReport context = new PartialReport();
        context.setStudentName(STUDENT_NAME);
        context.setProfessorName(PROFESSOR_NAME);
        context.setNrcSubject(SUBJECT_NRC);
        context.setSchoolPeriod(SCHOOL_PERIOD);
        context.setProjectName(PROJECT_NAME);
        return context;
    }

    private PartialReport buildReportWithActivity() {
        PartialReport report = new PartialReport();
        report.getActivityNames()[0] = ACTIVITY_NAME;
        return report;
    }

    private PartialReportCommon buildPartialReportCommonWithMockedTemplateAndDAO() throws Exception {
        PartialReportCommon partialReportCommonSpy = spy(new PartialReportCommon());

        doReturn(jasperPrint)
            .when(partialReportCommonSpy).
            fillReportTemplate(any(PartialReport.class));

        Field reportContextDAOField = PartialReportCommon.class.getDeclaredField("reportContextDAO");
        reportContextDAOField.setAccessible(true);
        reportContextDAOField.set(partialReportCommonSpy, reportContextDAO);

        return partialReportCommonSpy;
    }

    private void mockStudentContext() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID))
            .thenReturn(buildContext());
        when(reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID))
            .thenReturn(TOTAL_HOURS);
    }

    private void mockSession(MockedStatic<SessionManager> mockedSession, Student student) {
        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.getCurrentStudent()).thenReturn(student);
        mockedSession.when(SessionManager::getInstance).thenReturn(sessionManager);
    }

    @Test
    void generatePartialReport_noStudentInSession_throwsOperationException() throws Exception {
        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, null);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(new PartialReport()));
        }
    }

    @Test
    void generatePartialReport_contextDaoFails_throwsOperationException() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID))
            .thenThrow(new OperationException(CONTEXT_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, currentStudent);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(new PartialReport()));
        }
    }

    @Test
    void generatePartialReport_activityNotFound_throwsOperationException() throws Exception {
        mockStudentContext();
        when(reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME))
            .thenReturn(null);

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, currentStudent);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(buildReportWithActivity()));
        }
    }

    @Test
    void generatePartialReport_validData_returnsJasperPrint() throws Exception {
        mockStudentContext();
        when(reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME))
            .thenReturn(activity);

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class);
             MockedStatic<WorkProgressCalculator> mockedCalculator
                 = mockStatic(WorkProgressCalculator.class)) {

            mockSession(mockedSession, currentStudent);

            mockedCalculator.when(
                () -> WorkProgressCalculator.calculateActivityWeeks(activity))
                .thenReturn(PLANNED_ADVANCE);
            mockedCalculator.when(
                () -> WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity))
                .thenReturn(PLANNED_ADVANCE);

            assertNotNull(buildPartialReportCommonWithMockedTemplateAndDAO()
                .generatePartialReport(buildReportWithActivity()));
        }
    }
}