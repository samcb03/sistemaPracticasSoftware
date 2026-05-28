package uv.lis.GUI.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;

import uv.lis.GUI.WindowHandler;

public class FXMLReportsMenuController extends WindowHandler {

    private static final String MONTHLY_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGenerateMonthlyReport.fxml";
    private static final String PARTIAL_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGeneratePartialReport.fxml";
    private static final String FINAL_REPORT_VIEW = "/uv/lis/GUI/view/FXMLGenerateFinalReport.fxml";

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}