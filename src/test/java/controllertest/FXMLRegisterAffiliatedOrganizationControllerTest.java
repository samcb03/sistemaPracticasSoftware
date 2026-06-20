package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterAffiliatedOrganizationController;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterAffiliatedOrganizationControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML =
        "/uv/lis/GUI/view/FXMLRegisterAffiliatedOrganization.fxml";
    private static final String ORGANIZATION_DAO_FIELD = "affiliatedOrganizationDAO";

    private static final String NAME_FIELD_SELECTOR = "#textFieldName";
    private static final String CITY_FIELD_SELECTOR = "#textFieldCity";
    private static final String STATE_FIELD_SELECTOR = "#textFieldState";
    private static final String SECTOR_FIELD_SELECTOR = "#textFieldSector";
    private static final String STREET_FIELD_SELECTOR = "#textFieldStreet";
    private static final String STREET_NUMBER_FIELD_SELECTOR = "#textFieldStreetNumber";
    private static final String POSTAL_CODE_FIELD_SELECTOR = "#textFieldPostalCode";
    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String PHONE_FIELD_SELECTOR = "#textFieldPhoneNumber";
    private static final String DIRECT_USERS_FIELD_SELECTOR = "#textFieldNumberOfDirectUsers";
    private static final String INDIRECT_USERS_FIELD_SELECTOR = "#textFieldNumberOfIndirectUsers";
    private static final String REGISTER_BUTTON_SELECTOR = "#buttonRegisterOrganization";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final String VALID_NAME = "Universidad Veracruzana";
    private static final String VALID_CITY = "Xalapa";
    private static final String VALID_STATE = "Veracruz";
    private static final String VALID_SECTOR = "Privado";
    private static final String VALID_STREET = "Avenida Xalapa";
    private static final String VALID_STREET_NUMBER = "123";
    private static final String VALID_POSTAL_CODE = "91000";
    private static final String VALID_EMAIL = "uv@organizacion.com";
    private static final String VALID_PHONE = "2281234567";
    private static final String VALID_DIRECT_USERS = "10";
    private static final String VALID_INDIRECT_USERS = "20";
    private static final String INVALID_EMAIL = "uv@organizacion";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_ORGANIZATION_DATA =
        VALID_NAME + FIELD_SEPARATOR + VALID_CITY + FIELD_SEPARATOR + VALID_STATE + FIELD_SEPARATOR
        + VALID_SECTOR + FIELD_SEPARATOR + VALID_STREET + FIELD_SEPARATOR + VALID_STREET_NUMBER
        + FIELD_SEPARATOR + VALID_POSTAL_CODE + FIELD_SEPARATOR + VALID_EMAIL + FIELD_SEPARATOR
        + VALID_PHONE + FIELD_SEPARATOR + VALID_DIRECT_USERS + FIELD_SEPARATOR + VALID_INDIRECT_USERS;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Organización registrada correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar la organización";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE =
        "El correo electrónico no tiene un formato válido";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private FXMLRegisterAffiliatedOrganizationController registerController;
    private AffiliatedOrganizationDAO organizationDAOMock;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(REGISTER_VIEW_FXML));
        Parent root = loader.load();
        registerController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMock() throws Exception {
        organizationDAOMock = mock(AffiliatedOrganizationDAO.class);
        injectOrganizationDAO(organizationDAOMock);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws OperationException {
        when(organizationDAOMock.registerOrganization(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws OperationException {
        ArgumentCaptor<AffiliatedOrganization> organizationCaptor =
            ArgumentCaptor.forClass(AffiliatedOrganization.class);
        when(organizationDAOMock.registerOrganization(any())).thenReturn(true);

        fillForm(VALID_EMAIL);
        clickRegister();

        verify(organizationDAOMock).registerOrganization(organizationCaptor.capture());
        assertEquals(EXPECTED_ORGANIZATION_DATA, organizationData(organizationCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsOperationExceptionMessage() throws OperationException {
        when(organizationDAOMock.registerOrganization(any())).thenReturn(false);

        fillForm(VALID_EMAIL);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsOperationExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(organizationDAOMock.registerOrganization(any())).thenThrow(operationException);

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

    private void injectOrganizationDAO(AffiliatedOrganizationDAO organizationDAOInstance) throws Exception {
        Field organizationDAOField =
            FXMLRegisterAffiliatedOrganizationController.class.getDeclaredField(ORGANIZATION_DAO_FIELD);
        organizationDAOField.setAccessible(true);
        organizationDAOField.set(registerController, organizationDAOInstance);
    }

    private void fillForm(String email) {
        clickOn(NAME_FIELD_SELECTOR).write(VALID_NAME);
        clickOn(CITY_FIELD_SELECTOR).write(VALID_CITY);
        clickOn(STATE_FIELD_SELECTOR).write(VALID_STATE);
        clickOn(SECTOR_FIELD_SELECTOR).write(VALID_SECTOR);
        clickOn(STREET_FIELD_SELECTOR).write(VALID_STREET);
        clickOn(STREET_NUMBER_FIELD_SELECTOR).write(VALID_STREET_NUMBER);
        clickOn(POSTAL_CODE_FIELD_SELECTOR).write(VALID_POSTAL_CODE);
        clickOn(EMAIL_FIELD_SELECTOR).write(email);
        clickOn(PHONE_FIELD_SELECTOR).write(VALID_PHONE);
        clickOn(DIRECT_USERS_FIELD_SELECTOR).write(VALID_DIRECT_USERS);
        clickOn(INDIRECT_USERS_FIELD_SELECTOR).write(VALID_INDIRECT_USERS);
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String organizationData(AffiliatedOrganization organization) {
        String data = organization.getName() + FIELD_SEPARATOR + organization.getCity() + FIELD_SEPARATOR
            + organization.getState() + FIELD_SEPARATOR + organization.getSector() + FIELD_SEPARATOR
            + organization.getStreet() + FIELD_SEPARATOR + organization.getStreetNumber() + FIELD_SEPARATOR
            + organization.getPostalCode() + FIELD_SEPARATOR + organization.getEmail() + FIELD_SEPARATOR
            + organization.getPhoneNumber() + FIELD_SEPARATOR + organization.getNumberOfDirectUsers()
            + FIELD_SEPARATOR + organization.getNumberOfIndirectUsers();
        return data;
    }
}