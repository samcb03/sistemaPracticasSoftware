package uv.lis.logic.dto;

public class PartialReport extends Report {
    private boolean isMonthly;

    public PartialReport(){
    }

public PartialReport(String observation, String dueDate, int idReport, String matricula, boolean isMonthly) {
        super(observation, dueDate, idReport, matricula);
        this.isMonthly = isMonthly;
    }

    public PartialReport(boolean isMonthly) {
        this.isMonthly = isMonthly;
    }

    public boolean getIsMonthly() {
        return isMonthly;
    }

    public void setIsMonthly(boolean isMonthly) {
        this.isMonthly = isMonthly;
    }
}
