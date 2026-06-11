package uv.lis.logic.dto;

import java.util.Objects;

public class Subject {
    private final static String CAREER = "Ingenieria de Software";
    private final static String SUBJECT_NAME = "Practicas Profesionales";
    private String name;
    private int nrc;
    private String professorPersonnelNumber;
    private int schoolPeriodId;
    private String schoolPeriodName;
    private String section;

    public String getSchoolPeriodName() {
        return schoolPeriodName;
    }

    public void setSchoolPeriodName(String schoolPeriodName) {
        this.schoolPeriodName = schoolPeriodName;
    }

    public Subject() {

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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
    
    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Subject other = (Subject) object;
            isEqual = nrc == other.nrc
                && schoolPeriodId == other.schoolPeriodId
                && Objects.equals(professorPersonnelNumber, other.professorPersonnelNumber)
                && Objects.equals(schoolPeriodName, other.schoolPeriodName);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nrc, schoolPeriodId, professorPersonnelNumber, schoolPeriodName);
    }
}
