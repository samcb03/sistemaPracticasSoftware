package uv.lis.logic.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uv.lis.logic.utils.InputValidator;

public class MonthlyReport extends Report {

    private int idReport;
    private int idProject;
    private String month;
    private int year;
    private String coordinatorName;
    private int accumulatedHours;
    private int reportedHours;
    private int reportNumber;
    private String period;
    private String section;
    private String block;
    private List<String> periods;
    private List<String> activities;
    private List<String> observations;

    public MonthlyReport() {
        this.periods = new ArrayList<>();
        this.activities = new ArrayList<>();
        this.observations = new ArrayList<>();
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }

    public int getAccumulatedHours() {
        return accumulatedHours;
    }

    public void setAccumulatedHours(int accumulatedHours) {
        this.accumulatedHours = accumulatedHours;
    }

    public int getReportedHours() {
        return reportedHours;
    }

    public void setReportedHours(int reportedHours) {
        this.reportedHours = reportedHours;
    }

    public int getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(int reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }


    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public void addActivityEntry(String entryPeriod, String activity, String observation) {
        periods.add(entryPeriod);
        activities.add(activity);
        observations.add(observation);
    }

    public int getActivityCount() {
        return activities.size();
    }

    public String getPeriodAt(int index) {
        String periodValue = "";

        if (index >= InputValidator.MIN_POSITIVE_INTEGER && index <= periods.size()) {
            periodValue = periods.get(index - InputValidator.MIN_POSITIVE_INTEGER);
        }
        return periodValue;
    }

    public String getActivityAt(int index) {
        String activityValue = "";

        if (index >= InputValidator.MIN_POSITIVE_INTEGER && index <= activities.size()) {
            activityValue = activities.get(index - InputValidator.MIN_POSITIVE_INTEGER);
        }
        return activityValue;
    }

    public String getObservationAt(int index) {
        String observationValue = "";

        if (index >= InputValidator.MIN_POSITIVE_INTEGER && index <= observations.size()) {
            observationValue = observations.get(index - InputValidator.MIN_POSITIVE_INTEGER);
        }
        return observationValue;
    }

    public int getYear() {
        return year;
    }
 
    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            MonthlyReport other = (MonthlyReport) object;
            isEqual = idReport == other.idReport
                && reportedHours == other.reportedHours
                && reportNumber == other.reportNumber
                && Objects.equals(month, other.month);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReport, month, reportedHours, reportNumber);
    }
}