package uv.lis.logic.dto;

public class ProjectSummary {
    private String professorName;
    private String organizationName;
    private String projectName;
    private String studentName;
    private String description;
    private String objective;
    private String methodology;

    public String getProfessorName() {
        return professorName;
    }
    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getObjective() {
        return objective;
    }
    public void setObjective(String objective) {
        this.objective = objective;
    }
    public String getMethodology() {
        return methodology;
    }
    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectSummary that = (ProjectSummary) o;
        return java.util.Objects.equals(projectName, that.projectName)
        && java.util.Objects.equals(professorName, that.professorName)
        && java.util.Objects.equals(methodology, that.methodology)
        && java.util.Objects.equals(objective, that.objective)
        && java.util.Objects.equals(description, that.description)
        && java.util.Objects.equals(organizationName, that.organizationName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(projectName, professorName, methodology,
            objective, description, organizationName);
    }
}
