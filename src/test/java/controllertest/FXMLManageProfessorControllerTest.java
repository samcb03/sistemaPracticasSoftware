package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import uv.lis.GUI.controller.FXMLManageProfessorController;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class FXMLManageProfessorControllerTest extends ApplicationTest {

    private static final String MANAGE_VIEW_FXML = "/uv/lis/GUI/view/FXMLManageProfessor.fxml";
    private static final String PROFESSOR_DAO_FIELD = "professorDAO";

    private static final String SEARCH_FIELD_SELECTOR = "#textFieldProfessorPersonnelNumber";
    private static final String FIRST_NAME_FIELD_SELECTOR = "#textFieldFirstName";
    private static final String SEARCH_BUTTON_SELECTOR = "#buttonSearch";
    private static final String UPDATE_BUTTON_SELECTOR = "#buttonUpdate";
    private static final String SAVE_BUTTON_SELECTOR = "#buttonSave";
    private static final String FIRST_NAME_LABEL_SELECTOR = "#labelFirstName";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final int PROFESSOR_ID = 4;
    private static final int VALID_USER_ID = 10;
    private static final String VALID_PERSONNEL = "33621";
    private static final String VALID_FIRST_NAME = "Juan";
    private static final String VALID_LAST_NAME = "Perez";
    private static final String EMPTY_FIRST_NAME = "";

    private static final String EXPECTED_NOT_FOUND_MESSAGE = "No se encontró al profesor";
    private static final String EXPECTED_UPDATE_SUCCESS_MESSAGE = "Profesor actualizado correctamente";
    private static final String EXPECTED_NO_CHANGES_MESSAGE = "No se realizaron cambios en el profesor";
    private static final String EXPECTED_EMPTY_NAME_MESSAGE = "El nombre no puede estar vacío";

    private Stage primaryStage;
    private FXMLManageProfessorController manageController;
    private ProfessorDAO professorDAOMock;

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
        professorDAOMock = mock(ProfessorDAO.class);
        injectField(PROFESSOR_DAO_FIELD, professorDAOMock);
        when(professorDAOMock.searchProfessorPersonalNumbers(anyString())).thenReturn(new ArrayList<>());
    }

    @Test
    void searchProfessor_found_displaysFirstName() throws OperationException {
        stubFoundSearch();

        performSearch();

        assertEquals(VALID_FIRST_NAME, firstNameLabelText());
    }

    @Test
    void searchProfessor_notFound_showsErrorMessage() throws OperationException {
        when(professorDAOMock.getProfessorById(anyInt())).thenReturn(Optional.empty());

        performSearch();

        assertEquals(EXPECTED_NOT_FOUND_MESSAGE, messageText());
    }

    @Test
    void saveProfessor_validData_showsSuccessMessage() throws OperationException {
        stubFoundSearch();
        when(professorDAOMock.modifyProfessor(any())).thenReturn(true);

        performSearch();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_UPDATE_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void saveProfessor_noChangesApplied_showsErrorMessage() throws OperationException {
        stubFoundSearch();
        when(professorDAOMock.modifyProfessor(any())).thenReturn(false);

        performSearch();
        enterEditMode();
        clickSave();

        assertEquals(EXPECTED_NO_CHANGES_MESSAGE, messageText());
    }

    @Test
    void saveProfessor_emptyFirstName_showsValidationError() throws OperationException {
        stubFoundSearch();

        performSearch();
        enterEditMode();
        interact(() -> lookup(FIRST_NAME_FIELD_SELECTOR).queryAs(TextField.class).setText(EMPTY_FIRST_NAME));
        clickSave();

        assertEquals(EXPECTED_EMPTY_NAME_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = FXMLManageProfessorController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manageController, value);
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setId(PROFESSOR_ID);
        professor.setPersonnelNumber(VALID_PERSONNEL);
        professor.setFirstName(VALID_FIRST_NAME);
        professor.setLastName(VALID_LAST_NAME);
        professor.setIsCoordinator(false);
        return professor;
    }

    private void stubFoundSearch() throws OperationException {
        when(professorDAOMock.getIdUserByProfessorPersonnelNumber(anyString())).thenReturn(VALID_USER_ID);
        when(professorDAOMock.getProfessorById(anyInt())).thenReturn(Optional.of(buildProfessor()));
        when(professorDAOMock.isProfessorInactive(anyString())).thenReturn(false);
        when(professorDAOMock.getSubjectsByProfessor(anyString())).thenReturn(new ArrayList<>());
    }

    private void performSearch() {
        clickOn(SEARCH_FIELD_SELECTOR).write(VALID_PERSONNEL);
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

    private String firstNameLabelText() {
        String firstName = lookup(FIRST_NAME_LABEL_SELECTOR).queryAs(Label.class).getText();
        return firstName;
    }
}