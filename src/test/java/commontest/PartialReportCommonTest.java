package commontest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import uv.lis.logic.common.PartialReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;
import uv.lis.logic.utils.WorkProgressCalculator;

@ExtendWith(MockitoExtension.class)
class PartialReportCommonTest {

    private static final String STUDENT_ID = "S23013127";
    private static final String ACTIVITY_NAME = "Desarrollo de modulo";
    private static final String STUDENT_NAME = "Ana Gomez Ramirez";
    private static final String PROFESSOR_NAME = "Alberto Lopez Hernandez";
    private static final String SUBJECT_NRC = "12345";
    private static final String SCHOOL_PERIOD = "Febrero-Julio 2026";
    private static final String PROJECT_NAME = "Sistema de Practicas";
    private static final String TOTAL_HOURS = "120";
    private static final String CONTEXT_ERROR = "Error al obtener el contexto";
    private static final int PLANNED_ADVANCE = 5;

    @Mock private ReportContextDAO reportContextDAO;
    @Mock private Student currentStudent;
    @Mock private PartialReport partialReport;
    @Mock private Activity activity;
    @Mock private JasperPrint jasperPrint;

    private PartialReportCommon partialReportCommon;

    @BeforeEach
    void setUp() throws Exception {
        partialReportCommon = new PartialReportCommon();
        Field field = PartialReportCommon.class.getDeclaredField("reportContextDAO");
        field.setAccessible(true);
        field.set(partialReportCommon, reportContextDAO);
    }

    private PartialReport builderContext() {
        PartialReport context = new PartialReport();
        context.setStudentName(STUDENT_NAME);
        context.setProfessorName(PROFESSOR_NAME);
        context.setNrcSubject(SUBJECT_NRC);
        context.setSchoolPeriod(SCHOOL_PERIOD);
        context.setProjectName(PROJECT_NAME);
        return context;
    }

    private PartialReport builderFullReport() {
        PartialReport report = new PartialReport();
        report.setActivityName(ACTIVITY_NAME);
        return report;
    }

    private SessionManager mockSessionWithStudent(MockedStatic<SessionManager> mockedSession, Student student) {
        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.getCurrentStudent()).thenReturn(student);
        mockedSession.when(SessionManager::getInstance).thenReturn(sessionManager);
        return sessionManager;
    }

    @Test
    void generatePartialReport_noStudentInSession_throwsOperationException() throws Exception {
        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSessionWithStudent(mockedSession, null);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(partialReport));
        }
    }

    @Test
    void generatePartialReport_contextDaoFails_throwsOperationException() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID))
            .thenThrow(new OperationException(CONTEXT_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSessionWithStudent(mockedSession, currentStudent);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(partialReport));
        }
    }

    @Test
    void generatePartialReport_activityNotFound_throwsOperationException() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID))
            .thenReturn(builderContext());
        when(reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID))
            .thenReturn(TOTAL_HOURS);
        when(partialReport.getActivityName()).thenReturn(ACTIVITY_NAME);
        when(reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME)).thenReturn(null);

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSessionWithStudent(mockedSession, currentStudent);

            assertThrows(OperationException.class,
                () -> partialReportCommon.generatePartialReport(partialReport));
        }
    }

    @Test
    void generatePartialReport_validData_returnsJasperPrint() throws Exception {
        PartialReport report = builderFullReport();
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID))
            .thenReturn(builderContext());
        when(reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID))
            .thenReturn(TOTAL_HOURS);
        when(reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME)).thenReturn(activity);

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class);
            MockedStatic<WorkProgressCalculator> mockedCalculator 
                = mockStatic(WorkProgressCalculator.class);
            MockedStatic<JasperFillManager> mockedFillManager = mockStatic(JasperFillManager.class)) {

                mockSessionWithStudent(mockedSession, currentStudent);
                mockedCalculator.when(
                    () -> WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity))
                    .thenReturn(PLANNED_ADVANCE);
                mockedFillManager.when(() -> JasperFillManager.fillReport(
                    any(InputStream.class), anyMap(), any(JRDataSource.class))).thenReturn(jasperPrint);

                assertNotNull(partialReportCommon.generatePartialReport(report));
            }
    }
}