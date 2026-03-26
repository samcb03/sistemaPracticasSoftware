package uv.lis.logic.dto;

public class Report {
    protected int id;
    protected String observation;
    protected String dueDate;
    protected int idStudent;

    public Report() {
    }
    public Report(String observation, String dueDate, int id, int idStudent) {
        this.observation = observation;
        this.dueDate = dueDate;
        this.id = id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(int idStudent) {
        this.idStudent = idStudent;
    }
}
