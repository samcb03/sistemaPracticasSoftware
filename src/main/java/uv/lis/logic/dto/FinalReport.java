package uv.lis.logic.dto;


public class FinalReport extends Report {
    private int advancePercentage;
    private String result;

    public FinalReport() {
    }

    public FinalReport(int advancePercentage, String result) {
        this.advancePercentage = advancePercentage;
        this.result = result;
    }

    public FinalReport(String observations, String dueDate, int id, String studentId, int advancePercentage, 
        String result, String description, String activity) {
        super(id, description, observations, activity, studentId); 
        this.advancePercentage = advancePercentage;
        this.result = result;
    }

    public int getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(int advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}