package uv.lis.logic.dto;

import java.util.ArrayList;

public class Project {
    private int id;
    private String name;
    private String methodology;
    private int capacity;
    private String objective;
    private String description;
    private int counter = 0;
    private ArrayList<Activity> activities;
    private AffiliatedOrganization affiliatedOrganization;
    private String idStudent;

    public Project() {
        
    }

    public Project(String name, String methodology, int capacity, String objective, String description, ArrayList<Activity> activities, String idStudent) {
        this.id = generateId();
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
    
    private int generateId() {
        return ++counter;
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
}