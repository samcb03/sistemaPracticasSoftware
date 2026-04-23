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


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Project other = (Project) object;
        return id == other.id
            && Objects.equals(name, other.name)
            && Objects.equals(methodology, other.methodology)
            && capacity == other.capacity
            && Objects.equals(objective, other.objective)
            && Objects.equals(description, other.description)
            && Objects.equals(activities, other.activities)
            && Objects.equals(affiliatedOrganization, other.affiliatedOrganization)
            && Objects.equals(idStudent, other.idStudent);
    }
}