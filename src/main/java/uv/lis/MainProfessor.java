package uv.lis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainProfessor extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/FXMLRegisterProfessor.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("Registro Profesor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}