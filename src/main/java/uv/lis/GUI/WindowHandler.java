package uv.lis.GUI;


import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;


public abstract class WindowHandler implements Initializable {

    @FXML protected Button buttonLogOut;
    @FXML protected Button buttonBack;

    protected void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error al cargar la pantalla.");
        }
    }

    @FXML
    protected void navigateToLogOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/uv/lis/GUI/view/FXMLLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonLogOut.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error al cerrar sesión");
                
        }
    }

    @FXML
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    @FXML
    public void goBack() {
        Stage currentStage = (Stage) buttonBack.getScene().getWindow();
        currentStage.close();
    }

}