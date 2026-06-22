package controllertest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLNotificationsController;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dto.Notification;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLNotificationsControllerTest extends ApplicationTest {

    private static final String NOTIFICATIONS_VIEW_FXML =
        "/uv/lis/GUI/view/FXMLNotifications.fxml";
    private static final String NOTIFICATION_DAO_FIELD = "notificationDAO";
    private static final String STUDENT_FIELD = "student";
    private static final String LOAD_NOTIFICATIONS_METHOD = "loadNotifications";
    private static final String CHECK_STUDENT_AND_LOAD_METHOD = "checkStudentAndLoad";

    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";
    private static final String LIST_VIEW_SELECTOR = "#listViewNotifications";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_STUDENT_NAME = "Denisse";
    private static final String VALID_STUDENT_LASTNAME = "Reyes";

    private static final int VALID_NOTIFICATION_ID = 1;
    private static final String VALID_NOTIFICATION_TITLE = "Asignación proyecto";
    private static final String VALID_NOTIFICATION_BODY = "Has sido asignado al proyecto"
        + " Sistema de Gestión de Inventario para PYMES";

    private static final String EXPECTED_NO_STUDENT_MESSAGE = "No hay un estudiante en sesión";
    private static final String EXPECTED_NO_NOTIFICATIONS_MESSAGE =
        "No tienes notificaciones por el momento";
    private static final String EXPECTED_OPERATION_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLNotificationsController controller;
    private NotificationDAO notificationDAOMock;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentStudent(buildStudent());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(NOTIFICATIONS_VIEW_FXML));
            Parent root = loader.load();
            controller = loader.getController();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @BeforeEach
    void setUpMocks() throws OperationException {
        notificationDAOMock = mock(NotificationDAO.class);

        when(notificationDAOMock.getNotificationsByStudentId(anyString()))
            .thenReturn(new ArrayList<>());

        injectField(NOTIFICATION_DAO_FIELD, notificationDAOMock);
        injectField(STUDENT_FIELD, buildStudent());

        interact(() -> clearLabelMessage());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void loadNotifications_noStudent_showsNoStudentMessage() {
        injectField(STUDENT_FIELD, null);

        invokeMethod(CHECK_STUDENT_AND_LOAD_METHOD);

        assertEquals(EXPECTED_NO_STUDENT_MESSAGE, messageText());
    }

    @Test
    void loadNotifications_emptyList_showsNoNotificationsMessage() throws OperationException {
        when(notificationDAOMock.getNotificationsByStudentId(anyString()))
            .thenReturn(new ArrayList<>());

        invokeMethod(LOAD_NOTIFICATIONS_METHOD);

        assertEquals(EXPECTED_NO_NOTIFICATIONS_MESSAGE, messageText());
    }

    @Test
    void loadNotifications_withNotifications_populatesListView() throws OperationException {
        ArrayList<Notification> notifications = new ArrayList<>();
        notifications.add(buildUnreadNotification());
        when(notificationDAOMock.getNotificationsByStudentId(anyString()))
            .thenReturn(notifications);

        invokeMethod(LOAD_NOTIFICATIONS_METHOD);

        assertEquals(1, listView().getItems().size());
    }

    @Test
    void loadNotifications_withNotifications_clearsMessage() throws OperationException {
        ArrayList<Notification> notifications = new ArrayList<>();
        notifications.add(buildUnreadNotification());
        when(notificationDAOMock.getNotificationsByStudentId(anyString()))
            .thenReturn(notifications);

        invokeMethod(LOAD_NOTIFICATIONS_METHOD);

        assertEquals("", messageText());
    }

    @Test
    void loadNotifications_daoThrowsOperationException_showsErrorMessage()
            throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(notificationDAOMock.getNotificationsByStudentId(anyString()))
            .thenThrow(operationException);

        invokeMethod(LOAD_NOTIFICATIONS_METHOD);

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void selectNotification_unreadNotification_callsMarkAsRead() throws OperationException {
        interact(() -> listView().setItems(
            FXCollections.observableArrayList(buildUnreadNotification())));
        WaitForAsyncUtils.waitForFxEvents();

        //FIXME Validar si es número mágico
        interact(() -> listView().getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        verify(notificationDAOMock).markNotificationAsRead(VALID_NOTIFICATION_ID);
    }

    @Test
    void selectNotification_unreadNotification_marksNotificationAsRead() throws OperationException {
        interact(() -> listView().setItems(
            FXCollections.observableArrayList(buildUnreadNotification())));
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> listView().getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        Notification itemInList = listView().getItems().get(0);
        assertTrue(itemInList.isRead());
    }

    @Test
    void selectNotification_alreadyReadNotification_doesNotCallMarkAsRead()
            throws OperationException {
        interact(() -> listView().setItems(
            FXCollections.observableArrayList(buildReadNotification())));
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> listView().getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        verify(notificationDAOMock, never()).markNotificationAsRead(anyInt());
    }

    @Test
    void selectNotification_markAsReadThrowsOperationException_showsErrorMessage()
            throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(notificationDAOMock.markNotificationAsRead(anyInt()))
            .thenThrow(operationException);

        interact(() -> listView().setItems(
            FXCollections.observableArrayList(buildUnreadNotification())));
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> listView().getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_STUDENT_NAME);
        student.setLastName(VALID_STUDENT_LASTNAME);
        return student;
    }

    private Notification buildUnreadNotification() {
        Notification notification = new Notification();
        notification.setId(VALID_NOTIFICATION_ID);
        notification.setTitle(VALID_NOTIFICATION_TITLE);
        notification.setMessage(VALID_NOTIFICATION_BODY);
        notification.setRead(false);
        return notification;
    }

    private Notification buildReadNotification() {
        Notification notification = buildUnreadNotification();
        notification.setRead(true);
        return notification;
    }

    private void invokeMethod(String methodName) {
        try {
            Method method = FXMLNotificationsController.class
                .getDeclaredMethod(methodName);
            method.setAccessible(true);
            interact(() -> invokeAccessibleMethod(method));
            WaitForAsyncUtils.waitForFxEvents();
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new RuntimeException(noSuchMethodException);
        }
    }

    private void invokeAccessibleMethod(Method method) {
        try {
            method.invoke(controller);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private void injectField(String fieldName, Object value) {
        try {
            Field field = FXMLNotificationsController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    @SuppressWarnings("unchecked")
    private ListView<Notification> listView() {
        return lookup(LIST_VIEW_SELECTOR).queryAs(ListView.class);
    }

    private void clearLabelMessage() {
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }
}