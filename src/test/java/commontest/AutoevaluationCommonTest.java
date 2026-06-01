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

import uv.lis.logic.common.AutoevaluationCommon;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

@ExtendWith(MockitoExtension.class)
class AutoevaluationCommonTest {

    @Mock private AutoevaluationDAO autoevaluationDAO;
    @Mock private SessionManager sessionManager;

    private AutoevaluationCommon autoevaluationCommon;

    @BeforeEach
    void setUp() throws Exception {
        autoevaluationCommon = new AutoevaluationCommon();
        Field field = AutoevaluationCommon.class.getDeclaredField("autoevaluationDAO");
        field.setAccessible(true);
        field.set(autoevaluationCommon, autoevaluationDAO);
    }

    @Test
    void generateAutoevaluation_noStudentInSession_throwsOperationException() {
        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(null);

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_scoresOutOfRange_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent();

        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);

            Autoevaluation contextAutoevaluation = buildValidAutoevaluation();
            when(autoevaluationDAO.getAutoevaluationData(anyString()))
                .thenReturn(contextAutoevaluation);

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildInvalidRangeAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_studentAlreadyEvaluated_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent(); 
        Autoevaluation contextAutoevaluation = buildValidAutoevaluation();
        contextAutoevaluation.setIdStudent("S23013127");

        try (MockedStatic<SessionManager> sessionManagerMocked = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);

            when(autoevaluationDAO.getAutoevaluationData("S23013127"))
                    .thenReturn(contextAutoevaluation);
            
            when(autoevaluationDAO.existsByStudent("S23013127")).thenReturn(true);

            assertThrows(OperationException.class,
                    () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }

    @Test
    void generateAutoevaluation_contextDAOFails_throwsOperationException() throws Exception {
        Student mockStudent = buildStudent();

        try (MockedStatic<SessionManager> sessionManagerMocked
                = Mockito.mockStatic(SessionManager.class)) {
            sessionManagerMocked.when(SessionManager::getInstance).thenReturn(sessionManager);
            when(sessionManager.getCurrentStudent()).thenReturn(mockStudent);
            when(autoevaluationDAO.getAutoevaluationData(anyString()))
                .thenThrow(new OperationException("Error al obtener datos", null));

            assertThrows(OperationException.class,
                () -> autoevaluationCommon.generateAutoevaluation(buildValidAutoevaluation()));
        }
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent("S23013127");
        student.setFirstName("Ana");
        student.setLastName("Gomez Ramirez");
        return student;
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
}