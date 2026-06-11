package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.Notification;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for student notifications.
 */
public interface INotificationDAO {

    /**
     * Registers a new notification in the system.
     *
     * @param notification the notification data to register
     * @return the generated identifier of the registered notification
     * @throws OperationException if the notification cannot be registered
     */
    int registerNotification(Notification notification) throws OperationException;

    /**
     * Retrieves the notifications that belong to a student.
     *
     * @param idStudent the identifier of the student whose notifications are retrieved
     * @return the list of notifications for the student, empty if there are none
     * @throws OperationException if the notifications cannot be retrieved
     */
    List<Notification> getNotificationsByStudentId(String idStudent) throws OperationException;

    /**
     * Marks a notification as read.
     *
     * @param idNotification the identifier of the notification to mark as read
     * @return true if the notification was marked as read, false otherwise
     * @throws OperationException if the notification cannot be updated
     */
    boolean markNotificationAsRead(int idNotification) throws OperationException;
}