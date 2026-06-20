package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.PERIOD_TERM_SPRING;

import java.lang.reflect.Field;
import java.time.LocalDate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLRegisterSchoolPeriodController;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterSchoolPeriodControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterSchoolPeriod.fxml";
    private static final String SCHOOL_PERIOD_DAO_FIELD = "schoolPeriodDAO";

    private static final String TERM_COMBO_SELECTOR = "#comboBoxTerm";
    private static final String START_DATE_PICKER_SELECTOR = "#datePickerStartDate";
    private static final String END_DATE_PICKER_SELECTOR = "#datePickerEndDate";
    private static final String REGISTER_BUTTON_TEXT = "Registrar Periodo";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final LocalDate VALID_START_DATE = LocalDate.of(2026, 2, 9);
    private static final LocalDate VALID_END_DATE = LocalDate.of(2026, 7, 3);
    private static final LocalDate INVALID_START_DATE = LocalDate.of(2026, 3, 9);
    private static final String EXPECTED_PERIOD_NAME = "202651";

    private static final String EXPECTED_SUCCESS_MESSAGE = "Periodo escolar registrado correctamente";
    private static final String EXPECTED_FAILURE_MESSAGE = "Error al registrar el periodo escolar";
    private static final String EXPECTED_INVALID_START_MESSAGE =
        "La fecha de inicio debe iniciar en febrero para el periodo 51";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private FXMLRegisterSchoolPeriodController registerController;
    private SchoolPeriodDAO schoolPeriodDAOMock;

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
        schoolPeriodDAOMock = mock(SchoolPeriodDAO.class);
        injectSchoolPeriodDAO(schoolPeriodDAOMock);
    }

    @Test
    void validateFields_validData_showsSuccessMessage() throws OperationException {
        when(schoolPeriodDAOMock.registerSchoolPeriod(any())).thenReturn(true);

        fillForm(VALID_START_DATE);
        clickRegister();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void validateFields_validData_sendsGeneratedPeriodCodeToDAO() throws OperationException {
        ArgumentCaptor<SchoolPeriod> periodCaptor = ArgumentCaptor.forClass(SchoolPeriod.class);
        when(schoolPeriodDAOMock.registerSchoolPeriod(any())).thenReturn(true);

        fillForm(VALID_START_DATE);
        clickRegister();

        verify(schoolPeriodDAOMock).registerSchoolPeriod(periodCaptor.capture());
        assertEquals(EXPECTED_PERIOD_NAME, periodCaptor.getValue().getName());
    }

    @Test
    void validateFields_registrationFails_showsOperationExceptionMessage() throws OperationException {
        when(schoolPeriodDAOMock.registerSchoolPeriod(any())).thenReturn(false);

        fillForm(VALID_START_DATE);
        clickRegister();

        assertEquals(EXPECTED_FAILURE_MESSAGE, messageText());
    }

    @Test
    void validateFields_duplicateRecord_showsOperationExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(schoolPeriodDAOMock.registerSchoolPeriod(any())).thenThrow(operationException);

        fillForm(VALID_START_DATE);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_startDateNotInTermMonth_showsValidationError() {
        fillForm(INVALID_START_DATE);

        clickRegister();

        assertEquals(EXPECTED_INVALID_START_MESSAGE, messageText());
    }

    private void injectSchoolPeriodDAO(SchoolPeriodDAO schoolPeriodDAOInstance) throws Exception {
        Field schoolPeriodDAOField =
            FXMLRegisterSchoolPeriodController.class.getDeclaredField(SCHOOL_PERIOD_DAO_FIELD);
        schoolPeriodDAOField.setAccessible(true);
        schoolPeriodDAOField.set(registerController, schoolPeriodDAOInstance);
    }

    private void fillForm(LocalDate startDate) {
        interact(() -> setComboBoxValue(TERM_COMBO_SELECTOR, PERIOD_TERM_SPRING));
        interact(() -> lookup(START_DATE_PICKER_SELECTOR).queryAs(DatePicker.class).setValue(startDate));
        interact(() -> lookup(END_DATE_PICKER_SELECTOR).queryAs(DatePicker.class).setValue(VALID_END_DATE));
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
}