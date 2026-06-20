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

import uv.lis.GUI.controller.FXMLRegisterProjectSupervisorController;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterProjectSupervisorControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML =
        "/uv/lis/GUI/view/FXMLRegisterProjectSupervisor.fxml";
    private static final String PROJECT_SUPERVISOR_DAO_FIELD = "projectSupervisorDAO";
    private static final String ORGANIZATION_DAO_FIELD = "affiliatedOrganizationDAO";

    private static final String NAME_FIELD_SELECTOR = "#textFieldName";
    private static final String POSITION_FIELD_SELECTOR = "#textFieldPosition";
    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String ORGANIZATION_COMBO_SELECTOR = "#comboBoxOrganizationName";
    private static final String REGISTER_BUTTON_TEXT = "Registrar";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final String VALID_NAME = "Carlos Mendoza";
    private static final String VALID_POSITION = "Gerente";
    private static final String VALID_EMAIL = "carlos.mendoza@empresa.com";
    private static final String VALID_ORGANIZATION = "Tecnologias Avanzadas";
    private static final int VALID_ORGANIZATION_ID = 7;
    private static final String INVALID_EMAIL = "carlos.mendoza@empresa";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_SUPERVISOR_DATA =
        VALID_NAME + FIELD_SEPARATOR + VALID_POSITION + FIELD_SEPARATOR + VALID_EMAIL
        + FIELD_SEPARATOR + VALID_ORGANIZATION_ID;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Encargado técnico registrado correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar al encargado técnico";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE =
        "El correo electrónico no tiene un formato válido";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLRegisterProjectSupervisorController registerController;
    private ProjectSupervisorDAO projectSupervisorDAOMock;
    private AffiliatedOrganizationDAO organizationDAOMock;

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
        projectSupervisorDAOMock = mock(ProjectSupervisorDAO.class);
        organizationDAOMock = mock(AffiliatedOrganizationDAO.class);

        injectField(PROJECT_SUPERVISOR_DAO_FIELD, projectSupervisorDAOMock);
        injectField(ORGANIZATION_DAO_FIELD, organizationDAOMock);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws OperationException {
        when(organizationDAOMock.getOrganizationIdByName(VALID_ORGANIZATION)).thenReturn(VALID_ORGANIZATION_ID);
        when(projectSupervisorDAOMock.registerProjectSupervisor(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws OperationException {
        ArgumentCaptor<ProjectSupervisor> supervisorCaptor = ArgumentCaptor.forClass(ProjectSupervisor.class);
        when(organizationDAOMock.getOrganizationIdByName(VALID_ORGANIZATION)).thenReturn(VALID_ORGANIZATION_ID);
        when(projectSupervisorDAOMock.registerProjectSupervisor(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        verify(projectSupervisorDAOMock).registerProjectSupervisor(supervisorCaptor.capture());
        assertEquals(EXPECTED_SUPERVISOR_DATA, supervisorData(supervisorCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsErrorMessage() throws OperationException {
        when(organizationDAOMock.getOrganizationIdByName(VALID_ORGANIZATION)).thenReturn(VALID_ORGANIZATION_ID);
        when(projectSupervisorDAOMock.registerProjectSupervisor(any())).thenReturn(false);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(organizationDAOMock.getOrganizationIdByName(VALID_ORGANIZATION)).thenReturn(VALID_ORGANIZATION_ID);
        when(projectSupervisorDAOMock.registerProjectSupervisor(any())).thenThrow(operationException);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_invalidEmail_showsValidationError() {
        fillForm(INVALID_EMAIL);

        clickRegister();

        assertEquals(EXPECTED_INVALID_EMAIL_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLRegisterProjectSupervisorController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(registerController, value);
    }

    private void fillForm(String email) {
        clickOn(NAME_FIELD_SELECTOR).write(VALID_NAME);
        clickOn(POSITION_FIELD_SELECTOR).write(VALID_POSITION);
        clickOn(EMAIL_FIELD_SELECTOR).write(email);
        interact(() -> setComboBoxValue(ORGANIZATION_COMBO_SELECTOR, VALID_ORGANIZATION));
    }

    @SuppressWarnings("unchecked")
    private void setComboBoxValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.setValue(value);
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_TEXT);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String supervisorData(ProjectSupervisor supervisor) {
        String data = supervisor.getName() + FIELD_SEPARATOR + supervisor.getPosition() + FIELD_SEPARATOR
            + supervisor.getEmail() + FIELD_SEPARATOR + supervisor.getOrganizationInt();
        return data;
    }
}