package uv.lis.logic.dto;

import java.util.Objects;

public class ProjectSupervisor {
    private int id;
    private String name;
    private String email;
    private String position;

    public ProjectSupervisor(){

    }
    
    public ProjectSupervisor(String name, String lastName, String email, String position) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

        @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        ProjectSupervisor other = (ProjectSupervisor) object;
        return id == other.id
            && Objects.equals(name, other.name)
            && Objects.equals(email, other.email)
            && Objects.equals(position, other.position);
    }
}
