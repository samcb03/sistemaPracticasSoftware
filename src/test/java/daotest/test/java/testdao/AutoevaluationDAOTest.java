package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_VALUE;

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
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;

class AutoevaluationDAOTest {

    private static final String VALID_STUDENT_ID = "S200123";
    private static final String STUDENT_NAME = "Juan";
    private static final String STUDENT_LASTNAME = "Pérez";
    private static final String PROJECT_NAME = "Proyecto Alpha";
    private static final String ORGANIZATION_NAME = "Empresa XYZ";
    private static final String SUPERVISOR_NAME = "Dr. García";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final int ROWS_AFFECTED = 1;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private AutoevaluationDAO autoevaluationDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        autoevaluationDAO = new AutoevaluationDAO();
        Field field = AutoevaluationDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(autoevaluationDAO, connectionManager);

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

    private void mockResultSetAutoevaluationData() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("nombreAlumno")).thenReturn(STUDENT_NAME);
        when(resultSet.getString("apellidosAlumno")).thenReturn(STUDENT_LASTNAME);
        when(resultSet.getString("proyecto")).thenReturn(PROJECT_NAME);
        when(resultSet.getString("organizacion")).thenReturn(ORGANIZATION_NAME);
        when(resultSet.getString("responsable")).thenReturn(SUPERVISOR_NAME);
    }

    private Autoevaluation buildExpectedAutoevaluation() {
        Autoevaluation autoevaluation = new Autoevaluation();
        autoevaluation.setIdStudent(VALID_STUDENT_ID);
        autoevaluation.setStudentName(STUDENT_NAME + " " + STUDENT_LASTNAME);
        autoevaluation.setProjectName(PROJECT_NAME);
        autoevaluation.setOrganizationName(ORGANIZATION_NAME);
        autoevaluation.setProjectSupervisorName(SUPERVISOR_NAME);
        return autoevaluation;
    }

    private Autoevaluation buildAutoevaluation() {
        int[] answers = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        return new Autoevaluation(VALID_STUDENT_ID, answers);
    }

    @Test
    void getAutoevaluationData_successful_returnsAutoevaluation() throws Exception {
        mockQueryExecution();
        mockResultSetAutoevaluationData();

        assertEquals(buildExpectedAutoevaluation(),
            autoevaluationDAO.getAutoevaluationData(VALID_STUDENT_ID));
    }

    @Test
    void getAutoevaluationData_studentNotFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> autoevaluationDAO.getAutoevaluationData(VALID_STUDENT_ID));
    }

    @Test
    void getAutoevaluationData_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> autoevaluationDAO.getAutoevaluationData(VALID_STUDENT_ID));
    }

    @Test
    void registerAutoevaluation_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void registerAutoevaluation_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_VALUE);

        assertThrows(OperationException.class,
            () -> autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void registerAutoevaluation_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void existsByStudent_exists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        assertTrue(autoevaluationDAO.existsByStudent(VALID_STUDENT_ID));
    }

    @Test
    void existsByStudent_notExists_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(autoevaluationDAO.existsByStudent(VALID_STUDENT_ID));
    }

    @Test
    void existsByStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> autoevaluationDAO.existsByStudent(VALID_STUDENT_ID));
    }
}