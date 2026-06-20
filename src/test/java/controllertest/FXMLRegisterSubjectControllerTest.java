package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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

import uv.lis.GUI.controller.FXMLRegisterSubjectController;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public class FXMLRegisterSubjectControllerTest extends ApplicationTest {

    private static final String REGISTER_VIEW_FXML = "/uv/lis/GUI/view/FXMLRegisterSubject.fxml";
    private static final String SUBJECT_DAO_FIELD = "subjectDAO";
    private static final String PROFESSOR_DAO_FIELD = "professorDAO";
    private static final String SCHOOL_PERIOD_DAO_FIELD = "schoolPeriodDAO";
    private static final String PROFESSORS_MAP_FIELD = "professorsMap";

    private static final String NRC_FIELD_SELECTOR = "#textFieldNRC";
    private static final String PROFESSOR_COMBO_SELECTOR = "#comboBoxProfessorName";
    private static final String PERIOD_COMBO_SELECTOR = "#comboBoxPeriodName";
    private static final String SECTION_COMBO_SELECTOR = "#comboBoxSection";
    private static final String REGISTER_BUTTON_SELECTOR = "#buttonRegister";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String VALID_NRC = "54321";
    private static final String VALID_PROFESSOR = "Juan Perez";
    private static final String VALID_PERSONNEL = "12345";
    private static final String VALID_PERIOD = "202651";
    private static final String VALID_PERIOD_ID = "1";
    private static final int EXPECTED_PERIOD_ID = 1;
    private static final String VALID_SECTION = "1";
    private static final String EMPTY_NRC = "";

    private static final String FIELD_SEPARATOR = "|";
    private static final String EXPECTED_SUBJECT_DATA =
        VALID_NRC + FIELD_SEPARATOR + VALID_SECTION + FIELD_SEPARATOR + VALID_PERSONNEL
        + FIELD_SEPARATOR + EXPECTED_PERIOD_ID;

    private static final String EXPECTED_SECTION_TAKEN_MESSAGE =
        "La sección seleccionada ya está asignada en este periodo escolar.";
    private static final String EXPECTED_EMPTY_NRC_MESSAGE = "El NRC no puede estar vacío";
    private static final String DUPLICATE_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLRegisterSubjectController registerController;
    private SubjectDAO subjectDAOMock;
    private ProfessorDAO professorDAOMock;
    private SchoolPeriodDAO schoolPeriodDAOMock;

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
        subjectDAOMock = mock(SubjectDAO.class);
        professorDAOMock = mock(ProfessorDAO.class);
        schoolPeriodDAOMock = mock(SchoolPeriodDAO.class);

        injectField(SUBJECT_DAO_FIELD, subjectDAOMock);
        injectField(PROFESSOR_DAO_FIELD, professorDAOMock);
        injectField(SCHOOL_PERIOD_DAO_FIELD, schoolPeriodDAOMock);
        injectField(PROFESSORS_MAP_FIELD, buildProfessorsMap());

        when(schoolPeriodDAOMock.getSchoolPeriodIdByName(VALID_PERIOD)).thenReturn(Optional.of(VALID_PERIOD_ID));
    }

    @Test
    void validateFields_validData_registersSubject() throws Exception {
        when(subjectDAOMock.isSectionTakenInPeriod(anyInt(), anyString())).thenReturn(false);
        when(subjectDAOMock.registerSubject(any())).thenReturn(true);

        fillForm(VALID_NRC);
        clickRegister();

        verify(subjectDAOMock).registerSubject(any());
    }

    @Test
    void validateFields_validData_sendsEnteredDataToDAO() throws Exception {
        ArgumentCaptor<Subject> subjectCaptor = ArgumentCaptor.forClass(Subject.class);
        when(subjectDAOMock.isSectionTakenInPeriod(anyInt(), anyString())).thenReturn(false);
        when(subjectDAOMock.registerSubject(any())).thenReturn(true);

        fillForm(VALID_NRC);
        clickRegister();

        verify(subjectDAOMock).registerSubject(subjectCaptor.capture());
        assertEquals(EXPECTED_SUBJECT_DATA, subjectData(subjectCaptor.getValue()));
    }

    @Test
    void validateFields_sectionAlreadyTaken_showsErrorMessage() throws Exception {
        when(subjectDAOMock.isSectionTakenInPeriod(anyInt(), anyString())).thenReturn(true);

        fillForm(VALID_NRC);
        clickRegister();

        assertEquals(EXPECTED_SECTION_TAKEN_MESSAGE, messageText());
    }

    @Test
    void validateFields_operationFails_showsExceptionMessage() throws Exception {
        OperationException operationException = new OperationException(DUPLICATE_ERROR_MESSAGE, null);
        when(subjectDAOMock.isSectionTakenInPeriod(anyInt(), anyString())).thenReturn(false);
        when(subjectDAOMock.registerSubject(any())).thenThrow(operationException);

        fillForm(VALID_NRC);
        clickRegister();

        assertEquals(operationException.getMessage(), messageText());
    }

    @Test
    void validateFields_emptyNrc_showsValidationError() {
        fillForm(EMPTY_NRC);

        clickRegister();

        assertEquals(EXPECTED_EMPTY_NRC_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLRegisterSubjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(registerController, value);
    }

    private LinkedHashMap<String, String> buildProfessorsMap() {
        LinkedHashMap<String, String> professorsMap = new LinkedHashMap<>();
        professorsMap.put(VALID_PROFESSOR, VALID_PERSONNEL);
        return professorsMap;
    }

    private void fillForm(String nrc) {
        clickOn(NRC_FIELD_SELECTOR).write(nrc);
        interact(() -> setComboBoxValue(PERIOD_COMBO_SELECTOR, VALID_PERIOD));
        interact(() -> setComboBoxValue(PROFESSOR_COMBO_SELECTOR, VALID_PROFESSOR));
        interact(() -> setComboBoxValue(SECTION_COMBO_SELECTOR, VALID_SECTION));
    }

    @SuppressWarnings("unchecked")
    private void setComboBoxValue(String selector, String value) {
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        comboBox.setValue(value);
    }

    private void clickRegister() {
        clickOn(REGISTER_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }

    private String subjectData(Subject subject) {
        String data = subject.getNrc() + FIELD_SEPARATOR + subject.getSection() + FIELD_SEPARATOR
            + subject.getProfessorPersonnelNumber() + FIELD_SEPARATOR + subject.getSchoolPeriodId();
        return data;
    }
}