package uv.lis.GUI.controller;

import java.net.URL;
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
    @FXML private RadioButton rb1_1, rb1_2, rb1_3, rb1_4, rb1_5;
    @FXML private RadioButton rb2_1, rb2_2, rb2_3, rb2_4, rb2_5;
    @FXML private RadioButton rb3_1, rb3_2, rb3_3, rb3_4, rb3_5;
    @FXML private RadioButton rb4_1, rb4_2, rb4_3, rb4_4, rb4_5;
    @FXML private RadioButton rb5_1, rb5_2, rb5_3, rb5_4, rb5_5;
    @FXML private RadioButton rb6_1, rb6_2, rb6_3, rb6_4, rb6_5;
    @FXML private RadioButton rb7_1, rb7_2, rb7_3, rb7_4, rb7_5;
    @FXML private RadioButton rb8_1, rb8_2, rb8_3, rb8_4, rb8_5;
    @FXML private RadioButton rb9_1, rb9_2, rb9_3, rb9_4, rb9_5;
    @FXML private RadioButton rb10_1, rb10_2, rb10_3, rb10_4, rb10_5;

    private ToggleGroup[] groups;
    private AutoevaluationCommon autoevaluationCommon;
    private AutoevaluationDAO autoevaluationDAO;
    private Student currentStudent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        autoevaluationCommon = new AutoevaluationCommon();
        autoevaluationDAO    = new AutoevaluationDAO();
        currentStudent       = SessionManager.getInstance().getCurrentStudent();
        setupControls(labelMessage, buttonBack);
        setupToggleGroups();
        loadAutomaticData(); 
    }

    private void setupToggleGroups() {
        groups = new ToggleGroup[10]; 
        groups[0] = createGroup(rb1_1,  rb1_2,  rb1_3,  rb1_4,  rb1_5);
        groups[1] = createGroup(rb2_1,  rb2_2,  rb2_3,  rb2_4,  rb2_5);
        groups[2] = createGroup(rb3_1,  rb3_2,  rb3_3,  rb3_4,  rb3_5);
        groups[3] = createGroup(rb4_1,  rb4_2,  rb4_3,  rb4_4,  rb4_5);
        groups[4] = createGroup(rb5_1,  rb5_2,  rb5_3,  rb5_4,  rb5_5);
        groups[5] = createGroup(rb6_1,  rb6_2,  rb6_3,  rb6_4,  rb6_5);
        groups[6] = createGroup(rb7_1,  rb7_2,  rb7_3,  rb7_4,  rb7_5);
        groups[7] = createGroup(rb8_1,  rb8_2,  rb8_3,  rb8_4,  rb8_5);
        groups[8] = createGroup(rb9_1,  rb9_2,  rb9_3,  rb9_4,  rb9_5);
        groups[9] = createGroup(rb10_1, rb10_2, rb10_3, rb10_4, rb10_5);  
    }

    private ToggleGroup createGroup(RadioButton... parameters) {
        ToggleGroup group = new ToggleGroup(); 
        int value = 1;                        
        for (RadioButton rb : parameters) {
            rb.setToggleGroup(group);
            rb.setUserData(value++); 
        }
        return group;
    }

private void loadAutomaticData() { 
        if (currentStudent != null) {
            labelStudentName.setText(currentStudent.getFirstName() + " " + currentStudent.getLastName());
            labelStudentId.setText(currentStudent.getIdStudent());

            try {
                Autoevaluation contextData = autoevaluationDAO.getAutoevaluationData(currentStudent.getIdStudent());
                
                // --- PUNTO DE VERIFICACIÓN ---
                if (contextData == null) {
                    showError("No se encontraron datos de autoevaluación para este estudiante.");
                    return;
                }
                
                // Imprime en consola para verificar los valores reales
                System.out.println("Org: " + contextData.getOrganizationName());
                System.out.println("Proj: " + contextData.getProjectName());
                System.out.println("Sup: " + contextData.getProjectSupervisorName());
                // -----------------------------

                // Usamos validación simple para evitar strings "null" o vacíos
                labelOrganization.setText(contextData.getOrganizationName() != null ? contextData.getOrganizationName() : "Sin asignar");
                labelProject.setText(contextData.getProjectName() != null ? contextData.getProjectName() : "Sin proyecto");
                labelProjectSupervisor.setText(contextData.getProjectSupervisorName() != null ? contextData.getProjectSupervisorName() : "Sin supervisor");

            } catch (OperationException e) {
                showError("Error al cargar datos: " + e.getMessage());
                buttonGenerate.setDisable(true);
            }
        }
    }

    @FXML
    private void generateAutoevaluation() {
        int[] answers = new int[10];

        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getSelectedToggle() == null) {
                showError("Por favor, responda todas las preguntas antes de generar.");
                return;
            }
            answers[i] = (int) groups[i].getSelectedToggle().getUserData();
        }

        try {
            Autoevaluation evaluation = new Autoevaluation(
                currentStudent.getIdStudent(), answers);

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
            if(g.getSelectedToggle() != null) {
                g.getSelectedToggle().setSelected(false);
            }
        }
        labelMessage.setText("");
    }
}