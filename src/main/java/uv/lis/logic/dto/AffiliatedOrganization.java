package uv.lis.logic.dto;

import java.util.Objects;

public class AffiliatedOrganization {
    private int id;
    private String name;
    private String city;
    private String state;
    private String street;
    private String streetNumber;
    private String postalCode;
    private String sector;
    private String email;
    private String phoneNumber;
    private int numberOfDirectUsers;
    private int numberOfIndirectUsers;
    private ProjectSupervisor projectSupervisor;
    private int counter = 0;

    public AffiliatedOrganization(){       
        
    }
   
    public AffiliatedOrganization(String name, String city,
            String state, String sector,String email, String phoneNumber, int numberOfDirectUsers, 
            int numberOfIndirectUsers) {
        this.id = generateId();
        this.name = name;
        this.city = city;
        this.state = state;
        this.sector = sector;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.numberOfDirectUsers = numberOfDirectUsers;
        this.numberOfIndirectUsers = numberOfIndirectUsers;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getNumberOfDirectUsers() {
        return numberOfDirectUsers;
    }

    public void setNumberOfDirectUsers(int numberOfDirectUsers) {
        this.numberOfDirectUsers = numberOfDirectUsers;
    }

    public int getNumberOfIndirectUsers() {
        return numberOfIndirectUsers;
    }

    public void setNumberOfIndirectUsers(int numberOfIndirectUsers) {
        this.numberOfIndirectUsers = numberOfIndirectUsers;
    } 

    private int generateId() {
        return counter++;
    }

    public ProjectSupervisor getProjectSupervisor() {
        return projectSupervisor;
    }

    public void setProjectSupervisor(ProjectSupervisor projectSupervisor) {
        this.projectSupervisor = projectSupervisor;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            AffiliatedOrganization other = (AffiliatedOrganization) object;
            isEqual = id == other.id
                && numberOfDirectUsers == other.numberOfDirectUsers
                && numberOfIndirectUsers == other.numberOfIndirectUsers
                && Objects.equals(name, other.name)
                && Objects.equals(city, other.city)
                && Objects.equals(state, other.state)
                && Objects.equals(sector, other.sector)
                && Objects.equals(email, other.email)
                && Objects.equals(phoneNumber, other.phoneNumber);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, city, state, sector, email, phoneNumber, numberOfDirectUsers, 
            numberOfIndirectUsers);
    }
}
