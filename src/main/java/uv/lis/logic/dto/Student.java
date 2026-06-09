package uv.lis.logic.dto;


import java.sql.Date;
import java.util.Objects;

public class Student extends User {
    private String idStudent;
    private Date birthDate;
    private int completedHours;
    private String gender;
    private Subject subject;
    private Report report;
    private Project project;

    public Student() {
        
    }

    public Student(int idUser, String firstName, String lastName, String password, String email, int roleId, 
            boolean isActive, String idStudent, Date birthDate, int completedHours, String gender) {
        super(idUser, firstName, lastName, password, email, roleId, isActive);
        this.idStudent = idStudent;
        this.birthDate = birthDate;
        this.completedHours = completedHours;
        this.gender = gender;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public int getCompletedHours() {
        return completedHours;
    }

    public void setCompletedHours(int completedHours) {
        this.completedHours = completedHours;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } 
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Student other = (Student) object;
        return getId() == other.getId()
            && Objects.equals(idStudent, other.idStudent)
            && Objects.equals(getFirstName(), other.getFirstName())
            && Objects.equals(getLastName(), other.getLastName())
            && Objects.equals(gender, other.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), idStudent, getFirstName(), getLastName());
    }
}