package cuarto.construccion.logic.dto;

public class Report {
    protected String observation;
    protected String dueDate;
    protected int idReport;
    protected int idStudent;

    public Report() {
    }
    public Report(String observation, String dueDate, int idReport, int idStudent) {
        this.observation = observation;
        this.dueDate = dueDate;
        this.idReport = idReport;
        this.idStudent = idStudent;
    }


    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public int getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(int idStudent) {
        this.idStudent = idStudent;
    }
}
