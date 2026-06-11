package uv.lis.GUI;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public abstract class ValidationHandler extends WindowHandler {

    protected Label messageLabel;
    protected Button backButton;
    protected Button buttonInactive;

    protected void setupControls(Label messageLabel, Button backButton) {
        this.messageLabel = messageLabel;
        this.backButton = backButton;
    }

    protected abstract void clearFields();

    protected void showError(String errorMessage) {
        messageLabel.setText(errorMessage);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    protected void showSuccess(String successMessage) {
        messageLabel.setText(successMessage);
        messageLabel.setStyle("-fx-text-fill: green;");
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Éxito");
        successAlert.setHeaderText(null);
        successAlert.setContentText(successMessage);
        successAlert.showAndWait();
    }

    protected void handleValidation(Optional<String> validationError, Runnable registrationAction) {
        if (validationError.isPresent()) {
            showError(validationError.get());
        } else {
            messageLabel.setText("");
            registrationAction.run();
        }
    }

    protected boolean showConfirmation(String title, String message) {
        boolean confirmed = false;
        ButtonType yesButton = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(yesButton, noButton);

        if (messageLabel != null && messageLabel.getScene() != null) {
            Stage owner = (Stage) messageLabel.getScene().getWindow();
            alert.initOwner(owner);
        }

        Optional<ButtonType> result = alert.showAndWait();
        confirmed = result.isPresent() && result.get() == yesButton;
        return confirmed;
    }
}