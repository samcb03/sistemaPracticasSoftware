package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterActivityController;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterActivityControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterActivity.fxml";
    private static final String ACTIVITY_DAO_FIELD = "activityDAO";
    private static final String PROJECT_DAO_FIELD = "projectDAO";
    private static final String CURRENT_PROJECT_ID_FIELD = "currentProjectId";

    private static final String ACTIVITY_FIELD_SELECTOR = "#textFieldActivity";
    private static final String DESCRIPTION_FIELD_SELECTOR = "#textFieldDescription";
    private static final String HOURS_FIELD_SELECTOR = "#textFieldHours";
    private static final String START_DATE_PICKER_SELECTOR = "#datePickerStartDate";
    private static final String FINAL_DATE_PICKER_SELECTOR = "#datePickerFinalDate";
    private static final String REGISTER_BUTTON_SELECTOR = "#buttonRegister";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final int VALID_PROJECT_ID = 5;
    private static final int START_OFFSET_DAYS = 7;
    private static final String VALID_ACTIVITY_NAME = "Analisis de requerimientos";
    private static final String VALID_DESCRIPTION = "Documentacion de casos de uso";
    private static final String VALID_HOURS = "20";
    private static final String INVALID_ACTIVITY_NAME = "Tarea@Beta";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_ACTIVITY_DATA =
        VALID_ACTIVITY_NAME + FIELD_SEPARATOR + VALID_DESCRIPTION + FIELD_SEPARATOR + VALID_HOURS
        + FIELD_SEPARATOR + VALID_PROJECT_ID;

    private static final String EXPECTED_SUCCESS_MESSAGE = "Actividad registrada correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar la actividad";
    private static final String EXPECTED_INVALID_NAME_MESSAGE =
        "El nombre de la actividad no acepta carácteres especiales";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLRegisterActivityController registerController;
    private ActivityDAO activityDAOMock;
    private ProjectDAO projectDAOMock;

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
        activityDAOMock = mock(ActivityDAO.class);
        projectDAOMock = mock(ProjectDAO.class);

        injectField(ACTIVITY_DAO_FIELD, activityDAOMock);
        injectField(PROJECT_DAO_FIELD, projectDAOMock);
        injectField(CURRENT_PROJECT_ID_FIELD, VALID_PROJECT_ID);
        enableForm();
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws OperationException {
        when(activityDAOMock.registerActivity(any())).thenReturn(true);

        fillForm(VALID_ACTIVITY_NAME);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws OperationException {
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        when(activityDAOMock.registerActivity(any())).thenReturn(true);

        fillForm(VALID_ACTIVITY_NAME);
        clickRegister();

        verify(activityDAOMock).registerActivity(activityCaptor.capture());
        assertEquals(EXPECTED_ACTIVITY_DATA, activityData(activityCaptor.getValue()));
    }

    @Test
    void validateFields_registrationFails_showsErrorMessage() throws OperationException {
        when(activityDAOMock.registerActivity(any())).thenReturn(false);

        fillForm(VALID_ACTIVITY_NAME);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(activityDAOMock.registerActivity(any())).thenThrow(operationException);

        fillForm(VALID_ACTIVITY_NAME);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_invalidName_showsValidationError() {
        fillForm(INVALID_ACTIVITY_NAME);

        clickRegister();

        assertEquals(EXPECTED_INVALID_NAME_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLRegisterActivityController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(registerController, value);
    }

    private void enableForm() {
        interact(() -> {
            lookup(REGISTER_BUTTON_SELECTOR).query().setDisable(false);
            lookup(ACTIVITY_FIELD_SELECTOR).query().setDisable(false);
            lookup(DESCRIPTION_FIELD_SELECTOR).query().setDisable(false);
            lookup(HOURS_FIELD_SELECTOR).query().setDisable(false);
            lookup(START_DATE_PICKER_SELECTOR).query().setDisable(false);
            lookup(FINAL_DATE_PICKER_SELECTOR).query().setDisable(false);
        });
    }

    private void fillForm(String activityName) {
        LocalDate startDate = LocalDate.now().minusDays(START_OFFSET_DAYS);
        LocalDate endDate = LocalDate.now();

        clickOn(ACTIVITY_FIELD_SELECTOR).write(activityName);
        clickOn(DESCRIPTION_FIELD_SELECTOR).write(VALID_DESCRIPTION);
        clickOn(HOURS_FIELD_SELECTOR).write(VALID_HOURS);
        interact(() -> lookup(START_DATE_PICKER_SELECTOR).queryAs(DatePicker.class).setValue(startDate));
        interact(() -> lookup(FINAL_DATE_PICKER_SELECTOR).queryAs(DatePicker.class).setValue(endDate));
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String activityData(Activity activity) {
        String data = activity.getName() + FIELD_SEPARATOR + activity.getDescription() + FIELD_SEPARATOR
            + activity.getHoursReported() + FIELD_SEPARATOR + activity.getProjectId();
        return data;
    }
}