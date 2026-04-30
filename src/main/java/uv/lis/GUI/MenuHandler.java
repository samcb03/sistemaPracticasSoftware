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


public abstract class MenuHandler implements Initializable {

    @FXML protected Button buttonLogOut;

    private static Scene backScene = null;

    protected void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) buttonLogOut.getScene().getWindow();
            backScene = stage.getScene(); // Guarda la escena actual

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
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

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void goBack() {
        if (backScene != null) {
            Stage stage = (Stage) buttonLogOut.getScene().getWindow();
            stage.setScene(backScene);
            backScene = null;
        }
    }

}