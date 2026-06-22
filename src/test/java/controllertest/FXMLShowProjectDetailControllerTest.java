package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLShowProjectDetailController;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class FXMLShowProjectDetailControllerTest extends ApplicationTest {

    private static final String SHOW_PROJECT_DETAIL_VIEW_FXML =
        "/uv/lis/GUI/view/FXMLShowProjectDetail.fxml";
    private static final String PROJECT_DAO_FIELD = "projectDAO";
    private static final String REQUEST_PROJECT_DAO_FIELD = "requestProjectDAO";

    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";
    private static final String BUTTON_MODIFY_SELECTOR = "#buttonModifyProject";
    private static final String BUTTON_SAVE_SELECTOR = "#buttonSave";
    private static final String BUTTON_INACTIVATE_SELECTOR = "#buttonInactivateProject";
    private static final String LABEL_NAME_SELECTOR = "#labelName";
    private static final String TEXTFIELD_NAME_SELECTOR = "#textFieldName";
    private static final String TEXTFIELD_DESCRIPTION_SELECTOR = "#textAreaDescription";
    private static final String TEXTFIELD_OBJECTIVE_SELECTOR = "#textFieldObjective";
    private static final String TEXTFIELD_CAPACITY_SELECTOR = "#textFieldCapacity";
    private static final String TEXTFIELD_METHODOLOGY_SELECTOR = "#textFieldMethodology";
    private static final int VALID_PROJECT_ID = 1;
    private static final String VALID_PROJECT_NAME = "Sistema de Gestión de Inventario para PYMES";
    private static final String VALID_PROJECT_DESCRIPTION = "Aplicación web con backend en Java Spring Boot y" 
        + " base de datos MySQL para reducir errores en el conteo de almacén";
    private static final String VALID_PROJECT_OBJECTIVE = "Desarrollar una plataforma web para el control" 
        + " automático de existencias en tiempo real";
    private static final String VALID_PROJECT_METHODOLOGY = "Scrum";
    private static final int VALID_PROJECT_CAPACITY = 2;
    private static final String INVALID_PROJECT_CAPACITY = "abc";
    private static final String VALID_ORGANIZATION_NAME = "Universidad Veracruzana";
    private static final int VALID_ORGANIZATION_ID = 10;

    private static final String UPDATED_PROJECT_NAME = "Eminus";
    private static final String UPDATED_PROJECT_DESCRIPTION = "Mejorar la experiencia de usuario de eminus";
    private static final String UPDATED_PROJECT_OBJECTIVE = "Mejorar la experiencia de usuario de eminus";
    private static final String UPDATED_PROJECT_METHODOLOGY = "ICONIX";
    private static final String UPDATED_PROJECT_CAPACITY = "1"; 

    private static final String EXPECTED_SUCCESS_UPDATE_MESSAGE = "Proyecto actualizado correctamente";
    private static final String EXPECTED_NO_CHANGES_MESSAGE = "No se realizaron cambios en el proyecto";
    private static final String EXPECTED_INACTIVATE_WITH_STUDENTS_MESSAGE =
        "No se puede inactivar el proyecto porque tiene alumnos asignados";
    private static final String EXPECTED_OPERATION_ERROR_MESSAGE = "Error de operación de prueba";
    private static final String EXPECTED_NAME_VALIDATION_MESSAGE = "El nombre del proyecto";

    private Stage primaryStage;
    private FXMLShowProjectDetailController controller;
    private ProjectDAO projectDAOMock;
    private RequestProjectDAO requestProjectDAOMock;

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(SHOW_PROJECT_DETAIL_VIEW_FXML));

        try {
            Parent root = loader.load();
            controller = loader.getController();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @BeforeEach
    void setUpMocks() throws OperationException {
        projectDAOMock = mock(ProjectDAO.class);
        requestProjectDAOMock = mock(RequestProjectDAO.class);

        when(requestProjectDAOMock.getAssignedStudentsByProjectId(anyInt()))
            .thenReturn(new ArrayList<>());

        injectField(PROJECT_DAO_FIELD, projectDAOMock, controller);
        injectField(REQUEST_PROJECT_DAO_FIELD, requestProjectDAOMock, controller);

        interact(() -> {
            controller.initializeData(buildActiveProject());
            clearLabelMessage();
        });
        WaitForAsyncUtils.waitForFxEvents();
}

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void initializeData_activeProject_modifyButtonIsEnabled() {
        Button buttonModify = lookup(BUTTON_MODIFY_SELECTOR).queryAs(Button.class);

        assertFalse(buttonModify.isDisable());
    }

    @Test
    void initializeData_inactiveProject_modifyButtonIsDisabled() {
        interact(() -> controller.initializeData(buildInactiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        Button buttonModify = lookup(BUTTON_MODIFY_SELECTOR).queryAs(Button.class);

        assertTrue(buttonModify.isDisable());
    }

    @Test
    void initializeData_inactiveProject_inactivateButtonIsDisabled() {
        interact(() -> controller.initializeData(buildInactiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        Button buttonInactivate = lookup(BUTTON_INACTIVATE_SELECTOR).queryAs(Button.class);

        assertTrue(buttonInactivate.isDisable());
    }

    @Test
    void initializeData_projectWithStudents_studentsAreLoadedInListView() throws OperationException {
        ArrayList<Student> assignedStudents = new ArrayList<>();
        assignedStudents.add(buildStudent());
        when(requestProjectDAOMock.getAssignedStudentsByProjectId(anyInt())).thenReturn(assignedStudents);

        interact(() -> controller.initializeData(buildActiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, lookup("#listViewStudent").queryListView().getItems().size());
    }

    @Test
    void initializeData_daoThrowsOperationException_showsErrorMessage() throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(requestProjectDAOMock.getAssignedStudentsByProjectId(anyInt())).thenThrow(operationException);

        interact(() -> controller.initializeData(buildActiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void enableEditMode_activeProject_labelsAreHidden() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(lookup(LABEL_NAME_SELECTOR).queryAs(Label.class).isVisible());
    }

    @Test
    void enableEditMode_activeProject_textFieldsAreVisible() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(lookup(TEXTFIELD_NAME_SELECTOR).queryAs(TextField.class).isVisible());
    }

    @Test
    void enableEditMode_activeProject_saveButtonIsVisible() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(lookup(BUTTON_SAVE_SELECTOR).queryAs(Button.class).isVisible());
    }

    @Test
    void enableEditMode_activeProject_modifyButtonIsHidden() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(lookup(BUTTON_MODIFY_SELECTOR).queryAs(Button.class).isVisible());
    }

    @Test
    void saveProject_emptyName_showsValidationError() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        clearTextField(TEXTFIELD_NAME_SELECTOR);
        fillTextField(TEXTFIELD_DESCRIPTION_SELECTOR, VALID_PROJECT_DESCRIPTION);
        fillTextField(TEXTFIELD_OBJECTIVE_SELECTOR, VALID_PROJECT_OBJECTIVE);
        fillTextField(TEXTFIELD_CAPACITY_SELECTOR, String.valueOf(VALID_PROJECT_CAPACITY));
        fillTextField(TEXTFIELD_METHODOLOGY_SELECTOR, VALID_PROJECT_METHODOLOGY);

        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(messageText().contains(EXPECTED_NAME_VALIDATION_MESSAGE));
    }

    @Test
    void saveProject_invalidCapacity_showsValidationError() {
        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillTextField(TEXTFIELD_NAME_SELECTOR, VALID_PROJECT_NAME);
        fillTextField(TEXTFIELD_DESCRIPTION_SELECTOR, VALID_PROJECT_DESCRIPTION);
        fillTextField(TEXTFIELD_OBJECTIVE_SELECTOR, VALID_PROJECT_OBJECTIVE);
        fillTextField(TEXTFIELD_METHODOLOGY_SELECTOR, VALID_PROJECT_METHODOLOGY);
        clearTextField(TEXTFIELD_CAPACITY_SELECTOR);
        fillTextField(TEXTFIELD_CAPACITY_SELECTOR, INVALID_PROJECT_CAPACITY);

        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(messageText().contains("El cupo"));
    }

    @Test
    void saveProject_validData_callsModifyProject() throws OperationException {
        when(projectDAOMock.modifyProject(any(Project.class))).thenReturn(true);

        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillAllEditFields();
        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        verify(projectDAOMock).modifyProject(any(Project.class));
    }

    @Test
    void saveProject_modifyReturnsTrue_showsSuccessMessage() throws OperationException {
        when(projectDAOMock.modifyProject(any(Project.class))).thenReturn(true);

        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillAllEditFields();
        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_SUCCESS_UPDATE_MESSAGE, messageText());
    }

    @Test
    void saveProject_modifyReturnsTrue_labelsAreRestoredAfterSave() throws OperationException {
        when(projectDAOMock.modifyProject(any(Project.class))).thenReturn(true);

        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillAllEditFields();
        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(lookup(LABEL_NAME_SELECTOR).queryAs(Label.class).isVisible());
    }

    @Test
    void saveProject_modifyReturnsFalse_showsNoChangesMessage() throws OperationException {
        when(projectDAOMock.modifyProject(any(Project.class))).thenReturn(false);

        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillAllEditFields();
        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_NO_CHANGES_MESSAGE, messageText());
    }

    @Test
    void saveProject_daoThrowsOperationException_showsErrorMessage() throws OperationException {
        OperationException operationException =
            new OperationException(EXPECTED_OPERATION_ERROR_MESSAGE, null);
        when(projectDAOMock.modifyProject(any(Project.class))).thenThrow(operationException);

        clickOn(BUTTON_MODIFY_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        fillAllEditFields();
        clickOn(BUTTON_SAVE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_OPERATION_ERROR_MESSAGE, messageText());
    }

    @Test
    void handleInactivateProject_projectHasAssignedStudents_showsErrorMessage() throws OperationException {
        ArrayList<Student> assignedStudents = new ArrayList<>();
        assignedStudents.add(buildStudent());
        when(requestProjectDAOMock.getAssignedStudentsByProjectId(anyInt())).thenReturn(assignedStudents);

        interact(() -> controller.initializeData(buildActiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> clearLabelMessage());
        clickOn(BUTTON_INACTIVATE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(EXPECTED_INACTIVATE_WITH_STUDENTS_MESSAGE, messageText());
    }

    @Test
    void handleInactivateProject_projectHasAssignedStudents_doesNotCallInactivateDAO() throws OperationException {
        ArrayList<Student> assignedStudents = new ArrayList<>();
        assignedStudents.add(buildStudent());
        when(requestProjectDAOMock.getAssignedStudentsByProjectId(anyInt())).thenReturn(assignedStudents);

        interact(() -> controller.initializeData(buildActiveProject()));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(BUTTON_INACTIVATE_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();

        verify(projectDAOMock, never()).inactivateProject(any(Project.class));
    }

    private Project buildActiveProject() {
        Project project = new Project();
        project.setId(VALID_PROJECT_ID);
        project.setName(VALID_PROJECT_NAME);
        project.setDescription(VALID_PROJECT_DESCRIPTION);
        project.setObjective(VALID_PROJECT_OBJECTIVE);
        project.setCapacity(VALID_PROJECT_CAPACITY);
        project.setMethodology(VALID_PROJECT_METHODOLOGY);
        project.setAffiliatedOrganizationName(VALID_ORGANIZATION_NAME);
        project.setIdAffiliatedOrganization(VALID_ORGANIZATION_ID);
        project.setActive(true);
        return project;
    }

    private Project buildInactiveProject() {
        Project project = buildActiveProject();
        project.setActive(false);
        return project;
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent("S24013322");
        student.setFirstName("Denisse");
        student.setLastName("Reyes");
        return student;
    }

    private void injectField(String fieldName, Object value,
            FXMLShowProjectDetailController target) throws RuntimeException {
        try {
            Field field = FXMLShowProjectDetailController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private void fillAllEditFields() {
        fillTextField(TEXTFIELD_NAME_SELECTOR, UPDATED_PROJECT_NAME);
        fillTextField(TEXTFIELD_DESCRIPTION_SELECTOR, UPDATED_PROJECT_DESCRIPTION);
        fillTextField(TEXTFIELD_OBJECTIVE_SELECTOR, UPDATED_PROJECT_OBJECTIVE);
        fillTextField(TEXTFIELD_CAPACITY_SELECTOR, UPDATED_PROJECT_CAPACITY);
        fillTextField(TEXTFIELD_METHODOLOGY_SELECTOR, UPDATED_PROJECT_METHODOLOGY);
    }

    private void fillTextField(String selector, String text) {
        interact(() -> lookup(selector).queryTextInputControl().clear());
        clickOn(selector).write(text);
    }

    private void clearTextField(String selector) {
        interact(() -> lookup(selector).queryTextInputControl().clear());
    }

    private void clearLabelMessage() {
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }

}