package uv.lis.logic.dto;


import java.util.Date;


public class Student extends User {
    private String idStudent;
    private Date dateOfBirth;
    private int completedHours;
    private String gender;
    private boolean indigenousLanguage;
    private String autoevaluation;
    public Student(){

    }

    public Student(int idUser, String firstName, String lastName, String password, String idStudent, Date dateOfBirth, 
            int completedHours, String gender, boolean indigenousLanguage, String autoevaluation) {
        super(idUser, firstName, lastName, password, "Alumno");
        this.idStudent = idStudent;
        this.dateOfBirth = dateOfBirth;
        this.completedHours = completedHours;
        this.gender = gender;
        this.indigenousLanguage = indigenousLanguage;
        this.autoevaluation = autoevaluation;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getAutoevaluation() {
        return autoevaluation;
    }

    public void setAutoevaluation(String autoevaluation) {
        this.autoevaluation = autoevaluation;
    }
}