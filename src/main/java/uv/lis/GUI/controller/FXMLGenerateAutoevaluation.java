package uv.lis.GUI.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import uv.lis.GUI.ValidationHandler;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.common.AutoevaluationCommon;
import uv.lis.logic.utils.SessionManager;

public class FXMLGenerateAutoevaluation extends ValidationHandler {

    @FXML private Label labelStudentName;
    @FXML private Label labelStudentId;
    @FXML private Label labelOrganization;
    @FXML private Label labelProject;
    @FXML private Label labelProjectSupervisor;
    @FXML private Label labelMessage;
    @FXML private Button buttonGenerate;
    @FXML private Button buttonBack;
    @FXML private RadioButton radioButton1_1;
    @FXML private RadioButton radioButton1_2;
    @FXML private RadioButton radioButton1_3;
    @FXML private RadioButton radioButton1_4;
    @FXML private RadioButton radioButton1_5;
    @FXML private RadioButton radioButton2_1;
    @FXML private RadioButton radioButton2_2;
    @FXML private RadioButton radioButton2_3;
    @FXML private RadioButton radioButton2_4;
    @FXML private RadioButton radioButton2_5;
    @FXML private RadioButton radioButton3_1;
    @FXML private RadioButton radioButton3_2;
    @FXML private RadioButton radioButton3_3;
    @FXML private RadioButton radioButton3_4;
    @FXML private RadioButton radioButton3_5;
    @FXML private RadioButton radioButton4_1;
    @FXML private RadioButton radioButton4_2;
    @FXML private RadioButton radioButton4_3;
    @FXML private RadioButton radioButton4_4;
    @FXML private RadioButton radioButton4_5;
    @FXML private RadioButton radioButton5_1;
    @FXML private RadioButton radioButton5_2;
    @FXML private RadioButton radioButton5_3;
    @FXML private RadioButton radioButton5_4;
    @FXML private RadioButton radioButton5_5;
    @FXML private RadioButton radioButton6_1;
    @FXML private RadioButton radioButton6_2;
    @FXML private RadioButton radioButton6_3;
    @FXML private RadioButton radioButton6_4;
    @FXML private RadioButton radioButton6_5;
    @FXML private RadioButton radioButton7_1;
    @FXML private RadioButton radioButton7_2;
    @FXML private RadioButton radioButton7_3;
    @FXML private RadioButton radioButton7_4;
    @FXML private RadioButton radioButton7_5;
    @FXML private RadioButton radioButton8_1;
    @FXML private RadioButton radioButton8_2;
    @FXML private RadioButton radioButton8_3;
    @FXML private RadioButton radioButton8_4;
    @FXML private RadioButton radioButton8_5;
    @FXML private RadioButton radioButton9_1;
    @FXML private RadioButton radioButton9_2;
    @FXML private RadioButton radioButton9_3;
    @FXML private RadioButton radioButton9_4;
    @FXML private RadioButton radioButton9_5;
    @FXML private RadioButton radioButton10_1;
    @FXML private RadioButton radioButton10_2;
    @FXML private RadioButton radioButton10_3;
    @FXML private RadioButton radioButton10_4;
    @FXML private RadioButton radioButton10_5;

