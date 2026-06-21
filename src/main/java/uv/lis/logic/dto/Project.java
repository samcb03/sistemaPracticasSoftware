package uv.lis.logic.dto;

import java.util.ArrayList;
import java.util.Objects;

public class Project {
    private int id;
    private String name;
    private String methodology;
    private int capacity;
    private String objective;
    private String description;
    private ArrayList<Activity> activities;
    private AffiliatedOrganization affiliatedOrganization;
    private String idStudent;
    private int idAffiliatedOrganization;
    private String affiliatedOrganizationName;
    private int idSupervisor;
    private boolean isActive;

    public Project() {
        
    }

    public Project(String name, String methodology, int capacity, 
        String objective, String description, 
        ArrayList<Activity> activities, String idStudent) {

        this.name = name;
        this.methodology = methodology;
        this.capacity = capacity;
        this.objective = objective;
        this.description = description;
        this.activities = activities;
        this.idStudent = idStudent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity > 0) {
            
        }
        this.capacity = capacity;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }  

    public ArrayList<Activity> getActivities() {
        return activities;
    }
    
    public AffiliatedOrganization getAffiliatedOrganization() {
        return affiliatedOrganization;
    }

    public void setAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) {
        this.affiliatedOrganization = affiliatedOrganization;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public int getIdAffiliatedOrganization() {
        return idAffiliatedOrganization; 
    }
    
    public void setIdAffiliatedOrganization(int id) { 
        this.idAffiliatedOrganization = id; 
    }

    public String getAffiliatedOrganizationName() {
        return affiliatedOrganizationName;
    }

    public void setAffiliatedOrganizationName(String affiliatedOrganizationName) {
        this.affiliatedOrganizationName = affiliatedOrganizationName;
    }

    public void setActivities(ArrayList<Activity> activities) {
        this.activities = activities;
    }

    public int getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(int idSupervisor) {
        this.idSupervisor = idSupervisor;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Project other = (Project) object;
            isEqual = id == other.id
                && capacity == other.capacity
                && Objects.equals(name, other.name)
                && Objects.equals(methodology, other.methodology)
                && Objects.equals(objective, other.objective)
                && Objects.equals(description, other.description)
                && Objects.equals(activities, other.activities)
                && Objects.equals(affiliatedOrganization, other.affiliatedOrganization)
                && Objects.equals(idStudent, other.idStudent);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, methodology, capacity, objective, 
            description, activities, affiliatedOrganization, idStudent);
    }
}