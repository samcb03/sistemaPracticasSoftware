package uv.lis.logic.dto;

import java.util.Objects;

public class FinalReport extends Report {

    private String projectName;
    private String projectMethodology;
    private String projectObjective;
    private String affiliatedOrganization;
    private String totalHours;
    private String dateReport;
    private ActivityProgress firstActivity;
    private ActivityProgress secondActivity;
    private DeliverableResult firstDeliverable;
    private DeliverableResult secondDeliverable;
    private String generalObservations;

    public FinalReport() {
        this.firstActivity = new ActivityProgress();
        this.secondActivity = new ActivityProgress();
        this.firstDeliverable = new DeliverableResult();
        this.secondDeliverable = new DeliverableResult();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getAffiliatedOrganization() {
        return affiliatedOrganization;
    }

    public void setAffiliatedOrganization(String affiliatedOrganization) {
        this.affiliatedOrganization = affiliatedOrganization;
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

    public ActivityProgress getFirstActivity() {
        return firstActivity;
    }

    public void setFirstActivity(ActivityProgress firstActivity) {
        this.firstActivity = firstActivity;
    }

    public ActivityProgress getSecondActivity() {
        return secondActivity;
    }

    public void setSecondActivity(ActivityProgress secondActivity) {
        this.secondActivity = secondActivity;
    }

    public DeliverableResult getFirstDeliverable() {
        return firstDeliverable;
    }

    public void setFirstDeliverable(DeliverableResult firstDeliverable) {
        this.firstDeliverable = firstDeliverable;
    }

    public DeliverableResult getSecondDeliverable() {
        return secondDeliverable;
    }

    public void setSecondDeliverable(DeliverableResult secondDeliverable) {
        this.secondDeliverable = secondDeliverable;
    }

    public String getGeneralObservations() {
        return generalObservations;
    }

    public void setGeneralObservations(String generalObservations) {
        this.generalObservations = generalObservations;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            FinalReport other = (FinalReport) object;
            
            isEqual = super.equals(object)
                && Objects.equals(projectName, other.projectName)
                && Objects.equals(projectMethodology, other.projectMethodology)
                && Objects.equals(projectObjective, other.projectObjective)
                && Objects.equals(affiliatedOrganization, other.affiliatedOrganization)
                && Objects.equals(totalHours, other.totalHours)
                && Objects.equals(dateReport, other.dateReport)
                && Objects.equals(firstActivity, other.firstActivity)
                && Objects.equals(secondActivity, other.secondActivity)
                && Objects.equals(firstDeliverable, other.firstDeliverable)
                && Objects.equals(secondDeliverable, other.secondDeliverable)
                && Objects.equals(generalObservations, other.generalObservations);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(super.hashCode(), projectName, projectMethodology,
            projectObjective, affiliatedOrganization, totalHours, dateReport,
            firstActivity, secondActivity, firstDeliverable, secondDeliverable,
            generalObservations);
        return hash;
    }
}