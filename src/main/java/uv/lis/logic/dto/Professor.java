package uv.lis.logic.dto;


public class Professor extends User {
    private String personnelNumber;
    private boolean isCoordinator;

    public Professor(){
        
    }
   
    public Professor(int idUser, String firstName, String lastName,String password, String personnelNumber, boolean isCoordinator) {
        super(idUser, firstName, lastName, password);
        this.personnelNumber = personnelNumber;
        this.isCoordinator = isCoordinator;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    public boolean getIsCoordinator() {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }
}