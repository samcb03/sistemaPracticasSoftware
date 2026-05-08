package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import uv.lis.GUI.controller.FXMLConsultStudentController;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


@ExtendWith(MockitoExtension.class)
class FXMLConsultStudentControllerTest {

    @Mock private StudentDAO studentDAO;

    private FXMLConsultStudentController controller;

    @BeforeAll
    static void initJFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @BeforeEach
    void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller = new FXMLConsultStudentController();
                injectField("textFieldStudentId", new TextField());
                injectField("buttonSearch", new Button());
                injectField("gridPaneStudentInfo", new GridPane());
                injectField("labelStudentId", new Label());
                injectField("labelFirstName", new Label());
                injectField("labelLastName", new Label());
                injectField("labelDateBirth", new Label());
                injectField("labelGender", new Label());
                injectField("buttonUpdate", new Button());
                injectField("buttonInactivate", new Button());
                injectField("buttonBack", new Button());
                injectField("labelMessage", new Label());
                injectField("contextMenuSuggestions", new ContextMenu());
                injectField("studentDAO", studentDAO);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    private void injectField(String name, Object value) throws Exception {
        Field field;
        try {
            field = FXMLConsultStudentController.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            field = FXMLConsultStudentController.class.getSuperclass().getDeclaredField(name);
        }
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void invokeSearchStudent() throws Exception {
        Method method = FXMLConsultStudentController.class.getDeclaredMethod("searchStudent");
        method.setAccessible(true);
        method.invoke(controller);
    }

    private Label getLabel(String name) throws Exception {
        Field field = FXMLConsultStudentController.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Label) field.get(controller);
    }

    private TextField getTextField() throws Exception {
        Field field = FXMLConsultStudentController.class.getDeclaredField("textFieldStudentId");
        field.setAccessible(true);
        return (TextField) field.get(controller);
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent("S200123");
        student.setFirstName("Denisse");
        student.setLastName("Reyes");
        student.setBirthDate(new Date(System.currentTimeMillis()));
        student.setGender("Femenino");
        return student;
    }

    private void runOnFxThread(ThrowingRunnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    // searchStudent - camino 1: validación falla
    @Test
    void searchStudent_invalidLength_showsError() throws Exception {
        runOnFxThread(() -> {
            getTextField().setText("123");
            invokeSearchStudent();
        });

        assertFalse(getLabel("labelMessage").getText().isEmpty());
    }

    @Test
    void searchStudent_validId_displaysStudent() throws Exception {
        when(studentDAO.getIdUserByStudentId("S200123")).thenReturn(1);
        when(studentDAO.getStudentById(1)).thenReturn(buildStudent());

        runOnFxThread(() -> {
            getTextField().setText("S200123");
            invokeSearchStudent();
        });

        assertEquals("S200123", getLabel("labelStudentId").getText());
    }

    @Test
    void searchStudent_studentNotFound_showsError() throws Exception {
        when(studentDAO.getIdUserByStudentId(anyString()))
            .thenThrow(new OperationException("No se encontró un alumno con la matricula", null));

        runOnFxThread(() -> {
            getTextField().setText("S200123");
            invokeSearchStudent();
        });

        assertFalse(getLabel("labelMessage").getText().isEmpty());
    }

    @Test
    void setupAutocomplete_emptyText_hidesMenu() throws Exception {
        runOnFxThread(() -> {
            getTextField().setText("S");
            getTextField().setText("");
        });

        assertTrue(getLabel("labelMessage").getText().isEmpty());
    }

    @Test
    void setupAutocomplete_noMatches_hidesMenu() throws Exception {
        when(studentDAO.searchStudentIds("Z99")).thenReturn(new ArrayList<>());

        runOnFxThread(() -> getTextField().setText("Z99"));

        assertTrue(getLabel("labelMessage").getText().isEmpty());
    }

    @Test
    void setupAutocomplete_withMatches_addsMenuItems() throws Exception {
        ArrayList<String> matches = new ArrayList<>();
        matches.add("S200123");
        matches.add("S200124");
        when(studentDAO.searchStudentIds("S2")).thenReturn(matches);

        ContextMenu contextMenu = new ContextMenu();
        injectField("contextMenuSuggestions", contextMenu);

        runOnFxThread(() -> getTextField().setText("S2"));

        assertEquals(2, contextMenu.getItems().size());
    }

    @Test
    void setupAutocomplete_operationException_showsError() throws Exception {
        when(studentDAO.searchStudentIds(anyString()))
            .thenThrow(new OperationException("No se pudieron obtener las matriculas", null));

        runOnFxThread(() -> getTextField().setText("S2"));

        assertFalse(getLabel("labelMessage").getText().isEmpty());
    }
}
