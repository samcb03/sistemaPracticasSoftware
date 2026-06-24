package uv.lis.logic.dto;

import java.time.LocalDate;
import java.util.Objects;

public class Activity {
    private int id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int hoursReported;
    private int reportId;
    private int projectId;
    private String studentId;

    public Activity() {
    }

    public Activity(int id, String name, String description, LocalDate startDate, LocalDate endDate, int reportId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportId = reportId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getHoursReported() {
        return hoursReported;
    }

    public void setHoursReported(int hoursReported) {
        this.hoursReported = hoursReported;
    }

    
    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Activity other = (Activity) object;
            isEqual = id == other.id
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(startDate, other.startDate)
                && Objects.equals(endDate, other.endDate);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, startDate, endDate, projectId);
    }
}