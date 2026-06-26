package controllertest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.EventQueue;
import javafx.stage.Window;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import uv.lis.GUI.controller.FXMLGenerateAutoevaluationController;
import uv.lis.logic.common.AutoevaluationCommon;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateAutoevaluationControllerTest extends ApplicationTest {
    private static final String AUTOEVALUATION_VIEW_FXML = "/uv/lis/GUI/view/FXMLGenerateAutoevaluation.fxml";
    private static final String AUTOEVALUATION_COMMON_FIELD = "autoevaluationCommon";
    private static final String AUTOEVALUATION_DAO_FIELD = "autoevaluationDAO";
    private static final String EXPEDIENT_DAO_FIELD = "expedientDAO";

    private static final String GENERATE_BUTTON_SELECTOR = "#buttonGenerate";
    private static final String MESSAGE_LABEL_SELECTOR = "#labelMessage";

    private static final String RADIO_1_1 = "#radioButton1_1";
    private static final String RADIO_2_1 = "#radioButton2_1";
    private static final String RADIO_3_1 = "#radioButton3_1";
    private static final String RADIO_4_1 = "#radioButton4_1";
    private static final String RADIO_5_1 = "#radioButton5_1";
    private static final String RADIO_6_1 = "#radioButton6_1";
    private static final String RADIO_7_1 = "#radioButton7_1";
    private static final String RADIO_8_1 = "#radioButton8_1";
    private static final String RADIO_9_1 = "#radioButton9_1";
    private static final String RADIO_10_1 = "#radioButton10_1";

    private static final String VALID_STUDENT_ID = "S24013322";
    private static final String VALID_STUDENT_NAME = "Denisse";
    private static final String VALID_STUDENT_LASTNAME = "Reyes";

    private static final String EXPECTED_NOT_ALL_ANSWERED_MESSAGE = "Por favor, responda todas las preguntas" 
        + " antes de generar.";
    private static final String EXPECTED_JR_ERROR_MESSAGE = "Error técnico al generar el documento PDF.";

    private Stage primaryStage;
    private FXMLGenerateAutoevaluationController controller;
    private AutoevaluationCommon autoevaluationCommonMock;
    private AutoevaluationDAO autoevaluationDAOMock;
    private ExpedientDAO expedientDAOMock;
    private static final Logger LOGGER = Logger.getLogger(
    FXMLGenerateAutoevaluationControllerTest.class.getName());

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        SessionManager.getInstance().setCurrentStudent(buildStudent());

        autoevaluationCommonMock = mock(AutoevaluationCommon.class);
        autoevaluationDAOMock = mock(AutoevaluationDAO.class);
        expedientDAOMock = mock(ExpedientDAO.class);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(AUTOEVALUATION_VIEW_FXML));
        Parent root = loader.load();

        controller = loader.getController();
        injectField(AUTOEVALUATION_COMMON_FIELD, autoevaluationCommonMock);
        injectField(AUTOEVALUATION_DAO_FIELD, autoevaluationDAOMock);
        injectField(EXPEDIENT_DAO_FIELD, expedientDAOMock);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUpMocks() throws OperationException {

        interact(() -> clearLabelMessage());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeSecondaryWindows() {
        interact(() -> List.copyOf(listWindows()).stream()
            .filter(window -> window != primaryStage)
            .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();

        EventQueue.invokeLater(() ->
            Arrays.stream(java.awt.Window.getWindows()).forEach(java.awt.Window::dispose));
    }

    @Test
    void generateAutoevaluation_notAllQuestionsAnswered_showsValidationError() {

        clickOn(RADIO_1_1);
        clickOn(RADIO_2_1);

        clickGenerate();

        assertEquals(EXPECTED_NOT_ALL_ANSWERED_MESSAGE, messageText());
    }

 
    @Test
    void generateAutoevaluation_validData_generatesReport() throws Exception {
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("TestAutoevaluation");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
 
        when(autoevaluationCommonMock.generateAutoevaluation(any())).thenReturn(jasperPrint);

        fillAllQuestions();
        clickGenerate();
 
        WaitForAsyncUtils.waitForFxEvents();

        verify(autoevaluationCommonMock).generateAutoevaluation(any());
    }

    @Test
    void generateAutoevaluation_operationExceptionThrown_showErrorMessage() throws Exception {
        OperationException operationException = new OperationException(EXPECTED_JR_ERROR_MESSAGE, null);
        when(autoevaluationCommonMock.generateAutoevaluation(any()))
        .thenThrow(operationException);
 
        fillAllQuestions();
        clickGenerate();
 
        assertEquals(EXPECTED_JR_ERROR_MESSAGE, messageText());
    }

    @Test
    void generateAutoevaluation_jasperExceptionThrown_showErrorMessage() throws Exception {
        when(autoevaluationCommonMock.generateAutoevaluation(any()))
        .thenThrow(new JRException("Error Jasper de prueba"));
 
        fillAllQuestions();
        clickGenerate();
        WaitForAsyncUtils.waitForFxEvents();
 
        assertEquals(EXPECTED_JR_ERROR_MESSAGE, messageText());
    }

    private Student buildStudent() {
        Student student = new Student();
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(VALID_STUDENT_NAME);
        student.setLastName(VALID_STUDENT_LASTNAME);
        return student;
    }

    private void injectField(String fieldName, Object value) {
        try {
            Field field = FXMLGenerateAutoevaluationController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (ReflectiveOperationException reflectiveOperationException) {
            LOGGER.log(Level.SEVERE,
                    "Error al inyectar campo: " + fieldName, reflectiveOperationException);
        }
    }

    private void fillAllQuestions() {
        clickOn(RADIO_1_1);
        clickOn(RADIO_2_1);
        clickOn(RADIO_3_1);
        clickOn(RADIO_4_1);
        clickOn(RADIO_5_1);
        clickOn(RADIO_6_1);
        clickOn(RADIO_7_1);
        clickOn(RADIO_8_1);

        interact(() -> lookup(".scroll-pane").queryAs(ScrollPane.class).setVvalue(1.0));

        WaitForAsyncUtils.waitForFxEvents();
        clickOn(RADIO_9_1);
        clickOn(RADIO_10_1);
    }

    private void clickGenerate() {
        clickOn(GENERATE_BUTTON_SELECTOR);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clearLabelMessage() {
        lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).setText("");
    }

    private String messageText() {
        return lookup(MESSAGE_LABEL_SELECTOR).queryAs(Label.class).getText();
    }

}