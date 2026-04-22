package uv.lis.logic.dto;


public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String password;
    private String identification;
    private String userType;
    private boolean isInactive;

    public User(){
    }

    public User(int id, String firstName, String lastName, String password, String userType, boolean isInactive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
        this.isInactive = isInactive;
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

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isInactive() {
        return isInactive;
    }

    public void setInactive(boolean isInactive) {
        this.isInactive = isInactive;
    }
}