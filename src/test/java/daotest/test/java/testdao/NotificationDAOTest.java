package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.exceptions.OperationException;

class NotificationDAOTest {

    private static final int    GENERATED_ID            = 15;
    private static final int    ROWS_AFFECTED           = 1;
    private static final int    NOTIFICATION_COUNT      = 2;
    private static final int    NOTIFICATION_ID         = 15;
    private static final String STUDENT_ID              = "S23013127";
    private static final String NOTIFICATION_TITLE      = "Proyecto asignado";
    private static final String NOTIFICATION_MESSAGE    = "Cumples el perfil";
    private static final String DATABASE_ERROR_MESSAGE  = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection             databaseConnection;
    @Mock private PreparedStatement      preparedStatement;
    @Mock private ResultSet              resultSet;
    @Mock private ResultSet              generatedKeys;

    private NotificationDAO notificationDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        notificationDAO = new NotificationDAO();
        Field field = NotificationDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(notificationDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
    }

    private Notification buildNotification() {
        Notification notification = new Notification();
        notification.setIdStudent(STUDENT_ID);
        notification.setTitle(NOTIFICATION_TITLE);
        notification.setMessage(NOTIFICATION_MESSAGE);
        notification.setCreationDate(new Timestamp(System.currentTimeMillis()));
        notification.setRead(false);
        return notification;
    }

    @Test
    void registerNotification_successful_returnsGeneratedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(GENERATED_ID);

        assertEquals(GENERATED_ID, notificationDAO.registerNotification(buildNotification()));
    }

    @Test
    void registerNotification_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> notificationDAO.registerNotification(buildNotification()));
    }

    @Test
    void getNotificationsByStudentId_studentHasNotifications_returnsList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, true, false);

        assertEquals(NOTIFICATION_COUNT,
            notificationDAO.getNotificationsByStudentId(STUDENT_ID).size());
    }

    @Test
    void getNotificationsByStudentId_noNotifications_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(notificationDAO.getNotificationsByStudentId(STUDENT_ID).isEmpty());
    }

    @Test
    void getNotificationsByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> notificationDAO.getNotificationsByStudentId(STUDENT_ID));
    }

    @Test
    void markNotificationAsRead_successful_returnsTrue() throws Exception {
        mockUpdateExecution();

        assertTrue(notificationDAO.markNotificationAsRead(NOTIFICATION_ID));
    }

    @Test
    void markNotificationAsRead_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> notificationDAO.markNotificationAsRead(NOTIFICATION_ID));
    }
}