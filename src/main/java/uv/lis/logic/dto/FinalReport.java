package uv.lis.logic.dto;

public class FinalReport extends Report {
    private int porcentajeAvance;
    private String resultadoEntregable;

    public FinalReport() {
    }

    public FinalReport(int porcentajeAvance, String resultadoEntregable) {
        this.porcentajeAvance = porcentajeAvance;
        this.resultadoEntregable = resultadoEntregable;
    }

    public FinalReport(String observation, String dueDate, int idReport, int studentId, int porcentajeAvance, String resultadoEntregable) {
        super(observation, dueDate, idReport, studentId); 
        this.porcentajeAvance = porcentajeAvance;
        this.resultadoEntregable = resultadoEntregable;
    }

    public int getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(int porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getResultadoEntregable() {
        return resultadoEntregable;
    }

    public void setResultadoEntregable(String resultadoEntregable) {
        this.resultadoEntregable = resultadoEntregable;
    }
}