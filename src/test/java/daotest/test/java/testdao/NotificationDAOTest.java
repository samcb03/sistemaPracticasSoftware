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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class NotificationDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private NotificationDAO notificationDAO;

    @BeforeEach
    void setUp() throws Exception {
        notificationDAO = new NotificationDAO();
        Field field = NotificationDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(notificationDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void registerNotification_successful_returnsGeneratedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(15);

        int result = notificationDAO.registerNotification(
            builderNotification("S23013127", "Proyecto asignado", "Cumples el perfil"));

        assertEquals(15, result);
    }

    @Test
    void registerNotification_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException("fallo de conexión"));

        assertThrows(OperationException.class, () -> notificationDAO.registerNotification(
            builderNotification("S23013127", "Proyecto asignado", "Cumples el perfil")));
    }

    @Test
    void getNotificationsByStudentId_studentHasNotifications_returnsList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        List<Notification> result = notificationDAO.getNotificationsByStudentId("S23013127");

        assertEquals(2, result.size());
    }

    @Test
    void markNotificationAsRead_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = notificationDAO.markNotificationAsRead(15);

        assertTrue(result);
    }

    @Test
    void getUnreadCountByStudentId_hasUnreadNotifications_returnsCount() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(3);

        int result = notificationDAO.getUnreadCountByStudentId("S23013127");

        assertEquals(3, result);
    }

    private Notification builderNotification(String idStudent, String title, String message) {
        Notification notification = new Notification();
        notification.setIdStudent(idStudent);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreationDate(new Timestamp(System.currentTimeMillis()));
        notification.setRead(false);
        return notification;
    }
}