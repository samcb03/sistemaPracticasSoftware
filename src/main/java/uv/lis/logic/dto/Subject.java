package uv.lis.logic.dto;


import java.util.Objects;


public class Subject {
    private final static String CAREER = "Ingenieria de Software";
    private final static String SUBJECT_NAME = "Practicas Profesionales";
    private int nrc;
    private Professor professor;
    private String schoolPeriod;

    public Subject(){

    }

    public Subject(int nrc, String schoolPeriod) {
        this.nrc = nrc;
        this.schoolPeriod = schoolPeriod;
    }

    public int getNrc() {
        return nrc;
    }

    public void setNrc(int nrc) {
        this.nrc = nrc;
    }

    public String getSUBJECT_NAME() {
        return SUBJECT_NAME;
    }

    public String getCAREER() {
        return CAREER;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public String getSchoolPeriod() {
        return schoolPeriod;
    }

    public void setSchoolPeriod(String schoolPeriod) {
        this.schoolPeriod = schoolPeriod;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Subject other = (Subject) object;
        return nrc == other.nrc
            && Objects.equals(nrc, other.nrc)
            && Objects.equals(professor, other.professor)
            && Objects.equals(schoolPeriod, other.schoolPeriod);
    }

}
