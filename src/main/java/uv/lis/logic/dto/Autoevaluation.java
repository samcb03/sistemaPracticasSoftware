package uv.lis.logic.dto;


import java.util.Objects;


public class Autoevaluation {
    private static final int MAX_POSSIBLE_SCORE = 50;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;
    private String idStudent;
    private int productiveParticipation;
    private int appliedKnowledge;
    private int confidenceInActivities;
    private int activitiesInterest;
    private int organizationSupport;
    private int rulesAwareness;
    private int supervisorGuidance;
    private int effectiveMonitoring;
    private int careerAlignment;
    private int internshipImportance;
    private double finalScore;
    private String evaluationDate;

    public Autoevaluation() {

    }

    public Autoevaluation(String idStudent, int[] answers) throws IllegalArgumentException {
        this.idStudent = idStudent;
        if (answers == null || answers.length != 10) {
            throw new IllegalArgumentException("Invalid number of answers. Expected 10.");
        }
            this.productiveParticipation = answers[0];
            this.appliedKnowledge = answers[1];
            this.confidenceInActivities = answers[2];
            this.activitiesInterest = answers[3];
            this.organizationSupport = answers[4];
            this.rulesAwareness = answers[5];
            this.supervisorGuidance = answers[6];
            this.effectiveMonitoring = answers[7];
            this.careerAlignment = answers[8];
            this.internshipImportance = answers[9];

            calculateFinalScore();
        }
    
    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public int getProductiveParticipation() {
        return productiveParticipation;
    }

    public void setProductiveParticipation(int productiveParticipation) {
        this.productiveParticipation = productiveParticipation;
    }

    public int getAppliedKnowledge() {
        return appliedKnowledge;
    }

    public void setAppliedKnowledge(int appliedKnowledge) {
        this.appliedKnowledge = appliedKnowledge;
    }

    public int getConfidenceInActivities() {
        return confidenceInActivities;
    }

    public void setConfidenceInActivities(int confidenceInActivities) {
        this.confidenceInActivities = confidenceInActivities;
    }

    public int getActivitiesInterest() {
        return activitiesInterest;
    }

    public void setActivitiesInterest(int activitiesInterest) {
        this.activitiesInterest = activitiesInterest;
    }

    public int getOrganizationSupport() {
        return organizationSupport;
    }

    public void setOrganizationSupport(int organizationSupport) {
        this.organizationSupport = organizationSupport;
    }

    public int getRulesAwareness() {
        return rulesAwareness;
    }

    public void setRulesAwareness(int rulesAwareness) {
        this.rulesAwareness = rulesAwareness;
    }

    public int getSupervisorGuidance() {
        return supervisorGuidance;
    }

    public void setSupervisorGuidance(int supervisorGuidance) {
        this.supervisorGuidance = supervisorGuidance;
    }

    public int getEffectiveMonitoring() {
        return effectiveMonitoring;
    }

    public void setEffectiveMonitoring(int effectiveMonitoring) {
        this.effectiveMonitoring = effectiveMonitoring;
    }

    public int getCareerAlignment() {
        return careerAlignment;
    }

    public void setCareerAlignment(int careerAlignment) {
        this.careerAlignment = careerAlignment;
    }

    public int getInternshipImportance() {
        return internshipImportance;
    }

    public void setInternshipImportance(int internshipImportance) {
        this.internshipImportance = internshipImportance;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(String evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public void calculateFinalScore() {
        double totalScore = productiveParticipation + appliedKnowledge + confidenceInActivities; 
        totalScore += activitiesInterest + organizationSupport + rulesAwareness; 
        totalScore += supervisorGuidance + effectiveMonitoring + careerAlignment;
        totalScore += internshipImportance;
        this.finalScore = (totalScore / MAX_POSSIBLE_SCORE) * PERCENTAGE_MULTIPLIER;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Autoevaluation other = (Autoevaluation) object;
        return Objects.equals(idStudent,other.idStudent)
        && productiveParticipation == other.productiveParticipation
        && appliedKnowledge == other.appliedKnowledge
        && confidenceInActivities == other.confidenceInActivities
        && activitiesInterest == other.activitiesInterest
        && organizationSupport == other.organizationSupport
        && rulesAwareness == other.rulesAwareness
        && supervisorGuidance == other.supervisorGuidance
        && effectiveMonitoring == other.effectiveMonitoring
        && careerAlignment == other.careerAlignment
        && internshipImportance == other.internshipImportance
        && finalScore == other.finalScore;
    }
}
