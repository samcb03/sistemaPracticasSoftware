package uv.lis.logic.dto;


public class Subject {
    private final static String CAREER = "Ingenieria de Software";
    private int nrc;
    private String subjectName;
    private int idSchoolPeriod;
    private Professor professor;
    private SchoolPeriod schoolPeriod;

    public Subject(){

    }

    public Subject(int nrc, String subjectName, int idSchoolPeriod) {
        this.nrc = nrc;
        this.subjectName = subjectName;
        this.idSchoolPeriod = idSchoolPeriod;
    }

    public int getNrc() {
        return nrc;
    }

    public void setNrc(int nrc) {
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
    public Professor getProfessor() {
        return professor;
    }
    public void setProfessor(Professor professor) {
        this.professor = professor;
    }
    public SchoolPeriod getSchoolPeriod() {
        return schoolPeriod;
    }
    public void setSchoolPeriod(SchoolPeriod schoolPeriod) {
        this.schoolPeriod = schoolPeriod;
    }

}
