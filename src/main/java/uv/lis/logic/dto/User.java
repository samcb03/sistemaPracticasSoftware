package uv.lis.logic.dto;


import java.util.Objects;


public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private int roleId;
    private boolean isActive;

    public User(){
    }

    public User(int id, String firstName, String lastName, String password, String email, int roleId,
            boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.roleId = roleId;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        User other = (User) object;
        return id == other.id
            && Objects.equals(firstName, other.firstName)
            && Objects.equals(lastName, other.lastName)
            && Objects.equals(password, other.password)
            && Objects.equals(email, other.email)
            && Objects.equals(roleId, other.roleId)
            && Objects.equals(isActive, other.isActive);
    }
}