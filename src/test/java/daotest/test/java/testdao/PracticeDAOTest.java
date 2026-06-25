package daotest.test.java.testdao;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

class PracticeDAOTest {

    private static final String STUDENT_ID = "S23013127";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String COLUMN_ID_PRACTICE = "idPractica";
    private static final String COLUMN_GRADE = "calificacion";
    private static final String COLUMN_STUDENT_ID = "matricula";

    private static final int PRACTICE_ID = 1;
    private static final int GRADE = 9;
    private static final int NO_ROWS_AFFECTED = 0;
    private static final int ONE_ROW_AFFECTED = 1;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private PracticeDAO practiceDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        practiceDAO = new PracticeDAO();
        Field field = PracticeDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(practiceDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSinglePractice() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt(COLUMN_ID_PRACTICE)).thenReturn(PRACTICE_ID);
        when(resultSet.getInt(COLUMN_GRADE)).thenReturn(GRADE);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(STUDENT_ID);
    }

    private Practice buildPractice() {
        Practice practice = new Practice();
        practice.setIdStudent(STUDENT_ID);
        practice.setCalification(GRADE);
        return practice;
    }

    @Test
    void getPracticeByStudent_practiceFound_returnsStudentId() throws Exception {
        mockQueryExecution();
        mockResultSetSinglePractice();

        assertEquals(STUDENT_ID, practiceDAO.getPracticeByStudent(STUDENT_ID).getIdStudent());
    }

    @Test
    void getPracticeByStudent_practiceNotFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> practiceDAO.getPracticeByStudent(STUDENT_ID));
    }

    @Test
    void getPracticeByStudent_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> practiceDAO.getPracticeByStudent(STUDENT_ID));
    }

    @Test
    void registerPractice_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ONE_ROW_AFFECTED);

        assertTrue(practiceDAO.registerPractice(buildPractice()));
    }

    @Test
    void registerPractice_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> practiceDAO.registerPractice(buildPractice()));
    }

    @Test
    void registerPractice_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> practiceDAO.registerPractice(buildPractice()));
    }

    @Test
    void existsByStudent_practiceExists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        assertTrue(practiceDAO.existsByStudent(STUDENT_ID));
    }

    @Test
    void existsByStudent_practiceNotExists_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(practiceDAO.existsByStudent(STUDENT_ID));
    }

    @Test
    void existsByStudent_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> practiceDAO.existsByStudent(STUDENT_ID));
    }
}