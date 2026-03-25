package cuarto.construccion.logic.dto;


import java.util.Date;


public class Student extends User {
    private int studentId;
    private Date dateOfBirth;
    private int completedHours;
    private Gender gender;
    private boolean indigenousLanguage;
    private String autoevaluation;

    public Student(int idUser, String firstName, String lastName, int studentId, Date dateOfBirth, 
            int completedHours, Gender gender, boolean indigenousLanguage, String autoevaluation) {
        super(idUser, firstName, lastName);
        this.studentId = studentId;
        this.dateOfBirth = dateOfBirth;
        this.completedHours = completedHours;
        this.gender = gender;
        this.indigenousLanguage = indigenousLanguage;
        this.autoevaluation = autoevaluation;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean isIndigenousLanguage() {
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

    private enum Gender {
        MALE,
        FEMALE,
        OTHER
    }
}