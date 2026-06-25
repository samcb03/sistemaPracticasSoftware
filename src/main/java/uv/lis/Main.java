package uv.lis;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException{
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/view/FXMLLogin.fxml")
        );
        stage.setTitle("Sistema de Practicas");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}