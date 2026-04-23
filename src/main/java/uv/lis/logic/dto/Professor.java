package uv.lis.logic.dto;

import java.util.Objects;

public class Professor extends User {
    private String personnelNumber;
    private boolean isCoordinator;

    public Professor(){
        
    }
   
    public Professor(int idUser, String firstName, String lastName,String password, String personnelNumber, boolean isCoordinator) {
        super(idUser, firstName, lastName, password, "Profesor", true);
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Professor other = (Professor) object;
        return personnelNumber == other.personnelNumber
            && Objects.equals(isCoordinator, other.isCoordinator);
    }
}
