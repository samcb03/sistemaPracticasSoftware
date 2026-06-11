package commontest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import net.sf.jasperreports.engine.JasperPrint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import uv.lis.logic.common.FinalReportCommon;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

class FinalReportCommonTest {

    private static final String STUDENT_ID = "S23013127";
    private static final String STUDENT_NAME  = "Ana Gomez Ramirez";
    private static final String PROFESSOR_NAME = "Alberto Lopez Hernandez";
    private static final String SUBJECT_NRC = "12345";
    private static final String SCHOOL_PERIOD = "Febrero-Julio 2026";
    private static final String PROJECT_NAME = "Sistema de Practicas";
    private static final String TOTAL_HOURS = "240";
    private static final String CONTEXT_ERROR = "Error al obtener el contexto";

    @Mock private ReportContextDAO reportContextDAO;
    @Mock private Student currentStudent;
    @Mock private FinalReport finalReport;

    private FinalReportCommon finalReportCommon;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        finalReportCommon = new FinalReportCommon();

        Field field = FinalReportCommon.class.getDeclaredField("reportContextDAO");
        field.setAccessible(true);
        field.set(finalReportCommon, reportContextDAO);
    }

    private FinalReport buildContext() {
        FinalReport context = new FinalReport();
        context.setStudentName(STUDENT_NAME);
        context.setProfessorName(PROFESSOR_NAME);
        context.setNrcSubject(SUBJECT_NRC);
        context.setSchoolPeriod(SCHOOL_PERIOD);
        context.setProjectName(PROJECT_NAME);
        return context;
    }

    private void mockSession(MockedStatic<SessionManager> mockedSession, Student student) {
        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.getCurrentStudent()).thenReturn(student);
        mockedSession.when(SessionManager::getInstance).thenReturn(sessionManager);
    }

    @Test
    void generateFinalReport_noStudentInSession_throwsOperationException() throws Exception {
        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, null);

            assertThrows(OperationException.class,
                () -> finalReportCommon.generateFinalReport(finalReport));
        }
    }

    @Test
    void generateFinalReport_contextDaoFails_throwsOperationException() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getFinalReportContextByStudentId(STUDENT_ID))
            .thenThrow(new OperationException(CONTEXT_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, currentStudent);

            assertThrows(OperationException.class,
                () -> finalReportCommon.generateFinalReport(finalReport));
        }
    }

    @Test
    void generateFinalReport_validData_returnsJasperPrint() throws Exception {
        when(currentStudent.getIdStudent()).thenReturn(STUDENT_ID);
        when(reportContextDAO.getFinalReportContextByStudentId(STUDENT_ID))
            .thenReturn(buildContext());
        when(reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID))
            .thenReturn(TOTAL_HOURS);

        try (MockedStatic<SessionManager> mockedSession = mockStatic(SessionManager.class)) {
            mockSession(mockedSession, currentStudent);

            JasperPrint result = finalReportCommon.generateFinalReport(new FinalReport());

            assertNotNull(result);
        }
    }
}