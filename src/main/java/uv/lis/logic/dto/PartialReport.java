package uv.lis.logic.dto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

public class PartialReport extends Report {

    public static final int MAX_ACTIVITIES = 6;
    public static final int MAX_WEEKS = 8;

    private String observations;
    private String activityName;
    private String reportPeriod;
    private LocalDate reportPeriodStart;
    private int reportNumber;
    private String projectSupervisor;
    private String result;
    private String projectName;
    private String projectMethodology;
    private String projectObjective;
    private String affiliatedOrganization;
    private String totalHours;
    private String dateReport;
    private boolean manualAdvances;

    private String[] activityNames;
    private int[] realWeeklyAdvances;
    private int[][] plannedAdvances;
    private int[][] realAdvances;

    public PartialReport() {
        this.activityNames = new String[MAX_ACTIVITIES];
        this.realWeeklyAdvances = new int[MAX_ACTIVITIES];
        this.plannedAdvances = new int[MAX_WEEKS][MAX_ACTIVITIES];
        this.realAdvances = new int[MAX_WEEKS][MAX_ACTIVITIES];
        Arrays.fill(this.activityNames, "");
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public LocalDate getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDate reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public int getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(int reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getProjectSupervisor() {
        return projectSupervisor;
    }

    public void setProjectSupervisor(String projectSupervisor) {
        this.projectSupervisor = projectSupervisor;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectMethodology() {
        return projectMethodology;
    }

    public void setProjectMethodology(String projectMethodology) {
        this.projectMethodology = projectMethodology;
    }

    public String getProjectObjective() {
        return projectObjective;
    }

    public void setProjectObjective(String projectObjective) {
        this.projectObjective = projectObjective;
    }

    public String getAffiliatedOrganization() {
        return affiliatedOrganization;
    }

    public void setAffiliatedOrganization(String affiliatedOrganization) {
        this.affiliatedOrganization = affiliatedOrganization;
    }

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }

    public String getDateReport() {
        return dateReport;
    }

    public void setDateReport(String dateReport) {
        this.dateReport = dateReport;
    }

    public String[] getActivityNames() {
        return activityNames;
    }

    public void setActivityNames(String[] activityNames) {
        this.activityNames = activityNames;
    }

    public int[] getRealWeeklyAdvances() {
        return realWeeklyAdvances;
    }

    public void setRealWeeklyAdvances(int[] realWeeklyAdvances) {
        this.realWeeklyAdvances = realWeeklyAdvances;
    }

    public int[][] getPlannedAdvances() {
        return plannedAdvances;
    }

    public void setPlannedAdvances(int[][] plannedAdvances) {
        this.plannedAdvances = plannedAdvances;
    }

    public int[][] getRealAdvances() {
        return realAdvances;
    }

    public void setRealAdvances(int[][] realAdvances) {
        this.realAdvances = realAdvances;
    }

    public boolean isManualAdvances() {
        return manualAdvances;
    }

    public void setManualAdvances(boolean manualAdvances) {
        this.manualAdvances = manualAdvances;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            PartialReport other = (PartialReport) object;
            isEqual = reportNumber == other.reportNumber
                && Objects.equals(observations, other.observations)
                && Objects.equals(activityName, other.activityName)
                && Objects.equals(reportPeriod, other.reportPeriod)
                && Objects.equals(result, other.result)
                && manualAdvances == other.manualAdvances;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(observations, activityName, reportPeriod, reportNumber, result, manualAdvances);
    }
}