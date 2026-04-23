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

        FXMLLoader loaderStudent = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/FXMLRegisterStudent.fxml")
        );
        Stage stageStudent = new Stage();
        stageStudent.setTitle("Registro de Estudiante");
        stageStudent.setScene(new Scene(loaderStudent.load()));
        stageStudent.show();

        FXMLLoader loaderProfessor = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/FXMLRegisterProfessor.fxml")
        );
        Stage stageProfessor = new Stage();
        stageProfessor.setTitle("Registro de Profesor");
        stageProfessor.setScene(new Scene(loaderProfessor.load()));
        stageProfessor.show();

        FXMLLoader loaderOrganization = new FXMLLoader(
            getClass().getResource("/uv/lis/GUI/FXMLRegisterAffiliedOrganization.fxml")
        );
        Stage stageOrganization = new Stage();
        stageOrganization.setTitle("Registro de Organización Vinculada");
        stageOrganization.setScene(new Scene(loaderOrganization.load()));
        stageOrganization.show();
    }
}