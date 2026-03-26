package cuarto.construccion.logic.dto;

public class Activity {
    private int idActivity;
    private String activityName;
    private String description;
    private String startDate;
    private String endDate;

    public Activity() {
    }

    public Activity(int idActivity, String activityName, String description, String startDate, String endDate) {
        this.idActivity = idActivity;
        this.activityName = activityName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}