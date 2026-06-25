package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLAssignationProjectController;
import uv.lis.logic.dao.NotificationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLAssignationProjectControllerTest extends ApplicationTest {

    private static final String ASSIGN_VIEW_FXML = "/uv/lis/GUI/view/FXMLAssignationProject.fxml";
    private static final String REQUEST_PROJECT_DAO_FIELD = "requestProjectDAO";
    private static final String PROJECT_DAO_FIELD = "projectDAO";
    private static final String STUDENT_DAO_FIELD = "studentDAO";
    private static final String NOTIFICATION_DAO_FIELD = "notificationDAO";

    private static final String PROJECT_COMBO_SELECTOR = "#comboBoxProjects";
    private static final String APPLICANTS_LIST_SELECTOR = "#listViewApplicants";
    private static final String REASON_AREA_SELECTOR = "#textAreaReason";
    private static final String ASSIGN_BUTTON_SELECTOR = "#buttonAssignProject";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelError";

    private static final int PROJECT_ID = 5;
    private static final String PROJECT_NAME = "Sistema de Inventario";
    private static final String APPLICANT_ROW = "Ana Lopez - S24013305";
    private static final String VALID_REASON = "Buen desempeño academico";

    private static final String EXPECTED_SUCCESS_MESSAGE = "Asignación exitosa para S24013305";
    private static final String EXPECTED_ALREADY_ASSIGNED_MESSAGE = "El alumno ya tiene un proyecto asignado.";
    private static final String EXPECTED_NO_CAPACITY_MESSAGE = "El proyecto ya no tiene cupo disponible.";
    private static final String EXPECTED_EMPTY_REASON_MESSAGE = "El motivo de asignación no puede estar vacío";
    private static final String EXPECTED_NO_SELECTION_MESSAGE = "Seleccione un proyecto y un alumno de la lista.";

    private Stage primaryStage;
    private FXMLAssignationProjectController assignController;
    private RequestProjectDAO requestProjectDAOMock;
    private ProjectDAO projectDAOMock;
    private StudentDAO studentDAOMock;
    private NotificationDAO notificationDAOMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SessionManager.getInstance().setCurrentProfessor(buildCoordinatorProfessor());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(ASSIGN_VIEW_FXML));
        Parent root = loader.load();
        assignController = loader.getController();

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
    void setUpMocks() throws ReflectiveOperationException, OperationException {
        requestProjectDAOMock = mock(RequestProjectDAO.class);
        projectDAOMock = mock(ProjectDAO.class);
        studentDAOMock = mock(StudentDAO.class);
        notificationDAOMock = mock(NotificationDAO.class);

        when(requestProjectDAOMock.getAvailableProjects()).thenReturn(new ArrayList<>());

        injectField(REQUEST_PROJECT_DAO_FIELD, requestProjectDAOMock);
        injectField(PROJECT_DAO_FIELD, projectDAOMock);
        injectField(STUDENT_DAO_FIELD, studentDAOMock);
        injectField(NOTIFICATION_DAO_FIELD, notificationDAOMock);
    }

    @Test
    void assignStudent_validInput_showsSuccessMessage() throws OperationException {
        stubAvailableAssignment();
        when(requestProjectDAOMock.assignStudentToProject(anyString(), anyInt(), anyBoolean()))
            .thenReturn(true);

        selectProject();
        selectApplicant();
        setReason();
        clickAssign();

        assertEquals(EXPECTED_SUCCESS_MESSAGE, messageText());
    }

    @Test
    void assignStudent_studentAlreadyAssigned_showsErrorMessage() throws OperationException {
        when(projectDAOMock.getProjectByName(anyString())).thenReturn(Optional.of(buildProject()));
        when(studentDAOMock.hasProjectAssigned(anyString())).thenReturn(true);

        selectProject();
        selectApplicant();
        setReason();
        clickAssign();

        assertEquals(EXPECTED_ALREADY_ASSIGNED_MESSAGE, messageText());
    }

    @Test
    void assignStudent_projectWithoutCapacity_showsErrorMessage() throws OperationException {
        when(projectDAOMock.getProjectByName(anyString())).thenReturn(Optional.of(buildProject()));
        when(studentDAOMock.hasProjectAssigned(anyString())).thenReturn(false);
        when(requestProjectDAOMock.hasAvailableCapacity(anyInt())).thenReturn(false);

        selectProject();
        selectApplicant();
        setReason();
        clickAssign();

        assertEquals(EXPECTED_NO_CAPACITY_MESSAGE, messageText());
    }

    @Test
    void assignStudent_emptyReason_showsValidationError() throws OperationException {
        when(projectDAOMock.getProjectByName(anyString())).thenReturn(Optional.of(buildProject()));

        selectProject();
        selectApplicant();
        clickAssign();

        assertEquals(EXPECTED_EMPTY_REASON_MESSAGE, messageText());
    }

    @Test
    void assignStudent_noApplicantSelected_showsValidationError() throws OperationException {
        when(projectDAOMock.getProjectByName(anyString())).thenReturn(Optional.of(buildProject()));

        selectProject();
        setReason();
        clickAssign();

        assertEquals(EXPECTED_NO_SELECTION_MESSAGE, messageText());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLAssignationProjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(assignController, value);
    }

    private void stubAvailableAssignment() throws OperationException {
        when(projectDAOMock.getProjectByName(anyString())).thenReturn(Optional.of(buildProject()));
        when(studentDAOMock.hasProjectAssigned(anyString())).thenReturn(false);
        when(requestProjectDAOMock.hasAvailableCapacity(anyInt())).thenReturn(true);
    }

    private Project buildProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        return project;
    }

    private Professor buildCoordinatorProfessor() {
        Professor professor = new Professor();
        professor.setIsCoordinator(true);
        return professor;
    }

    @SuppressWarnings("unchecked")
    private void selectProject() {
        interact(() -> {
            ComboBox<String> comboBox = lookup(PROJECT_COMBO_SELECTOR).queryAs(ComboBox.class);
            comboBox.setValue(PROJECT_NAME);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @SuppressWarnings("unchecked")
    private void selectApplicant() {
        interact(() -> {
            ListView<String> listView = lookup(APPLICANTS_LIST_SELECTOR).queryAs(ListView.class);
            listView.getItems().setAll(APPLICANT_ROW);
            listView.getSelectionModel().select(0);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void setReason() {
        interact(() -> lookup(REASON_AREA_SELECTOR).queryAs(TextArea.class).setText(VALID_REASON));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickAssign() {
        clickOn(ASSIGN_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }
}