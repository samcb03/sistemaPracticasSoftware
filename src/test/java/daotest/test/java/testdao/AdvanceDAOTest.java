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
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AdvanceDAO;
import uv.lis.logic.dto.Advance;
import uv.lis.logic.exceptions.OperationException;

class AdvanceDAOTest {

    private static final int PROJECT_ID = 2;
    private static final int REPORT_ID = 10;
    private static final int WEEK_NUMBER = 3;
    private static final int ACCUMULATED_HOURS = 8;
    private static final int LATEST_ACCUMULATED_HOURS = 48;
    private static final int ZERO_ACCUMULATED_HOURS = 0;
    private static final int ROWS_AFFECTED = 1;
    private static final int ADVANCE_COUNT = 12;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private AdvanceDAO advanceDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        advanceDAO = new AdvanceDAO();
        Field field = AdvanceDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(advanceDAO, connectionManager);

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

    private void mockResultSetSingleAdvance() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID);
        when(resultSet.getInt("idReporte")).thenReturn(REPORT_ID);
        when(resultSet.getInt("semana")).thenReturn(WEEK_NUMBER);
        when(resultSet.getInt("horasAcumuladas")).thenReturn(ACCUMULATED_HOURS);
    }

    private Advance builderAdvance() {
        Advance advance = new Advance();
        advance.setProjectId(PROJECT_ID);
        advance.setReportId(REPORT_ID);
        advance.setWeekNumber(WEEK_NUMBER);
        advance.setAccumulatedHours(ACCUMULATED_HOURS);
        return advance;
    }

    private List<Advance> builderExpectedAdvances() {
        List<Advance> expectedAdvances = new ArrayList<>();
        expectedAdvances.add(builderAdvance());
        return expectedAdvances;
    }

    @Test
    void registerAdvance_validAdvance_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        boolean result = advanceDAO.registerAdvance(builderAdvance());

        assertTrue(result);
    }

    @Test
    void registerAdvance_noRowsAffected_returnsFalse() throws Exception {
        mockUpdateExecution(NO_VALUE);

        boolean result = advanceDAO.registerAdvance(builderAdvance());

        assertFalse(result);
    }

    @Test
    void registerAdvance_databaseError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.registerAdvance(builderAdvance()));
    }

    @Test
    void getAdvancesByProject_advancesExist_returnsExpectedList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleAdvance();
        List<Advance> expectedAdvances = builderExpectedAdvances();

        List<Advance> result = advanceDAO.getAdvancesByProject(PROJECT_ID);

        assertEquals(expectedAdvances, result);
    }

    @Test
    void getAdvancesByProject_noAdvances_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        List<Advance> result = advanceDAO.getAdvancesByProject(PROJECT_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAdvancesByProject_databaseError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.getAdvancesByProject(PROJECT_ID));
    }

    @Test
    void existsAdvanceForReport_advanceExists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(ADVANCE_COUNT);

        assertTrue(advanceDAO.existsAdvanceForReport(REPORT_ID));
    }

    @Test
    void existsAdvanceForReport_noAdvance_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(NO_VALUE);

        assertFalse(advanceDAO.existsAdvanceForReport(REPORT_ID));
    }

    @Test
    void existsAdvanceForReport_databaseError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.existsAdvanceForReport(REPORT_ID));
    }

    @Test
    void getAccumulatedHoursByProject_hoursFound_returnsAccumulatedHours() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("horasAcumuladas")).thenReturn(LATEST_ACCUMULATED_HOURS);

        int result = advanceDAO.getAccumulatedHoursByProject(PROJECT_ID);

        assertEquals(LATEST_ACCUMULATED_HOURS, result);
    }

    @Test
    void getAccumulatedHoursByProject_noHoursFound_returnsZero() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        int result = advanceDAO.getAccumulatedHoursByProject(PROJECT_ID);

        assertEquals(ZERO_ACCUMULATED_HOURS, result);
    }

    @Test
    void getAccumulatedHoursByProject_databaseError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.getAccumulatedHoursByProject(PROJECT_ID));
    }
}