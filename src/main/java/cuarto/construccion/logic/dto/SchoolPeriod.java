package cuarto.construccion.logic.dto;


import java.sql.Date;

public class SchoolPeriod {
    private int idSchoolPeriod;
    private Date startDate;
    private Date endDate;

    public SchoolPeriod() {
    }

    public SchoolPeriod(int idSchoolPeriod, Date startDate, Date endDate) {
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}