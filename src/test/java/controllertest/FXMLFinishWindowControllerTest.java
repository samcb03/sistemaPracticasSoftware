package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import uv.lis.GUI.controller.FXMLFinishWindowController;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.ProjectSummary;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLFinishWindowControllerTest extends ApplicationTest {

    private static final String FINISH_VIEW_FXML = "/uv/lis/GUI/view/FXMLFinishWindow.fxml";
    private static final String PROJECT_DAO_FIELD = "projectDAO";
    private static final String PRACTICE_DAO_FIELD = "practiceDAO";
    private static final String EXPEDIENT_DAO_FIELD = "expedientDAO";
    private static final String ACTIVITY_DAO_FIELD = "activityDAO";

    private static final String COMPLETED_HOURS_SELECTOR = "#labelCompletedHours";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";
    private static final String STUDENT_NAME_SELECTOR = "#labelStudentName";
    private static final String GRADE_SELECTOR = "#labelGrade";
    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_FIRST_NAME = "Juan";
    private static final String VALID_LAST_NAME = "Pérez";
    private static final String VALID_GRADE = "90";
    private static final int VALID_TOTAL_HOURS = 420;
    private static final String DAO_ERROR_MESSAGE = "Error de operación de prueba";

    private Stage primaryStage;
    private FXMLFinishWindowController finishController;
    private ProjectDAO projectDAOMock;
    private PracticeDAO practiceDAOMock;
    private ExpedientDAO expedientDAOMock;
    private ActivityDAO activityDAOMock;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        Student student = buildStudent();
        SessionManager.getInstance().setCurrentStudent(student);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FINISH_VIEW_FXML));
        Parent root = loader.load();
        finishController = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws ReflectiveOperationException {
        projectDAOMock = mock(ProjectDAO.class);
        practiceDAOMock = mock(PracticeDAO.class);
        expedientDAOMock = mock(ExpedientDAO.class);
        activityDAOMock = mock(ActivityDAO.class);

        injectField(PROJECT_DAO_FIELD, projectDAOMock);
        injectField(PRACTICE_DAO_FIELD, practiceDAOMock);
        injectField(EXPEDIENT_DAO_FIELD, expedientDAOMock);
        injectField(ACTIVITY_DAO_FIELD, activityDAOMock);
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
    }

    @Test
    void loadSummary_studentInSession_showsStudentName() {
        String studentName = lookup(STUDENT_NAME_SELECTOR).queryAs(Label.class).getText();
        assertEquals(VALID_FIRST_NAME + " " + VALID_LAST_NAME, studentName);
    }

    @Test
    void loadSummary_projectFound_showsGrade() throws OperationException {
        when(projectDAOMock.getProjectDetailsByStudentId(anyString()))
            .thenReturn(Optional.of(buildProjectSummary()));
        when(practiceDAOMock.getFinalGrade(anyString())).thenReturn(VALID_GRADE);

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(VALID_GRADE, lookup(GRADE_SELECTOR).queryAs(Label.class).getText());
    }

    @Test
    void loadSummary_noProjectAssigned_showsErrorMessage() throws OperationException {
        when(projectDAOMock.getProjectDetailsByStudentId(anyString())).thenReturn(Optional.empty());

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("No se encontró un proyecto asignado", messageText());
    }

    @Test
    void loadSummary_daoError_showsExceptionMessage() throws OperationException {
        OperationException operationException = new OperationException(DAO_ERROR_MESSAGE, null);
        when(projectDAOMock.getProjectDetailsByStudentId(anyString())).thenThrow(operationException);

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(DAO_ERROR_MESSAGE, messageText());
    }

    @Test
    void loadActivities_activitiesFound_showsCorrectHours() throws OperationException {
        when(activityDAOMock.getActivitiesByStudentId(anyString())).thenReturn(buildActivities());
        when(activityDAOMock.getTotalActivityHoursByStudent(anyString())).thenReturn(VALID_TOTAL_HOURS);

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(String.valueOf(VALID_TOTAL_HOURS),
            lookup(COMPLETED_HOURS_SELECTOR).queryAs(Label.class).getText());
    }

    @Test
    void loadActivities_daoError_showsErrorMessage() throws OperationException {
        OperationException operationException = new OperationException(DAO_ERROR_MESSAGE, null);
        when(activityDAOMock.getActivitiesByStudentId(anyString())).thenThrow(operationException);

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("No se pudieron cargar las actividades", messageText());
    }

    @Test
    void loadDocuments_daoError_showsErrorMessage() throws OperationException {
        OperationException operationException = new OperationException(DAO_ERROR_MESSAGE, null);
        when(expedientDAOMock.getDocumentsByStudentId(anyString())).thenThrow(operationException);

        interact(() -> finishController.initialize(null, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("No se pudieron cargar los documentos", messageText());
    }

    private void injectField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = FXMLFinishWindowController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(finishController, value);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_FIRST_NAME);
        student.setLastName(VALID_LAST_NAME);
        return student;
    }

    private ProjectSummary buildProjectSummary() {
        ProjectSummary summary = new ProjectSummary();
        summary.setProjectName("Sistema de Gestión");
        summary.setOrganizationName("Empresa XYZ");
        summary.setProfessorName("Dr. García");
        summary.setMethodology("Scrum");
        summary.setObjective("Desarrollar el sistema");
        summary.setDescription("Descripción del proyecto");
        return summary;
    }

    private List<Activity> buildActivities() {
        Activity activity1 = new Activity();
        activity1.setId(1);
        activity1.setName("Requisitos");
        activity1.setDescription("Levantamiento de requisitos");
        activity1.setStartDate(LocalDate.of(2026, 3, 2));
        activity1.setEndDate(LocalDate.of(2026, 3, 6));
        Activity activity2 = new Activity();
        activity2.setId(2);
        activity2.setName("Diseño de BD");
        activity2.setDescription("Diseño de base de datos");
        activity2.setStartDate(LocalDate.of(2026, 4, 1));
        activity2.setEndDate(LocalDate.of(2026, 6, 5));
        return List.of(activity1, activity2);
    }

    private String messageText() {
        String message = lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
        return message;
    }
}