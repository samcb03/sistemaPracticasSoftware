package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

class SubjectDAOTest {

    private static final int EXPECTED_NRC = 12345;
    private static final int SECOND_NRC = 67890;
    private static final int SCHOOL_PERIOD_ID = 3;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS_AFFECTED = 0;

    private static final String VALID_STUDENT_ID = "S23013127";
    private static final String SECOND_STUDENT_ID = "S23013128";
    private static final String INVALID_STUDENT_ID = "S999999999";
    private static final String PERSONNEL_NUMBER = "UV-001";
    private static final int VALID_PERIOD_ID = 3;
    private static final String SUBJECT_NAME = "Practicas Profesionales";
    private static final String SCHOOL_PERIOD_NAME = "Febrero-Julio 2026";
    private static final String CONNECTION_ERROR = "Fallo de conexión";
    private static final String NO_SUBJECT_MESSAGE = "No tiene asignada una experiencia";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String FIRST_STUDENT_NAME = "Ana";
    private static final String SECOND_STUDENT_NAME = "Luis";
    private static final String FIRST_STUDENT_LAST_NAME = "Gomez";
    private static final String SECOND_STUDENT_LAST_NAME = "Martinez";

    private static final String COLUMN_NRC_UPPER = "NRC";
    private static final String COLUMN_NRC_LOWER = "nrc";
    private static final String COLUMN_SUBJECT_NAME = "nombreExperiencia";
    private static final String COLUMN_PERIOD_NAME = "nombre";
    private static final String COLUMN_PERIOD_ID = "idPeriodoEscolar";
    private static final String COLUMN_STUDENT_ID = "matricula";
    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_LAST_NAME = "apellidos";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private SubjectDAO subjectDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        subjectDAO = new SubjectDAO(connectionManager);
        Field field = SubjectDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(subjectDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private Subject builderSubject() {
        Subject subject = new Subject();
        subject.setNrc(EXPECTED_NRC);
        subject.setSchoolPeriodId(SCHOOL_PERIOD_ID);
        subject.setProfessorPersonnelNumber(PERSONNEL_NUMBER);
        return subject;
    }

    private void mockResultSetAllSubjects() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_NRC_UPPER)).thenReturn(EXPECTED_NRC, SECOND_NRC);
        when(resultSet.getString(COLUMN_SUBJECT_NAME)).thenReturn(SUBJECT_NAME, SUBJECT_NAME);
    }

    private void mockResultSetSubjectsByProfessor() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_NRC_LOWER)).thenReturn(EXPECTED_NRC, SECOND_NRC);
        when(resultSet.getString(COLUMN_SUBJECT_NAME)).thenReturn(SUBJECT_NAME, SUBJECT_NAME);
        when(resultSet.getString(COLUMN_PERIOD_NAME)).thenReturn(SCHOOL_PERIOD_NAME);
        when(resultSet.getInt(COLUMN_PERIOD_ID)).thenReturn(SCHOOL_PERIOD_ID);
    }

    private void mockResultSetEnrolledStudents() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(VALID_STUDENT_ID, SECOND_STUDENT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_STUDENT_NAME, SECOND_STUDENT_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(FIRST_STUDENT_LAST_NAME, SECOND_STUDENT_LAST_NAME);
    }

    @Test
    void registerSubject_bothInsertsSuccessful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED, ROWS_AFFECTED);

        assertTrue(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_firstInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS_AFFECTED, ROWS_AFFECTED);

        assertFalse(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_secondInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED, NO_ROWS_AFFECTED);

        assertFalse(subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_secondInsertThrows_rollsBack() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate())
            .thenReturn(ROWS_AFFECTED)
            .thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.registerSubject(builderSubject()));
    }

    @Test
    void registerSubject_executeUpdateThrows_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.registerSubject(new Subject()));
    }

    @Test
    void registerSubject_preparedStatementError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException(CONNECTION_ERROR));

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
    void getAllSubjectsNrcName_withResults_returnsPopulatedList() throws Exception {
        mockQueryExecution();
        mockResultSetAllSubjects();

        assertEquals(EXPECTED_LIST_SIZE, subjectDAO.getAllSubjectsNrcName().size());
    }

    @Test
    void getAllSubjectsNrcName_emptyResultSet_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getAllSubjectsNrcName().isEmpty());
    }

    @Test
    void getAllSubjectsNrcName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getAllSubjectsNrcName());
    }

    @Test
    void assignStudentToSubject_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC,VALID_PERIOD_ID));
    }

    @Test
    void assignStudentToSubject_noRowsAffected_returnsFalse() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertFalse(subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC,VALID_PERIOD_ID));
    }

    @Test
    void assignStudentToSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.assignStudentToSubject(VALID_STUDENT_ID, EXPECTED_NRC,VALID_PERIOD_ID));
    }

    @Test
    void getSubjectNrcByStudentID_found_returnsNRC() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_NRC_UPPER)).thenReturn(String.valueOf(EXPECTED_NRC));

        assertEquals(String.valueOf(EXPECTED_NRC),
            subjectDAO.getSubjectNrcByStudentID(VALID_STUDENT_ID));
    }

    @Test
    void getSubjectNrcByStudentID_notFound_returnsDefaultMessage() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertEquals(NO_SUBJECT_MESSAGE,
            subjectDAO.getSubjectNrcByStudentID(INVALID_STUDENT_ID));
    }

    @Test
    void getSubjectNrcByStudentID_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getSubjectNrcByStudentID(VALID_STUDENT_ID));
    }

    @Test
    void unassignProfessorFromSubject_successful_doesNotThrow() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

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
        mockQueryExecution();
        mockResultSetSubjectsByProfessor();

        ArrayList<Subject> result = subjectDAO.getSubjectsByProfessor(PERSONNEL_NUMBER);

        assertEquals(EXPECTED_LIST_SIZE, result.size());
    }

    @Test
    void getSubjectsByProfessor_emptyResultSet_returnsEmptyList() throws Exception {
        mockQueryExecution();
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
        mockQueryExecution();
        mockResultSetEnrolledStudents();

        ArrayList<Student> result = subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC);

        assertEquals(EXPECTED_LIST_SIZE, result.size());
    }

    @Test
    void getEnrolledStudentsBySubject_emptyResultSet_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC).isEmpty());
    }

    @Test
    void getEnrolledStudentsBySubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.getEnrolledStudentsBySubject(EXPECTED_NRC));
    }

    @Test
    void isSectionTakenInPeriod_sectionAlreadyExists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        assertTrue(subjectDAO.isSectionTakenInPeriod(SCHOOL_PERIOD_ID, "1"));
    }

    @Test
    void isSectionTakenInPeriod_sectionNotTaken_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        assertFalse(subjectDAO.isSectionTakenInPeriod(SCHOOL_PERIOD_ID, "2"));
    }

    @Test
    void isSectionTakenInPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> subjectDAO.isSectionTakenInPeriod(SCHOOL_PERIOD_ID, "1"));
    }
}