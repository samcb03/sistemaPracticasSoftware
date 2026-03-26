package uv.lis.logic.dto;


public class Project {
    private int id;
    private String name;
    private String methodology;
    private int capacity;
    private String objective;
    private String description;
    
    public Project() {
    }

    public Project(int id, String name, String methodology, int capacity, 
            String objective, String description) {
        this.id = id;
        this.name = name;
        this.methodology = methodology;
        this.capacity = capacity;
        this.objective = objective;
        this.description = description;
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
}