package uv.lis.logic.dto;

public class PartialReport extends Report {
    private boolean isMensual;

    public PartialReport(){
    }

public PartialReport(String observation, String dueDate, int idReport, int idStudent, boolean isMensual) {
        super(observation, dueDate, idReport, idStudent);
        this.isMensual = isMensual;
    }

    public PartialReport(boolean isMensual) {
        this.isMensual = isMensual;
    }

    public boolean getIsMensual() {
        return isMensual;
    }

    public void setIsMensual(boolean isMensual) {
        this.isMensual = isMensual;
    }
}
