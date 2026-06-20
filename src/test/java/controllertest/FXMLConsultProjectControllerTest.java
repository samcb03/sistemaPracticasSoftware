package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLConsultProjectController;
import uv.lis.logic.dto.Project;

public class FXMLConsultProjectControllerTest extends ApplicationTest {

    private static final String CONSULT_VIEW_FXML = "/uv/lis/GUI/view/FXMLConsultProject.fxml";
    private static final String ALL_PROJECTS_FIELD = "allProjects";

    private static final String ORGANIZATION_COMBO_SELECTOR = "#comboBoxOrganization";
    private static final String TABLE_SELECTOR = "#tableViewProjects";

    private static final String ORGANIZATION_A = "Tecnologias Avanzadas";
    private static final String ORGANIZATION_B = "Innovacion Digital";
    private static final String ORGANIZATION_C = "Soluciones Globales";

    private static final String PROJECT_ONE = "Sistema de Inventario";
    private static final String PROJECT_TWO = "Portal de Reportes";
    private static final String PROJECT_THREE = "Aplicacion Movil";

    private static final int ORGANIZATION_A_PROJECTS = 2;
    private static final int ORGANIZATION_B_PROJECTS = 1;
    private static final int ORGANIZATION_C_PROJECTS = 0;

    private Stage primaryStage;
    private FXMLConsultProjectController consultController;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(CONSULT_VIEW_FXML));
        Parent root = loader.load();
        consultController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
    }

    @BeforeEach
    void setUpData() throws ReflectiveOperationException {
        injectField(ALL_PROJECTS_FIELD, buildProjects());
    }

    @Test
    void filterByOrganization_matchingOrganization_showsMatchingProjects() {
        selectOrganization(ORGANIZATION_A);

        assertEquals(ORGANIZATION_A_PROJECTS, tableRowCount());
    }

    @Test
    void filterByOrganization_otherOrganization_showsMatchingProjects() {
        selectOrganization(ORGANIZATION_B);

        assertEquals(ORGANIZATION_B_PROJECTS, tableRowCount());
    }

    @Test
    void filterByOrganization_organizationWithoutProjects_showsEmptyTable() {
        selectOrganization(ORGANIZATION_C);

        assertEquals(ORGANIZATION_C_PROJECTS, tableRowCount());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLConsultProjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(consultController, value);
    }

    private Project builderProject(String name, String organizationName) {
        Project project = new Project();
        project.setName(name);
        project.setAffiliatedOrganizationName(organizationName);
        return project;
    }

    private ArrayList<Project> buildProjects() {
        ArrayList<Project> projects = new ArrayList<>();
        projects.add(builderProject(PROJECT_ONE, ORGANIZATION_A));
        projects.add(builderProject(PROJECT_TWO, ORGANIZATION_A));
        projects.add(builderProject(PROJECT_THREE, ORGANIZATION_B));
        return projects;
    }

    @SuppressWarnings("unchecked")
    private void selectOrganization(String organization) {
        interact(() -> {
            ComboBox<String> comboBox = lookup(ORGANIZATION_COMBO_SELECTOR).queryAs(ComboBox.class);
            if (comboBox.getItems().isEmpty()) {
                comboBox.getItems().addAll(ORGANIZATION_A, ORGANIZATION_B, ORGANIZATION_C);
            }
            comboBox.getSelectionModel().select(organization);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private int tableRowCount() {
        TableView<Project> tableView = lookup(TABLE_SELECTOR).queryAs(TableView.class);
        return tableView.getItems().size();
    }
}