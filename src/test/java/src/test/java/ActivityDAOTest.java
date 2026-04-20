package src.test.java;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
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
    }

    @Test
    void getAllActivities_successful_returnsActivityList() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        List<Activity> result = activityDAO.getAllActivities();

        assertEquals(2, result.size());
    }

    @Test
    void getAllActivities_emptyList_returnsEmptyList() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Activity> result = activityDAO.getAllActivities();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllActivities_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () -> activityDAO.getAllActivities());
    }

    @Test
    void getActivitiesById_successful_returnsActivityList() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        List<Activity> result = activityDAO.getActivitiesById(1);

        assertEquals(2, result.size());
    }

    @Test
    void getActivitiesById_emptyList_returnsEmptyList() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Activity> result = activityDAO.getActivitiesById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    void getActivitiesById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            activityDAO.getActivitiesById(1)
        );
        assertTrue(exception.getMessage().contains("Error al obtener las actividades"));
    }

    @Test
    void registerActivity_successful_returnsTrue() throws Exception {
        Activity activity = buildActivity(1, "Actividad 1");

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = activityDAO.registerActivity(activity);

        assertTrue(result);
    }

    @Test
    void registerActivity_noRowsAffected_throwsOperationException() throws Exception {
        Activity activity = buildActivity(1, "Actividad 1");

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            activityDAO.registerActivity(activity)
        );
        assertTrue(exception.getMessage().contains("Error al registrar la actividad"));
    }

    @Test
    void registerActivity_sqlError_throwsOperationException() throws Exception {
        Activity activity = buildActivity(1, "Actividad 1");

        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            activityDAO.registerActivity(activity)
        );
        assertTrue(exception.getMessage().contains("Error al registrar la actividad"));
    }

    @Test
    void modifyActivity_successful_returnsTrue() throws Exception {
        Activity activity = buildActivity(1, "Actividad Modificada");

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = activityDAO.modifyActivity(activity);

        assertTrue(result);
    }

    @Test
    void modifyActivity_noRowsAffected_throwsOperationException() throws Exception {
        Activity activity = buildActivity(999, "No Existe");

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            activityDAO.modifyActivity(activity)
        );
        assertTrue(exception.getMessage().contains("Error al modificar la actividad"));
    }

    @Test
    void modifyActivity_sqlError_throwsOperationException() throws Exception {
        Activity activity = buildActivity(1, "Actividad 1");

        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            activityDAO.modifyActivity(activity)
        );
        assertTrue(exception.getMessage().contains("Error al modificar la actividad"));
    }

    private Activity buildActivity(int id, String name) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setName(name);
        return activity;
    }
}