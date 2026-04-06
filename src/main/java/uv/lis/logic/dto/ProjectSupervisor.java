package uv.lis.logic.dto;


public class ProjectSupervisor {
    private int id;
    private String name;
    private String lastName;
    private String email;
    private String position;
    private int counter = 0;

    public ProjectSupervisor(){

    }
    
    public ProjectSupervisor(String name, String lastName, String email, String position) {
        this.id = generateId();
        this.name = name;
        this.lastName = lastName;
        this.position = position;
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    private int generateId() {
        return counter++;
    }
}
