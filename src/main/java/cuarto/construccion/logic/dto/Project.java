package cuarto.construccion.logic.dto;


public class Project {
    private int idProject;
    private String projectName;
    private String projectMethodology;
    private int capacity;
    private String projectObjective;
    private String projectDescription;
    
    public Project() {
    }

    public Project(int idProject, String projectName, String projectMethodology, int capacity, 
            String projectObjective, String projectDescription) {
        this.idProject = idProject;
        this.projectName = projectName;
        this.projectMethodology = projectMethodology;
        this.capacity = capacity;
        this.projectObjective = projectObjective;
        this.projectDescription = projectDescription;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getProjectObjective() {
        return projectObjective;
    }

    public void setProjectObjective(String projectObjective) {
        this.projectObjective = projectObjective;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }  
}