package uv.lis.logic.dto;

import java.util.Objects;

public class Report {

    private int id;
    private String description;
    private String observations;
    private String activity;
    private String studentId;
    private float calification;
    private String studentName;
    private String professorName;
    private String nrcSubject;
    private String schoolPeriod;

    public Report() {

    }

    public Report(int id, String description, String observations, String activity, String studentId) {
        this.id = id;
        this.description = description;
        this.observations = observations;
        this.activity = activity;
        this.studentId = studentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
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
                && Float.compare(calification, other.calification) == 0
                && Objects.equals(description, other.description)
                && Objects.equals(observations, other.observations)
                && Objects.equals(activity, other.activity)
                && Objects.equals(studentId, other.studentId);
        }
        return isEqual;
    }
}