package uv.lis.logic.dto;

import java.sql.Date;
import java.util.Objects;

public class SchoolPeriod {
    private int id;
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEquals = false;
        if (this == object) {
            isEquals = true;
        }
        if (object == null || getClass() != object.getClass()) {
            isEquals = false;
        } else {
            SchoolPeriod other = (SchoolPeriod) object;
            isEquals = id == other.id
                && Objects.equals(startDate, other.startDate)
                && Objects.equals(endDate, other.endDate);
        }
        return isEquals;
    }

}