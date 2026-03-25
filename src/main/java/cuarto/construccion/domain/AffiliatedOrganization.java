package cuarto.construccion.domain;


public class AffiliatedOrganization {
    private int idAffiliatedOrganization;
    private String nameAffiliatedOrganization;
    private String city;
    private String state;
    private String email;
    private String phoneNumber;
    private int numberOfDirectUsers;
    private int numberOfIndirectUsers;
   
    public AffiliatedOrganization(int idAffiliatedOrganization, String nameAffiliatedOrganization, String city,
            String state, String email, String phoneNumber, int numberOfDirectUsers, int numberOfIndirectUsers) {
        this.idAffiliatedOrganization = idAffiliatedOrganization;
        this.nameAffiliatedOrganization = nameAffiliatedOrganization;
        this.city = city;
        this.state = state;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.numberOfDirectUsers = numberOfDirectUsers;
        this.numberOfIndirectUsers = numberOfIndirectUsers;
    }

    public int getIdAffiliatedOrganization() {
        return idAffiliatedOrganization;
    }

    public void setIdAffiliatedOrganization(int idAffiliatedOrganization) {
        this.idAffiliatedOrganization = idAffiliatedOrganization;
    }

    public String getNameAffiliatedOrganization() {
        return nameAffiliatedOrganization;
    }

    public void setNameAffiliatedOrganization(String nameAffiliatedOrganization) {
        this.nameAffiliatedOrganization = nameAffiliatedOrganization;
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
}
