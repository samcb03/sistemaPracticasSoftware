package commontest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uv.lis.logic.common.AutoevaluationCommon;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

class AutoevaluationCommonTest {

    private static final String STUDENT_ID = "S23013127";
    private static final String STUDENT_NAME = "Ana";
    private static final String STUDENT_LAST_NAME = "Gomez Ramirez";
    private static final String CONTEXT_ERROR = "Error al obtener datos";

    @Mock private AutoevaluationDAO autoevaluationDAO;
    @Mock private SessionManager sessionManager;

    private AutoevaluationCommon autoevaluationCommon;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        autoevaluationCommon = new AutoevaluationCommon();

        Field field = AutoevaluationCommon.class.getDeclaredField("autoevaluationDAO");
        field.setAccessible(true);
        field.set(autoevaluationCommon, autoevaluationDAO);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(STUDENT_ID);
        student.setFirstName(STUDENT_NAME);
        student.setLastName(STUDENT_LAST_NAME);
        return student;
    }

    private void mockSession(MockedStatic<SessionManager> mockedSession, Student student) {
        mockedSession.when(SessionManager::getInstance).thenReturn(sessionManager);
        when(sessionManager.getCurrentStudent()).thenReturn(student);
    }

    private Autoevaluation buildValidAutoevaluation() {
        Autoevaluation autoevaluation = new Autoevaluation();
        autoevaluation.setProductiveParticipation(4);
        autoevaluation.setAppliedKnowledge(5);
        autoevaluation.setConfidenceInActivities(3);
        autoevaluation.setActivitiesInterest(4);
        autoevaluation.setOrganizationSupport(5);
        autoevaluation.setRulesAwareness(3);
        autoevaluation.setSupervisorGuidance(4);
        autoevaluation.setEffectiveMonitoring(5);
        autoevaluation.setCareerAlignment(3);
        autoevaluation.setInternshipImportance(4);
        return autoevaluation;
    }

    private Autoevaluation buildInvalidRangeAutoevaluation() {
        Autoevaluation autoevaluation = new Autoevaluation();
        autoevaluation.setProductiveParticipation(0);
        autoevaluation.setAppliedKnowledge(5);
        autoevaluation.setConfidenceInActivities(3);
        autoevaluation.setActivitiesInterest(4);
        autoevaluation.setOrganizationSupport(5);
        autoevaluation.setRulesAwareness(3);
        autoevaluation.setSupervisorGuidance(4);
        autoevaluation.setEffectiveMonitoring(5);
        autoevaluation.setCareerAlignment(3);
        autoevaluation.setInternshipImportance(4);
        return autoevaluation;
    }

    @Test
    void generateAutoevaluation_noStudentInSession_throwsOperationException() {
        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, null);

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_contextDAOFails_throwsOperationException() throws Exception {
        when(autoevaluationDAO.getAutoevaluationData(anyString()))
            .thenThrow(new OperationException(CONTEXT_ERROR, null));

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_scoresOutOfRange_throwsOperationException() throws Exception {
        when(autoevaluationDAO.getAutoevaluationData(anyString()))
            .thenReturn(buildValidAutoevaluation());

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildInvalidRangeAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_studentAlreadyEvaluated_throwsOperationException() throws Exception {
        Autoevaluation context = buildValidAutoevaluation();
        context.setIdStudent(STUDENT_ID);

        when(autoevaluationDAO.getAutoevaluationData(STUDENT_ID)).thenReturn(context);
        when(autoevaluationDAO.existsByStudent(STUDENT_ID)).thenReturn(true);

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class)) {
            mockSession(mockedSession, buildStudent());

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }
}