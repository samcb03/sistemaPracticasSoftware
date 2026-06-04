package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.INVALID_ID;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.INotificationDAO;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.exceptions.OperationException;

public class NotificationDAO implements INotificationDAO {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public NotificationDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public NotificationDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public int registerNotification(Notification notification) throws OperationException {
        int generatedId = INVALID_ID;
        String notificationQuery = "INSERT INTO Notificacion "
                                 + "(matricula, titulo, mensaje, fechaCreacion, leida) "
                                 + "VALUES (?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(
                notificationQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, notification.getIdStudent());
            preparedStatement.setString(2, notification.getTitle());
            preparedStatement.setString(3, notification.getMessage());
            preparedStatement.setTimestamp(4, notification.getCreationDate());
            preparedStatement.setBoolean(5, notification.isRead());
            preparedStatement.executeUpdate();

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedId = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la notificación", e);
            throw new OperationException("Error al registrar la notificación", e);
        }

        return generatedId;
    }

    @Override
    public List<Notification> getNotificationsByStudentId(String idStudent) throws OperationException {
        List<Notification> notifications = new ArrayList<>();
        String notificationQuery = "SELECT idNotificacion, matricula, titulo, mensaje, "
                                 + "fechaCreacion, leida FROM Notificacion "
                                 + "WHERE matricula = ? ORDER BY fechaCreacion DESC;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(notificationQuery)) {

            preparedStatement.setString(1, idStudent);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(buildNotificationFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener las notificaciones del estudiante", e);
            throw new OperationException("Error al obtener las notificaciones del estudiante", e);
        }

        return notifications;
    }

    @Override
    public boolean markNotificationAsRead(int idNotification) throws OperationException {
        boolean isUpdated = false;
        String notificationQuery = "UPDATE Notificacion SET leida = ? WHERE idNotificacion = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(notificationQuery)) {

            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, idNotification);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isUpdated = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al marcar la notificación como leída", e);
            throw new OperationException("Error al marcar la notificación como leída", e);
        }

        return isUpdated;
    }

    @Override
    public int getUnreadCountByStudentId(String idStudent) throws OperationException {
        int unreadCount = NO_ROWS_AFFECTED;
        String notificationQuery = "SELECT COUNT(*) AS total FROM Notificacion "
                                 + "WHERE matricula = ? AND leida = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(notificationQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setBoolean(2, false);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    unreadCount = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar las notificaciones no leídas", e);
            throw new OperationException("Error al contar las notificaciones no leídas", e);
        }

        return unreadCount;
    }

    private Notification buildNotificationFromResultSet(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setId(resultSet.getInt("idNotificacion"));
        notification.setIdStudent(resultSet.getString("matricula"));
        notification.setTitle(resultSet.getString("titulo"));
        notification.setMessage(resultSet.getString("mensaje"));
        notification.setCreationDate(resultSet.getTimestamp("fechaCreacion"));
        notification.setRead(resultSet.getBoolean("leida"));
        return notification;
    }
}