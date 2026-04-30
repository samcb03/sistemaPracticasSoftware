package uv.lis.GUI;


import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public abstract class ValidationHandler implements Initializable {

    protected Label messageLabel;
    protected Button backButton;

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

    @FXML
    public void goBack() {
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }
}