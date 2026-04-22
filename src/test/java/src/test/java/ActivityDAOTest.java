package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;


class ActivityDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;

    @Mock
    private Connection databaseConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ActivityDAO activityDAO;

    @BeforeEach
    void setUp() throws Exception {
        activityDAO = new ActivityDAO();
        Field field = ActivityDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(activityDAO, connectionManager);

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

    private void mockResultSetActivity() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);

        when(resultSet.getInt("idActividad")).thenReturn(1, 2);
        when(resultSet.getString("nombreActividad")).thenReturn("Actividad 1", "Actividad 2");
        when(resultSet.getString("descripcionActividad")).thenReturn("Descripción 1", "Descripción 2");
        when(resultSet.getDate("FechaInicio"))
                .thenReturn(Date.valueOf("2024-01-01"), Date.valueOf("2024-02-01"));
        when(resultSet.getDate("FechaFin"))
                .thenReturn(Date.valueOf("2024-12-31"), Date.valueOf("2024-11-30"));
    }

    private Activity buildExpectedActivity() {
        Activity expected = new Activity();
        expected.setId(1);
        expected.setName("Actividad 1");
        expected.setDescription("Descripción 1");
        expected.setStartDate(Date.valueOf("2024-01-01"));
        expected.setEndDate(Date.valueOf("2024-12-31"));
        return expected;
    }

    @Test
    void getAllActivities_successful_returnsActivityList() throws Exception {
        mockQueryExecution();
        mockResultSetActivity();
        when(resultSet.next()).thenReturn(true, true, false);

        assertEquals(2, activityDAO.getAllActivities().size());
    }

    @Test
    void getAllActivities_emptyList_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getAllActivities().isEmpty());
    }

    @Test
    void getAllActivities_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () -> activityDAO.getAllActivities());
    }

    @Test
    void getActivitiesById_successful_returnsActivityList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, true, false);

        assertEquals(2, activityDAO.getActivitiesById(1).size());
    }

    @Test
    void getActivitiesById_emptyList_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getActivitiesById(999).isEmpty());
    }

    @Test
    void getActivitiesById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () -> activityDAO.getActivitiesById(1));
    }

    @Test
    void registerActivity_successful_returnsTrue() throws Exception {
        mockUpdateExecution(1);

        assertTrue(activityDAO.registerActivity(buildExpectedActivity()));
    }

    @Test
    void registerActivity_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(0);

        assertThrows(OperationException.class, () ->
            activityDAO.registerActivity(buildExpectedActivity())
        );
    }

    @Test
    void registerActivity_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            activityDAO.registerActivity(buildExpectedActivity())
        );
    }

    @Test
    void modifyActivity_successful_returnsTrue() throws Exception {
        mockUpdateExecution(1);

        assertTrue(activityDAO.modifyActivity(buildExpectedActivity()));
    }

    @Test
    void modifyActivity_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(0);

        assertThrows(OperationException.class, () ->
            activityDAO.modifyActivity(buildExpectedActivity())
        );
    }

    @Test
    void modifyActivity_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            activityDAO.modifyActivity(buildExpectedActivity())
        );
    }
}