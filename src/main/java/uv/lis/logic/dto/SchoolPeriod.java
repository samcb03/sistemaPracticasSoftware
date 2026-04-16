package uv.lis.logic.dto;


import java.sql.Date;

public class SchoolPeriod {
    private int id;
    private Date startDate;
    private Date endDate;        

    public SchoolPeriod() {    
    }

    public SchoolPeriod(int id, Date startDate, Date endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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