package cuarto.construccion.logic.dto;

public class SchoolPeriod {
    private int idSchoolPeriod;
    private String startDate;
    private String endDate;

    public SchoolPeriod() {
    }

    public SchoolPeriod(int idSchoolPeriod, String startDate, String endDate) {
        this.idSchoolPeriod = idSchoolPeriod;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getIdSchoolPeriod() {
        return idSchoolPeriod;
    }

    public void setIdSchoolPeriod(int idSchoolPeriod) {
        this.idSchoolPeriod = idSchoolPeriod;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}