package uv.lis.logic.dto;


import java.util.Objects;


public class Report {
    private int id;
    private String description;
    private String observations;
    private String activity;
    private String studentId;
    private float calification;

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

        @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Report other = (Report) object;
        return id == other.id
            && Objects.equals(description, other.description)
            && Objects.equals(observations, other.observations)
            && Objects.equals(activity, other.activity)
            && Objects.equals(studentId, other.studentId)
            && calification == calification;
    }
}