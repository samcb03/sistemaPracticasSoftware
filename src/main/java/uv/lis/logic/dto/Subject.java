package uv.lis.logic.dto;


public class Subject {
    private final static String CAREER = "Ingenieria de Software";
    private String nrc;
    private String subjectName;
    private int idSchoolPeriod;

    public Subject(){

    }

    public Subject(String nrc, String subjectName, int idSchoolPeriod) {
        this.nrc = nrc;
        this.subjectName = subjectName;
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

    public String getCAREER() {
        return CAREER;
    }

    public int getIdSchoolPeriod() {
        return idSchoolPeriod;
    }

    public void setIdSchoolPeriod(int idSchoolPeriod) {
        this.idSchoolPeriod = idSchoolPeriod;
    }
}
