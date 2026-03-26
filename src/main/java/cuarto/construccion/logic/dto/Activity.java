package cuarto.construccion.logic.dto;


import java.sql.Date;

public class Activity {
    private int idActivity;
    private String activityName;
    private String description;
    private Date startDate;
    private Date endDate;

    public Activity() {
    }

    public Activity(int idActivity, String activityName, String description, Date startDate, Date endDate) {
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}