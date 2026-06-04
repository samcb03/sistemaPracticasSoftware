package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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
    private static final int ACTIVITY_ID = 3;
    private static final int ROWS_AFFECTED = 1;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

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
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetActivity() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);

        when(resultSet.getInt("idActividad")).thenReturn(1, 2);
        when(resultSet.getString("nombreActividad")).thenReturn("Actividad 1", "Actividad 2");
        when(resultSet.getString("descripcionActividad")).thenReturn("Descripción 1", "Descripción 2");
        when(resultSet.getObject("FechaInicio", LocalDate.class))
                .thenReturn(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
        when(resultSet.getObject("FechaFin", LocalDate.class))
                .thenReturn(LocalDate.of(2024, 12, 31), LocalDate.of(2024, 11, 30));
        when(resultSet.getInt("idProyecto")).thenReturn(10, 11);
    }

    private Activity buildExpectedActivity() {
        Activity expected = new Activity();
        expected.setId(1);
        expected.setName("Actividad 1");
        expected.setDescription("Descripción 1");
        expected.setStartDate(LocalDate.of(2024, 1, 1));
        expected.setEndDate(LocalDate.of(2024, 12, 31));
        expected.setId(10);
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
    void getActivityById_successful_returnsActivity() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, true, false);

        assertTrue(activityDAO.getActivityById(ACTIVITY_ID).isPresent());
    }

    @Test
    void getActivityById_emptyList_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getActivityById(ACTIVITY_ID).isEmpty());
    }

    @Test
    void getActivityById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () -> activityDAO.getActivityById(1));
    }

    @Test
    void registerActivity_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); 

        assertTrue(activityDAO.registerActivity(buildExpectedActivity()));
    }

    @Test
    void registerActivity_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

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
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(activityDAO.modifyActivity(buildExpectedActivity()));
    }

    @Test
    void modifyActivity_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

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