package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLManageAffiliatedOrganizationController;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class FXMLManageAffiliatedOrganizationControllerTest extends ApplicationTest {

    private static final String MANAGE_VIEW_FXML = "/uv/lis/GUI/view/FXMLManageAffiliatedOrganization.fxml";
    private static final String ORGANIZATION_DAO_FIELD = "affiliatedOrganizationDAO";

    private static final String SEARCH_FIELD_SELECTOR = "#textFieldOrganizationName";
    private static final String EMAIL_FIELD_SELECTOR = "#textFieldEmail";
    private static final String SEARCH_BUTTON_SELECTOR = "#buttonSearch";
    private static final String UPDATE_BUTTON_SELECTOR = "#buttonUpdate";
    private static final String SAVE_BUTTON_SELECTOR = "#buttonSave";
    private static final String NAME_LABEL_SELECTOR = "#labelName";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final int ORGANIZATION_ID = 7;
    private static final int DIRECT_USERS = 5;
    private static final int INDIRECT_USERS = 10;
    private static final String VALID_NAME = "Tecnologias Avanzadas";
    private static final String VALID_CITY = "Xalapa";
    private static final String VALID_STATE = "Veracruz";
    private static final String VALID_STREET = "Avenida Principal";
    private static final String VALID_STREET_NUMBER = "123";
    private static final String VALID_POSTAL_CODE = "91000";
    private static final String VALID_SECTOR = "Privado";
    private static final String VALID_EMAIL = "contacto@tecavanzadas.com";
    private static final String VALID_PHONE = "2281234567";
    private static final String INVALID_EMAIL = "contacto@tecavanzadas";

    private static final String EXPECTED_NOT_FOUND_MESSAGE = "No se encontró la organización";
    private static final String EXPECTED_UPDATE_SUCCESS_MESSAGE = "Organización actualizada correctamente";
    private static final String EXPECTED_NO_CHANGES_MESSAGE = "No se realizaron cambios en la organización";
    private static final String EXPECTED_INVALID_EMAIL_MESSAGE = "El correo no tiene un formato válido";

    private Stage primaryStage;
    private FXMLManageAffiliatedOrganizationController manageController;
    private AffiliatedOrganizationDAO organizationDAOMock;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MANAGE_VIEW_FXML));
        Parent root = loader.load();
        manageController = loader.getController();

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
        organizationDAOMock = mock(AffiliatedOrganizationDAO.class);
        injectField(ORGANIZATION_DAO_FIELD, organizationDAOMock);
        when(organizationDAOMock.searchActiveOrganizationsByNamePrefix(anyString())).thenReturn(new ArrayList<>());
    }

    @Test
    void searchAffiliatedOrganization_found_displaysName() throws OperationException {
        stubFoundSearch();

        performSearch();

        assertEquals(VALID_NAME, nameLabelText());
    }

    @Test
    void searchAffiliatedOrganization_notFound_showsErrorMessage() throws OperationException {
        when(organizationDAOMock.getOrganizationByName(anyString())).thenReturn(Optional.empty());

        performSearch();

        assertEquals(EXPECTED_NOT_FOUND_MESSAGE, messageText());
    }

    @Test
    void saveOrganization_validData_showsSuccessMessage() throws OperationException {
        stubFoundSearch();
        when(organizationDAOMock.modifyOrganization(any())).thenReturn(true);

        performSearch();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_UPDATE_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void saveOrganization_noChangesApplied_showsErrorMessage() throws OperationException {
        stubFoundSearch();
        when(organizationDAOMock.modifyOrganization(any())).thenReturn(false);

        performSearch();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_NO_CHANGES_MESSAGE, messageText());
    }

    @Test
    void saveOrganization_invalidEmail_showsValidationError() throws OperationException {
        stubFoundSearch();

        performSearch();
        enterEditMode();
        interact(() -> lookup(EMAIL_FIELD_SELECTOR).queryAs(TextField.class).setText(INVALID_EMAIL));
        clickSave();

        assertEquals(EXPECTED_INVALID_EMAIL_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLManageAffiliatedOrganizationController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manageController, value);
    }

    private AffiliatedOrganization buildOrganization() {
        AffiliatedOrganization organization = new AffiliatedOrganization();
        organization.setId(ORGANIZATION_ID);
        organization.setName(VALID_NAME);
        organization.setCity(VALID_CITY);
        organization.setState(VALID_STATE);
        organization.setStreet(VALID_STREET);
        organization.setStreetNumber(VALID_STREET_NUMBER);
        organization.setPostalCode(VALID_POSTAL_CODE);
        organization.setSector(VALID_SECTOR);
        organization.setEmail(VALID_EMAIL);
        organization.setPhoneNumber(VALID_PHONE);
        organization.setNumberOfDirectUsers(DIRECT_USERS);
        organization.setNumberOfIndirectUsers(INDIRECT_USERS);
        return organization;
    }

    private void stubFoundSearch() throws OperationException {
        when(organizationDAOMock.getOrganizationByName(anyString())).thenReturn(Optional.of(buildOrganization()));
        when(organizationDAOMock.isOrganizationInactive(anyString())).thenReturn(false);
        when(organizationDAOMock.getProjectsByOrganization(anyString())).thenReturn(new ArrayList<>());
        when(organizationDAOMock.getCompleteProjectsByOrganization(anyString()))
            .thenReturn(new ArrayList<Project>());
    }

    private void performSearch() {
        clickOn(SEARCH_FIELD_SELECTOR).write(VALID_NAME);
        clickOn(SEARCH_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void enterEditMode() {
        clickOn(UPDATE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickSave() {
        clickOn(SAVE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String nameLabelText() {
        String name = lookup(NAME_LABEL_SELECTOR).queryAs(Label.class).getText();
        return name;
    }
}