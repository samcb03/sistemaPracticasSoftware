package uv.lis.logic.dto;

public class FinalReport extends Report {
    private String porcentajeAvance;
    private String resultadoEntregable;

    public FinalReport() {
    }

    public FinalReport(String porcentajeAvance, String resultadoEntregable) {
        this.porcentajeAvance = porcentajeAvance;
        this.resultadoEntregable = resultadoEntregable;
    }

    public FinalReport(String observation, String dueDate, int idReport, int studentId, String porcentajeAvance, String resultadoEntregable) {
        super(observation, dueDate, idReport, studentId); 
        this.porcentajeAvance = porcentajeAvance;
        this.resultadoEntregable = resultadoEntregable;
    }

    public String getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(String porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getResultadoEntregable() {
        return resultadoEntregable;
    }

    public void setResultadoEntregable(String resultadoEntregable) {
        this.resultadoEntregable = resultadoEntregable;
    }
}