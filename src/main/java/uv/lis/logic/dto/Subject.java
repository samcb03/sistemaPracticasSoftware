package uv.lis.logic.dto;

import java.util.Objects;

public class Subject {
    private final static String CAREER = "Ingenieria de Software";
    private final static String SUBJECT_NAME = "Practicas Profesionales";
    private String name;
    private int nrc;
    private String professorPersonnelNumber;
    private int schoolPeriodId;

    public Subject(){

    }

    public static String getCareer() {
        return CAREER;
    }

    public static String getSubjectName() {
        return SUBJECT_NAME;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject(int nrc, int schoolPeriodId) {
        this.nrc = nrc;
        this.schoolPeriodId = schoolPeriodId;
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
    
    public int getSchoolPeriodId() {
        return schoolPeriodId;
    }

    public void setSchoolPeriodId(int schoolPeriodId) {
        this.schoolPeriodId = schoolPeriodId;
    }
    
    public String getProfessorPersonnelNumber() {
        return professorPersonnelNumber;
    }

    public void setProfessorPersonnelNumber(String professorPersonnelNumber) {
        this.professorPersonnelNumber = professorPersonnelNumber;
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
            && Objects.equals(professorPersonnelNumber, other.professorPersonnelNumber)
            && schoolPeriodId == other.schoolPeriodId;
    }
}
