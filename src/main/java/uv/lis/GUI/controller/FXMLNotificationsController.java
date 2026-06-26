package uv.lis.GUI.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import uv.lis.GUI.ValidationHandler;
import uv.lis.GUI.cell.NotificationListCell;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLNotificationsController extends ValidationHandler {

    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final String NO_NOTIFICATIONS_MESSAGE = "No tienes notificaciones por el momento";

    @FXML private ListView<Notification> listViewNotifications;
    @FXML private Label labelMessage;
    @FXML private Button buttonBack;

    private NotificationDAO notificationDAO;
    private Student student;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(labelMessage, buttonBack);
        notificationDAO = new NotificationDAO();
        student = SessionManager.getInstance().getCurrentStudent();
        setupNotificationCells();
        setupSelectionListener();
        checkStudentAndLoad();
    }

    @Override
    protected void clearFields() {
        listViewNotifications.getItems().clear();
    }

    private void checkStudentAndLoad() {
        if (student == null) {
            showError(NO_STUDENT_IN_SESSION_MESSAGE);
        } else {
            loadNotifications();
        }
    }

    private void setupNotificationCells() {
        listViewNotifications.setCellFactory(listView -> new NotificationListCell());
    }
    
    private void setupSelectionListener() {
        listViewNotifications.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                
                if (newValue != null && !newValue.isRead()) {
                    markSelectedAsRead(newValue);
                }
            }
        );
    }

    private void loadNotifications() {
        try {
            List<Notification> notifications = notificationDAO.getNotificationsByStudentId(student.getIdStudent());
            listViewNotifications.setItems(FXCollections.observableArrayList(notifications));

            if (notifications.isEmpty()) {
                showError(NO_NOTIFICATIONS_MESSAGE);
            } else {
                labelMessage.setText("");
            }
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }

    private void markSelectedAsRead(Notification notification) {
        try {
            notificationDAO.markNotificationAsRead(notification.getId());
            notification.setRead(true);
            listViewNotifications.refresh();
        } catch (OperationException e) {
            showError(e.getMessage());
        }
    }
}