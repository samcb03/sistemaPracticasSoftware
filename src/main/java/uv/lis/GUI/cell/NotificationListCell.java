package uv.lis.GUI.cell;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import uv.lis.logic.dto.Notification;

public class NotificationListCell extends ListCell<Notification> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final double CELL_SPACING = 4.0;
    private static final String MESSAGE_STYLE = "-fx-text-fill: #333333;";
    private static final String DATE_STYLE = "-fx-text-fill: #777777; -fx-font-size: 11;";
    private static final String READ_TITLE_STYLE = "-fx-font-weight: normal;";
    private static final String UNREAD_TITLE_STYLE = "-fx-font-weight: bold; -fx-text-fill: #1565C0;";

    private final VBox container;
    private final Label labelTitle;
    private final Label labelMessage;
    private final Label labelDate;

    public NotificationListCell() {
        labelTitle = new Label();
        labelMessage = new Label();
        labelDate = new Label();
        container = buildContainer();
    }

    @Override
    protected void updateItem(Notification notification, boolean empty) {
        super.updateItem(notification, empty);

        if (empty || notification == null) {
            setText(null);
            setGraphic(null);
        } else {
            renderNotification(notification);
        }
    }

    private VBox buildContainer() {
        labelMessage.setWrapText(true);
        labelMessage.setStyle(MESSAGE_STYLE);
        labelDate.setStyle(DATE_STYLE);

        VBox layout = new VBox(labelTitle, labelMessage, labelDate);
        layout.setSpacing(CELL_SPACING);
        return layout;
    }

    private void renderNotification(Notification notification) {
        labelTitle.setText(notification.getTitle());
        labelMessage.setText(notification.getMessage());
        labelDate.setText(formatDate(notification.getCreationDate()));
        applyReadStyle(notification.isRead());

        setText(null);
        setGraphic(container);
    }

    private void applyReadStyle(boolean isRead) {
        if (isRead) {
            labelTitle.setStyle(READ_TITLE_STYLE);
        } else {
            labelTitle.setStyle(UNREAD_TITLE_STYLE);
        }
    }

    private String formatDate(Timestamp creationDate) {
        String formattedDate = "";

        if (creationDate != null) {
            formattedDate = creationDate.toLocalDateTime().format(DATE_FORMATTER);
        }
        return formattedDate;
    }
}