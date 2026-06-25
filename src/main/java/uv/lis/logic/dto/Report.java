package uv.lis.logic.dto;

import java.util.Objects;

public class Report {

    private int id;
    private String studentId;
    private String studentName;
    private String professorName;
    private String nrcSubject;
    private String schoolPeriod;

    public Report() {

    }

    public Report(int id, String studentId) {
        this.id = id;
        this.studentId = studentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getNrcSubject() {
        return nrcSubject;
    }

    public void setNrcSubject(String nrcSubject) {
        this.nrcSubject = nrcSubject;
    }

    public String getSchoolPeriod() {
        return schoolPeriod;
    }

    public void setSchoolPeriod(String schoolPeriod) {
        this.schoolPeriod = schoolPeriod;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Report other = (Report) object;
            isEqual = id == other.id
                && Objects.equals(studentId, other.studentId);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId);
    }
}