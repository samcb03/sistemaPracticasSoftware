package uv.lis.logic.dto;

public class MonthlyReport extends Report {

    private String month;
    private int reportedHours;

    public MonthlyReport() {}

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
}
