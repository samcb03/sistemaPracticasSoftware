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
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AdvanceDAO;
import uv.lis.logic.dto.Advance;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class AdvanceDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private AdvanceDAO advanceDAO;
    private static final int PROJECT_ID = 1;

    @BeforeEach
    void setUp() throws Exception {
        advanceDAO = new AdvanceDAO();
        Field field = AdvanceDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(advanceDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void registerAdvance_validAdvance_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = advanceDAO.registerAdvance(buildAdvance());

        assertTrue(result);
    }

    @Test
    void registerAdvance_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = advanceDAO.registerAdvance(buildAdvance());

        assertFalse(result);
    }

    @Test
    void registerAdvance_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.registerAdvance(buildAdvance()));
    }

    @Test
    void getAdvancesByProject_advancesExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(1);
        when(resultSet.getInt("idReporte")).thenReturn(10);
        when(resultSet.getInt("semana")).thenReturn(1);
        when(resultSet.getInt("horasAcumuladas")).thenReturn(8);

        ArrayList<Advance> result = advanceDAO.getAdvancesByProject(1);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAdvancesByProject_noAdvances_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ArrayList<Advance> result = advanceDAO.getAdvancesByProject(PROJECT_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAccumulatedHoursByProject_hoursFound_returnsAccumulatedHours() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("horasAcumuladas")).thenReturn(48);

        int result = advanceDAO.getAccumulatedHoursByProject(PROJECT_ID);

        assertEquals(48, result);
    }

    @Test
    void getAccumulatedHoursByProject_noHoursFound_returnsZero() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int result = advanceDAO.getAccumulatedHoursByProject(PROJECT_ID);

        assertEquals(0, result);
    }

    @Test
    void getAccumulatedHoursByProject_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> advanceDAO.getAccumulatedHoursByProject(PROJECT_ID));
    }

    private Advance buildAdvance() {
        Advance advance = new Advance();
        advance.setProjectId(1);
        advance.setReportId(10);
        advance.setWeekNumber(1);
        advance.setAccumulatedHours(8);
        return advance;
    }
}