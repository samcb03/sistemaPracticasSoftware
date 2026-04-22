package uv.lis;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;


public class Main extends Application{
public static void main(String[] args) throws AuthenticateException, OperationException {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uv/lis/GUI/FXMLRegisterStudent.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Registro de estudiante");
        stage.show();

        FXMLLoader loaderProfessor = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/FXMLRegisterProfessor.fxml")
        );
        Stage stageProfessor = new Stage();
        stageProfessor.setTitle("Registro de Profesor");
        stageProfessor.setScene(new Scene(loaderProfessor.load()));
        stageProfessor.show();
        }
}