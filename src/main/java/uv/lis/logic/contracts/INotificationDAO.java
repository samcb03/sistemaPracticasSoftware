package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.Notification;
import uv.lis.logic.exceptions.OperationException;

public interface INotificationDAO {
    int registerNotification(Notification notification) throws OperationException;

    List<Notification> getNotificationsByStudentId(String idStudent) throws OperationException;

    boolean markNotificationAsRead(int idNotification) throws OperationException;

    int getUnreadCountByStudentId(String idStudent) throws OperationException;
}