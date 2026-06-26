package uv.lis.GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;

import uv.lis.logic.utils.SessionManager;

public abstract class WindowHandler implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(WindowHandler.class.getName());
    @FXML protected Button buttonLogOut;
    @FXML protected Button buttonBack;

    @FXML
    public void goBack() {
        Stage currentStage = (Stage) buttonBack.getScene().getWindow();
        currentStage.close();
    }

    protected void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Error al cargar la pantalla", e);
            showError("Error al cargar la pantalla.");
        }
    }

    @FXML
    protected void navigateToLogOut() {
        try {
            closeAllOpenStages();
            SessionManager.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/uv/lis/GUI/view/FXMLLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonLogOut.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar la pantalla de inicio de sesión", e);
            showError("Error al cerrar sesión");
        }
    }

    protected FXMLLoader navigateToWithLoader(String fxml) {
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al cargar la pantalla", ioException);
            showError("No se pudo cargar la pantalla. Intente más tarde");
        }
        return loader;
    }

    @FXML
    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void setNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    private void closeAllOpenStages() {
        for (Window window : new ArrayList<>(Stage.getWindows())) {
            if (window instanceof Stage) {
                ((Stage) window).close();
            }
        }
    }
}