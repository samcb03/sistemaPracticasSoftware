package uv.lis.logic.dto;


public class Report {
    protected int id;
    protected String observation;
    protected String dueDate;
    protected String matricula;
    private int counter = 0;

    public Report() {
    }
    public Report(String observation, String dueDate, int id, String matricula) {
        this.observation = observation;
        this.dueDate = dueDate;
        this.id = generateId();
        this.matricula = matricula;
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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    private int generateId() {
        return ++counter;
    }
}
