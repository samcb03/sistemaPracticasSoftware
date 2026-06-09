package uv.lis.logic.dto;

import java.util.Objects;

public class Advance {

    private int projectId;
    private int reportId;
    private int weekNumber;
    private int accumulatedHours;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getAccumulatedHours() {
        return accumulatedHours;
    }

    public void setAccumulatedHours(int accumulatedHours) {
        this.accumulatedHours = accumulatedHours;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Advance other = (Advance) object;
            isEqual = projectId == other.projectId
                && reportId == other.reportId
                && weekNumber == other.weekNumber;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, reportId, weekNumber);
    }
}
