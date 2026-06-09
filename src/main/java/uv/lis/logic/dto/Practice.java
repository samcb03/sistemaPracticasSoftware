package uv.lis.logic.dto;

import java.util.Objects;

public class Practice {
    private int idPractice;
    private String finalDate;
    private String startDate;
    private String practiceName;
    private String period;
    private String calification;
    private Student student;
    private Subject subject;
    private int projectId;

    public int getIdPractice() {
        return idPractice;
    }
    public void setIdPractice(int idPractice) {
        this.idPractice = idPractice;
    }

    public String getFinalDate() {
        return finalDate;
    }
    public void setFinalDate(String finalDate) {
        this.finalDate = finalDate;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String starDate) {
        this.startDate = starDate;
    }
    public String getPracticeName() {
        return practiceName;
    }
    public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }
    public String getPeriod() {
        return period;
    }
    public void setPeriod(String period) {
        this.period = period;
    }
    public String getCalification() {
        return calification;
    }
    public void setCalification(String calification) {
        this.calification = calification;
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public Subject getSubject() {
        return subject;
    }
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getProjectId() {
        return projectId;
    }
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Practice other = (Practice) object;
        return idPractice == other.idPractice
            && Objects.equals(startDate, other.startDate) 
            && Objects.equals(finalDate, other.finalDate)
            && Objects.equals(practiceName, other.practiceName)
            && Objects.equals(period, other.period)
            && Objects.equals(calification, other.calification)
            && Objects.equals(student, other.student)
            && Objects.equals(subject, other.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPractice, startDate, finalDate, practiceName, 
                            period, calification, student, subject);
    }
}
