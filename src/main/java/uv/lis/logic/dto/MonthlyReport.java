package uv.lis.logic.dto;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReport extends Report {

    private int idReport;
    private String month;
    private String studentName;
    private String professorName;
    private String coordinadorName;
    private int loggedHours;
    private int accumulateHour;
    private int reportNumber;
    private int reportedHours;
    private String period;
    private String section;
    private String block;
    private List<String> periods = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
    private List<String> observations = new ArrayList<>();
    private Activity activity;

    public MonthlyReport() {

    }

    public MonthlyReport(String month, int reportedHours, String description, String observations, 
            String studentId, String activity, int id, String dueDate) {
        super(id, description, observations, activity, studentId);
        this.month = month;
        this.reportedHours = reportedHours;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getReportedHours() {
        return reportedHours;
    }

    public void setReportedHours(int reportedHours) {
        this.reportedHours = reportedHours;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getCoordinadorName() {
        return coordinadorName;
    }

    public void setCoordinadorName(String coordinadorName) {
        this.coordinadorName = coordinadorName;
    }

    public int getLoggedHours() {
        return loggedHours;
    }

    public void setLoggedHours(int loggedHours) {
        this.loggedHours = loggedHours;
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

    public void addActivity(String period, String activity, String observation) {
        periods.add(period);
        activities.add(activity);
        observations.add(observation);
    }

    public int getActivityCount() {
        return activities.size();
    }

    public int getAccumulateHour() {
        return accumulateHour;
    }

    public void setAccumulateHour(int accumulateHour) {
        this.accumulateHour = accumulateHour;
    }


    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public String getPeriod(int i) { 
        return (i >= 1 && i <= periods.size()) ? periods.get(i - 1) : ""; 
    }
    public String getActivity(int i) { 
        return (i >= 1 && i <= activities.size()) ? activities.get(i - 1) : ""; 
    }
    public String getObservation(int i) { 
        return (i >= 1 && i <= observations.size()) ? observations.get(i - 1) : ""; 
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        if (!super.equals(object)) 
            return false;

        MonthlyReport other = (MonthlyReport) object;
        return month == other.month 
        && reportedHours == other.reportedHours;
    }
    
}
