package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterProjectController;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProjectControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterProject.fxml";
    private static final String PROJECT_DAO_FIELD = "projectDAO";
    private static final String ORGANIZATION_DAO_FIELD = "affiliatedOrganizationDAO";
    private static final String SUPERVISOR_DAO_FIELD = "projectSupervisorDAO";

    private static final String NAME_FIELD_SELECTOR = "#textFieldName";
    private static final String METHODOLOGY_FIELD_SELECTOR = "#textFieldMethodology";
    private static final String CAPACITY_FIELD_SELECTOR = "#textFieldCapacity";
    private static final String OBJECTIVE_FIELD_SELECTOR = "#textFieldObjective";
    private static final String DESCRIPTION_AREA_SELECTOR = "#textAreaDescription";
    private static final String ORGANIZATION_COMBO_SELECTOR = "#comboBoxOrganizationName";
    private static final String SUPERVISOR_COMBO_SELECTOR = "#comboBoxSupervisorName";
    private static final String REGISTER_BUTTON_TEXT = "Registrar Proyecto";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final String VALID_NAME = "Sistema de Inventario";
    private static final String VALID_METHODOLOGY = "Cascada";
    private static final String VALID_CAPACITY = "2";
    private static final String VALID_OBJECTIVE = "Optimizar procesos";
    private static final String VALID_DESCRIPTION = "Gestion de inventario en almacen";
    private static final String VALID_ORGANIZATION = "Tecnologias Avanzadas";
    private static final String VALID_SUPERVISOR = "Carlos Mendoza";
    private static final int VALID_ORGANIZATION_ID = 7;
    private static final int VALID_SUPERVISOR_ID = 3;
    private static final String INVALID_NAME = "Proyecto@Beta";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_PROJECT_DATA =
        VALID_NAME + FIELD_SEPARATOR + VALID_METHODOLOGY + FIELD_SEPARATOR + VALID_CAPACITY
        + FIELD_SEPARATOR + VALID_OBJECTIVE + FIELD_SEPARATOR + VALID_DESCRIPTION + FIELD_SEPARATOR
        + VALID_ORGANIZATION_ID + FIELD_SEPARATOR + VALID_SUPERVISOR_ID;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Proyecto registrado correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar el proyecto";
    private static final String EXPECTED_INVALID_NAME_MESSAGE = "El nombre no acepta carácteres especiales";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLRegisterProjectController registerController;
    private ProjectDAO projectDAOMock;
    private AffiliatedOrganizationDAO organizationDAOMock;
    private ProjectSupervisorDAO supervisorDAOMock;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(REGISTER_VIEW_FXML));
        Parent root = loader.load();
        registerController = loader.getController();

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
    void setUpMocks() throws Exception {
        projectDAOMock = mock(ProjectDAO.class);
        organizationDAOMock = mock(AffiliatedOrganizationDAO.class);
        supervisorDAOMock = mock(ProjectSupervisorDAO.class);

        injectField(PROJECT_DAO_FIELD, projectDAOMock);
        injectField(ORGANIZATION_DAO_FIELD, organizationDAOMock);
        injectField(SUPERVISOR_DAO_FIELD, supervisorDAOMock);

        when(organizationDAOMock.getOrganizationIdByName(VALID_ORGANIZATION)).thenReturn(VALID_ORGANIZATION_ID);
        when(supervisorDAOMock.getSupervisorIdByName(VALID_SUPERVISOR)).thenReturn(VALID_SUPERVISOR_ID);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws OperationException {
        when(projectDAOMock.registerProject(any())).thenReturn(true);

        fillForm(VALID_NAME);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws OperationException {
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        when(projectDAOMock.registerProject(any())).thenReturn(true);

        fillForm(VALID_NAME);
        clickRegister();

        verify(projectDAOMock).registerProject(projectCaptor.capture());
        assertEquals(EXPECTED_PROJECT_DATA, projectData(projectCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsErrorMessage() throws OperationException {
        when(projectDAOMock.registerProject(any())).thenReturn(false);

        fillForm(VALID_NAME);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(projectDAOMock.registerProject(any())).thenThrow(operationException);

        fillForm(VALID_NAME);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_invalidName_showsValidationError() {
        fillForm(INVALID_NAME);

        clickRegister();

        assertEquals(EXPECTED_INVALID_NAME_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLRegisterProjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(registerController, value);
    }

    private void fillForm(String name) {
        clickOn(NAME_FIELD_SELECTOR).write(name);
        clickOn(METHODOLOGY_FIELD_SELECTOR).write(VALID_METHODOLOGY);
        clickOn(CAPACITY_FIELD_SELECTOR).write(VALID_CAPACITY);
        clickOn(OBJECTIVE_FIELD_SELECTOR).write(VALID_OBJECTIVE);
        clickOn(DESCRIPTION_AREA_SELECTOR).write(VALID_DESCRIPTION);
        interact(() -> setComboBoxValue(ORGANIZATION_COMBO_SELECTOR, VALID_ORGANIZATION));
        interact(() -> setComboBoxValue(SUPERVISOR_COMBO_SELECTOR, VALID_SUPERVISOR));
    }

    @SuppressWarnings("unchecked")
    private void setComboBoxValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.setValue(value);
    }

    private void clickRegister() {
        Button registerButton = lookup(REGISTER_BUTTON_TEXT).queryAll().stream()
            .filter(node -> node instanceof Button)
            .map(Button.class::cast)
            .findFirst()
            .orElseThrow();
        clickOn(registerButton);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String projectData(Project project) {
        String data = project.getName() + FIELD_SEPARATOR + project.getMethodology() + FIELD_SEPARATOR
            + project.getCapacity() + FIELD_SEPARATOR + project.getObjective() + FIELD_SEPARATOR
            + project.getDescription() + FIELD_SEPARATOR + project.getIdAffiliatedOrganization()
            + FIELD_SEPARATOR + project.getIdSupervisor();
        return data;
    }
}