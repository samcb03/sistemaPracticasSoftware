package uv.lis.logic.dto;


import java.util.Date;


public class Student extends User {
    private String idStudent;
    private Date birthDate;
    private int completedHours;
    private String gender;
    private boolean indigenousLanguage;
    private Subject subject;
    private Report report;
    private Project project;

    public Student() {
        
    }

    public Student(int idUser, String firstName, String lastName, String password, String idStudent, Date birthDate, 
            int completedHours, String gender, boolean indigenousLanguage) {
        super(idUser, firstName, lastName, password, "Alumno", true);
        this.idStudent = idStudent;
        this.birthDate = birthDate;
        this.completedHours = completedHours;
        this.gender = gender;
        this.indigenousLanguage = indigenousLanguage;
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

    public boolean hasIndigenousLanguage() {
        return indigenousLanguage;
    }

    public void setIndigenousLanguage(boolean indigenousLanguage) {
        this.indigenousLanguage = indigenousLanguage;
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
}