package uv.lis.logic.dto;

public class PartialReport extends Report {

    private int plannedTime;
    private int realTime;

    public PartialReport() {

    }

    public PartialReport(int plannedTime, int realTime, String description, String observations, String studentId, 
        String dueDate, int id, String activity) {
        super(id, description, observations, activity, studentId);
        this.plannedTime = plannedTime;
        this.realTime = realTime;
    }

    public int getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(int plannedTime) {
        this.plannedTime = plannedTime;
    }

    public int getRealTime() {
        return realTime;
    }

    public void setRealTime(int realTime) {
        this.realTime = realTime;
    }
}