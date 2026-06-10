package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import uv.lis.GUI.WindowHandler;
import uv.lis.logic.dto.Student;
import uv.lis.logic.utils.SessionManager;

public class FXMLReportsMenuController extends WindowHandler {

    private static final String MONTHLY_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGenerateMonthlyReport.fxml";
    private static final String PARTIAL_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGeneratePartialReport.fxml";
    private static final String FINAL_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGenerateFinalReport.fxml";
    private static final int MIN_HOURS_FOR_FINAL_REPORT = 420;
    private static final int MIN_HOURS_FOR_PARTIAL_REPORT = 210;

    @FXML private Button buttonGenerateMonthlyReport;
    @FXML private Button buttonGeneratePartialReport;
    @FXML private Button buttonGenerateFinalReport;

    private Student student;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.student = SessionManager.getInstance().getCurrentStudent();
        //disableReportOptions();
    }
    
    @FXML
    public void goToGenerateMonthlyReport() {
        navigateTo(MONTHLY_REPORT_VIEW);
    }

    @FXML
    public void goToGeneratePartialReport() {
        navigateTo(PARTIAL_REPORT_VIEW);
    }

    @FXML
    public void goToGenerateFinalReport() {
        navigateTo(FINAL_REPORT_VIEW);
    }

    private void disableReportOptions() {
        if(student.getCompletedHours() < MIN_HOURS_FOR_PARTIAL_REPORT) {
            buttonGeneratePartialReport.setDisable(true);
            buttonGenerateFinalReport.setDisable(true);
        } 
        if(student.getCompletedHours() < MIN_HOURS_FOR_FINAL_REPORT) {
            buttonGenerateFinalReport.setDisable(true);
        }
    }
}