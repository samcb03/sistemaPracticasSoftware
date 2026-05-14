package uv.lis.logic.dto;


import java.util.Objects;


public class Report {
    private int id;
    private String description;
    private String observations;
    private String activity;
    private String studentId;
    private float calification;
    private String nrcSubject;
    private String professorName;
    private String schoolPeriod;
    private String studentName;
    private String affiliatedOrganization;
    private String projectName;
    private String totalHours;
    private String dateReport;
    private String projectMethodology;
    private String projectObjective;
    private String activityName1;
    private String advancePercentageActivity1;
    private String observationsActivity1;
    private String activityName2;
    private String observationsActivity2;
    private String advancePercentageActivity2;
    private String generalObservations;
    private String result1;
    private String resultAdvance1;
    private String resultObservations1;
    private String result2;
    private String resultAdvance2;
    private String resultObservations2;
    
    public Report() {
        
    }

    public Report(int id, String description, String observations, String activity, String studentId) {
        this.id = id;
        this.description = description;
        this.observations = observations;
        this.activity = activity;
        this.studentId = studentId;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
    }

    public String getNrcSubject() {
        return nrcSubject;
    }

    public void setNrcSubject(String nrcSubject) {
        this.nrcSubject = nrcSubject;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getSchoolPeriod() {
        return schoolPeriod;
    }

    public void setSchoolPeriod(String schoolPeriod) {
        this.schoolPeriod = schoolPeriod;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAffiliatedOrganization() {
        return affiliatedOrganization;
    }

    public void setAffiliatedOrganization(String affiliatedOrganization) {
        this.affiliatedOrganization = affiliatedOrganization;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }

    public String getDateReport() {
        return dateReport;
    }

    public void setDateReport(String dateReport) {
        this.dateReport = dateReport;
    }

    public String getProjectMethodology() {
        return projectMethodology;
    }

    public void setProjectMethodology(String projectMethodology) {
        this.projectMethodology = projectMethodology;
    }

    public String getProjectObjective() {
        return projectObjective;
    }

    public void setProjectObjective(String projectObjective) {
        this.projectObjective = projectObjective;
    }

    public String getActivityName1() {
        return activityName1;
    }

    public void setActivityName1(String activityName1) {
        this.activityName1 = activityName1;
    }

    public String getAdvancePercentageActivity1() {
        return advancePercentageActivity1;
    }

    public void setAdvancePercentageActivity1(String advancePercentageActivity1) {
        this.advancePercentageActivity1 = advancePercentageActivity1;
    }

    public String getObservationsActivity1() {
        return observationsActivity1;
    }

    public void setObservationsActivity1(String observationsActivity1) {
        this.observationsActivity1 = observationsActivity1;
    }

    public String getActivityName2() {
        return activityName2;
    }

    public void setActivityName2(String activityName2) {
        this.activityName2 = activityName2;
    }

    public String getObservationsActivity2() {
        return observationsActivity2;
    }

    public void setObservationsActivity2(String observationsActivity2) {
        this.observationsActivity2 = observationsActivity2;
    }

    public String getAdvancePercentageActivity2() {
        return advancePercentageActivity2;
    }

    public void setAdvancePercentageActivity2(String advancePercentageActivity2) {
        this.advancePercentageActivity2 = advancePercentageActivity2;
    }

    public String getGeneralObservations() {
        return generalObservations;
    }

    public void setGeneralObservations(String generalObservations) {
        this.generalObservations = generalObservations;
    }

    public String getResult1() {
        return result1;
    }

    public void setResult1(String result1) {
        this.result1 = result1;
    }

    public String getResultAdvance1() {
        return resultAdvance1;
    }

    public void setResultAdvance1(String resultAdvance1) {
        this.resultAdvance1 = resultAdvance1;
    }

    public String getResultObservations1() {
        return resultObservations1;
    }

    public void setResultObservations1(String resultObservations1) {
        this.resultObservations1 = resultObservations1;
    }

    public String getResult2() {
        return result2;
    }

    public void setResult2(String result2) {
        this.result2 = result2;
    }

    public String getResultAdvance2() {
        return resultAdvance2;
    }

    public void setResultAdvance2(String resultAdvance2) {
        this.resultAdvance2 = resultAdvance2;
    }

    public String getResultObservations2() {
        return resultObservations2;
    }

    public void setResultObservations2(String resultObservations2) {
        this.resultObservations2 = resultObservations2;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Report other = (Report) object;
        return id == other.id
            && Objects.equals(description, other.description)
            && Objects.equals(observations, other.observations)
            && Objects.equals(activity, other.activity)
            && Objects.equals(studentId, other.studentId)
            && calification == calification;
    }
}