    private ToggleGroup[] groups;
    private AutoevaluationCommon autoevaluationCommon;
    private AutoevaluationDAO autoevaluationDAO;
    private Student currentStudent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        autoevaluationCommon = new AutoevaluationCommon();
        autoevaluationDAO = new AutoevaluationDAO();
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        setupControls(labelMessage, buttonBack);
        setupToggleGroups();
        loadAutomaticData(); 
    }

    private void setupToggleGroups() {
        groups = new ToggleGroup[10]; 
        groups[0] = createGroup(radioButton1_1,  radioButton1_2,  radioButton1_3,  radioButton1_4,  radioButton1_5);
        groups[1] = createGroup(radioButton2_1,  radioButton2_2,  radioButton2_3,  radioButton2_4,  radioButton2_5);
        groups[2] = createGroup(radioButton3_1,  radioButton3_2,  radioButton3_3,  radioButton3_4,  radioButton3_5);
        groups[3] = createGroup(radioButton4_1,  radioButton4_2,  radioButton4_3,  radioButton4_4,  radioButton4_5);
        groups[4] = createGroup(radioButton5_1,  radioButton5_2,  radioButton5_3,  radioButton5_4,  radioButton5_5);
        groups[5] = createGroup(radioButton6_1,  radioButton6_2,  radioButton6_3,  radioButton6_4,  radioButton6_5);
        groups[6] = createGroup(radioButton7_1,  radioButton7_2,  radioButton7_3,  radioButton7_4,  radioButton7_5);
        groups[7] = createGroup(radioButton8_1,  radioButton8_2,  radioButton8_3,  radioButton8_4,  radioButton8_5);
        groups[8] = createGroup(radioButton9_1,  radioButton9_2,  radioButton9_3,  radioButton9_4,  radioButton9_5);
        groups[9] = createGroup(radioButton10_1, radioButton10_2, radioButton10_3, radioButton10_4, radioButton10_5);  
    }

    private ToggleGroup createGroup(RadioButton... parameters) {
        ToggleGroup group = new ToggleGroup(); 
        int value = 1;                        
        for (RadioButton radioButton : parameters) {
            radioButton.setToggleGroup(group);
            radioButton.setUserData(value++); 
        }
        return group;
    }

    private void loadAutomaticData() { 
        if (currentStudent != null) {
            labelStudentName.setText(currentStudent.getFirstName() + " " + currentStudent.getLastName());
            labelStudentId.setText(currentStudent.getIdStudent());

            try {
                Autoevaluation contextData = autoevaluationDAO.getAutoevaluationData(currentStudent.getIdStudent());

                labelOrganization.setText(contextData.getOrganizationName());
                labelProject.setText(contextData.getProjectName());
                labelProjectSupervisor.setText(contextData.getProjectSupervisorName());

            } catch (OperationException e) {
                showError("No se encontró un proyecto asignado: " + e.getMessage());
                buttonGenerate.setDisable(true);
            }
        }
    }

    @FXML
    private void generateAutoevaluation() {
        Optional<int[]> answers = collectAnswers();

        if (answers.isEmpty()) {
            showError("Por favor, responda todas las preguntas antes de generar.");
        } else {
            processAutoevaluation(answers.get());
        }
    }

    private Optional<int[]> collectAnswers() {
        Optional<int[]> result = Optional.empty();
        int[] answers = new int[10];
        boolean allAnswered = true;

        for (int questionIndex = 0; questionIndex < groups.length; questionIndex++) {
            if (groups[questionIndex].getSelectedToggle() == null) {
                allAnswered = false;
            } else {
                answers[questionIndex] = (int) groups[questionIndex].getSelectedToggle().getUserData();
            }
        }
            if (allAnswered) {
                result = Optional.of(answers);
            }

        return result;
    }

    private void processAutoevaluation(int[] answers) {
        try {
            Autoevaluation evaluation = new Autoevaluation(currentStudent.getIdStudent(), answers);
            JasperPrint report = autoevaluationCommon.generateAutoevaluation(evaluation);
            JasperViewer.viewReport(report, false);
            labelMessage.setStyle("-fx-text-fill: green;");
            labelMessage.setText("Autoevaluación guardada y generada con éxito.");
        } catch (IllegalArgumentException e) {
            showError("Respuestas inválidas: " + e.getMessage());
        } catch (OperationException e) {
            showError(e.getMessage());
        } catch (JRException e) {
            showError("Error técnico al generar el documento PDF.");
        }
    }

    @Override
    protected void clearFields() {
        for (ToggleGroup g : groups) {
            if (g.getSelectedToggle() != null) {
                g.getSelectedToggle().setSelected(false);
            }
        }
        labelMessage.setText("");
    }
}