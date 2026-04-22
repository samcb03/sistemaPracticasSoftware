package uv.lis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/views/RegisterProfessor.fxml")
        );
        Parent root = loader.load();
        stage.setScene(new Scene(root, 450, 350));
        stage.setTitle("Registrar Profesor");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}