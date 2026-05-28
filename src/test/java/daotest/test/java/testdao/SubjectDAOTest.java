package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class SubjectDAOTest {

    private static final int EXPECTED_NRC = 12345;
    private static final int SECOND_NRC = 67890;
    private static final int SCHOOL_PERIOD_ID = 1;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final String VALID_STUDENT_ID = "S23013127";
    private static final String INVALID_STUDENT_ID = "S999999999";
    private static final String PERSONNEL_NUMBER = "UV-001";
    private static final String SUBJECT_NAME = "Practicas Profesionales";
    private static final String CONNECTION_ERROR = "Fallo de conexión";
    private static final String NO_SUBJECT_MESSAGE = "No tiene asignada una experiencia";
    private static final int FIRST_INSERT_SUCCESS = 1;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private SubjectDAO subjectDAO;

    @BeforeEach
    void setUp() throws Exception {
        subjectDAO = new SubjectDAO(connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Subject builderSubject() {
        Subject subject = new Subject();
        subject.setNrc(EXPECTED_NRC);
        subject.setSchoolPeriodId(SCHOOL_PERIOD_ID);
        subject.setProfessorPersonnelNumber(PERSONNEL_NUMBER);
        return subject;
    }

    @Test
    void registerSubject_bothInsertsSuccessful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1, 1);

        assertTrue(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_firstInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0, 1);

        assertFalse(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_secondInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1, 0);

        assertFalse(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_secondInsertThrows_rollsBack() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate())
            .thenReturn(FIRST_INSERT_SUCCESS)
            .thenThrow(new SQLException("Error en segundo insert"));

        assertThrows(OperationException.class,
            () -> subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_executeUpdateThrows_invokesRollback() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Error en insert"));

        try {
            subjectDAO.registerSubject(builderSubject());
        } catch (OperationException operationException) {
            verify(databaseConnection).rollback();
        }
    }

    @Test
    void registerSubject_preparedStatementError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Error al preparar"));

        assertThrows(OperationException.class,
            () -> subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void getAllSubjectsNRCName_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("NRC")).thenReturn(EXPECTED_NRC, SECOND_NRC);
        when(resultSet.getString("nombreExperiencia"))
            .thenReturn(SUBJECT_NAME, SUBJECT_NAME);

        assertEquals(EXPECTED_LIST_SIZE, subjectDAO.getAllSubjectsNRCName().size());
    }

    @Test
    void getAllSubjectsNRCName_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getAllSubjectsNRCName().isEmpty());
    }

    @Test
    void getAllSubjectsNRCName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> subjectDAO.getAllSubjectsNRCName());
    }

    @Test
    void assignStudentToSubject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC));
    }

    @Test
    void assignStudentToSubject_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertFalse(subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC));
    }

    @Test
    void assignStudentToSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC));
    }

    @Test
    void getSubjectNRCByStudentID_found_returnsNRC() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("NRC")).thenReturn(String.valueOf(EXPECTED_NRC));

        assertEquals(String.valueOf(EXPECTED_NRC),
            subjectDAO.getSubjectNRCByStudentID(VALID_STUDENT_ID));
    }

    @Test
    void getSubjectNRCByStudentID_notFound_returnsDefaultMessage() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertEquals(NO_SUBJECT_MESSAGE,
            subjectDAO.getSubjectNRCByStudentID(INVALID_STUDENT_ID));
    }

    @Test
    void getSubjectNRCByStudentID_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getSubjectNRCByStudentID(VALID_STUDENT_ID));
    }

    @Test
    void unassignProfessorFromSubject_successful_doesNotThrow() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> subjectDAO.unassignProfessorFromSubject(PERSONNEL_NUMBER));
    }

    @Test
    void unassignProfessorFromSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.unassignProfessorFromSubject(PERSONNEL_NUMBER));
    }

    @Test
    void getSubjectsByProfessor_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("nrc")).thenReturn(EXPECTED_NRC, SECOND_NRC);
        when(resultSet.getString("nombreExperiencia"))
            .thenReturn(SUBJECT_NAME, SUBJECT_NAME);
        when(resultSet.getString("nombre")).thenReturn("Febrero-Julio 2026");
        when(resultSet.getInt("idPeriodoEscolar")).thenReturn(SCHOOL_PERIOD_ID);

        ArrayList<Subject> result = subjectDAO.getSubjectsByProfessor(PERSONNEL_NUMBER);

        assertEquals(EXPECTED_LIST_SIZE, result.size());
    }

    @Test
    void getSubjectsByProfessor_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getSubjectsByProfessor(PERSONNEL_NUMBER).isEmpty());
    }

    @Test
    void getSubjectsByProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getSubjectsByProfessor(PERSONNEL_NUMBER));
    }

    @Test
    void getEnrolledStudentsBySubject_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("matricula")).thenReturn(VALID_STUDENT_ID, "S23013128");
        when(resultSet.getString("nombre")).thenReturn("Ana", "Luis");
        when(resultSet.getString("apellidos")).thenReturn("Gomez", "Martinez");

        ArrayList<Student> result = subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC);

        assertEquals(EXPECTED_LIST_SIZE, result.size());
    }

    @Test
    void getEnrolledStudentsBySubject_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC).isEmpty());
    }

    @Test
    void getEnrolledStudentsBySubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC));
    }
}