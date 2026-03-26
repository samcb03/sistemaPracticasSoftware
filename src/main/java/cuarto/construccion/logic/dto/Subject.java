package cuarto.construccion.logic.dto;


public class Subject {
    private String nrc;
    private String subjectName;
    private String career;
    private int idSchoolPeriod;

    public Subject(){

    }

    public Subject(String nrc, String subjectName, String career, int idSchoolPeriod) {
        this.nrc = nrc;
        this.subjectName = subjectName;
        this.career = career;
        this.idSchoolPeriod = idSchoolPeriod;

    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public int getIdSchoolPeriod() {
        return idSchoolPeriod;
    }

    public void setIdSchoolPeriod(int idSchoolPeriod) {
        this.idSchoolPeriod = idSchoolPeriod;
    }
}
