package utilstest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.utils.SessionManager;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock private Student student;
    @Mock private Professor professor;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @Test
    void getInstance_called_returnsNotNull() {
        assertNotNull(SessionManager.getInstance());
    }

    @Test
    void getInstance_calledTwice_returnsSameInstance() {
        assertSame(SessionManager.getInstance(), SessionManager.getInstance());
    }

    @Test
    void setCurrentStudent_storesStudent_returnsSameStudent() {
        sessionManager.setCurrentStudent(student);

        assertSame(student, sessionManager.getCurrentStudent());
    }

    @Test
    void setCurrentProfessor_storesProfessor_returnsSameProfessor() {
        sessionManager.setCurrentProfessor(professor);

        assertSame(professor, sessionManager.getCurrentProfessor());
    }

    @Test
    void getCurrentCoordinator_noProfessor_returnsEmpty() {
        assertFalse(sessionManager.getCurrentCoordinator().isPresent());
    }

    @Test
    void getCurrentCoordinator_professorNotCoordinator_returnsEmpty() {
        when(professor.getIsCoordinator()).thenReturn(false);
        sessionManager.setCurrentProfessor(professor);

        assertFalse(sessionManager.getCurrentCoordinator().isPresent());
    }

    @Test
    void getCurrentCoordinator_professorIsCoordinator_returnsPresent() {
        when(professor.getIsCoordinator()).thenReturn(true);
        sessionManager.setCurrentProfessor(professor);

        assertTrue(sessionManager.getCurrentCoordinator().isPresent());
    }

    @Test
    void setCurrentCoordinator_validProfessor_marksAsCoordinator() {
        sessionManager.setCurrentCoordinator(professor);

        verify(professor).setIsCoordinator(true);
    }

    @Test
    void setCurrentCoordinator_nullProfessor_storesNull() {
        sessionManager.setCurrentCoordinator(null);

        assertNull(sessionManager.getCurrentProfessor());
    }

    @Test
    void clearSession_withActiveStudent_clearsStudent() {
        sessionManager.setCurrentStudent(student);

        sessionManager.clearSession();

        assertNull(sessionManager.getCurrentStudent());
    }

    @Test
    void clearSession_withActiveProfessor_clearsProfessor() {
        sessionManager.setCurrentProfessor(professor);

        sessionManager.clearSession();

        assertNull(sessionManager.getCurrentProfessor());
    }
}