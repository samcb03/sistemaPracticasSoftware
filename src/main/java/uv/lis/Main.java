package uv.lis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;

public class Main extends Application {
    
    public static void main(String[] args) throws AuthenticateException, OperationException {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/view/FXMLLogin.fxml")
        );
        stage.setTitle("Sistema de Practicas");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}