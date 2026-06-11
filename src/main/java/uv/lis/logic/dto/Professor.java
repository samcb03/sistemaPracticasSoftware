package uv.lis.logic.dto;

import java.util.Objects;

public class Professor extends User {
    private String personnelNumber;
    private boolean isCoordinator;

    public Professor() {
        
    }
   
    public Professor(int idUser, String firstName, String lastName, String password, String email, int roleId, 
            boolean isInactive, String personnelNumber, boolean isCoordinator) {
        super(idUser, firstName, lastName, password, email, roleId, isInactive);
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
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Professor other = (Professor) object;
            isEqual = isCoordinator == other.isCoordinator
                && Objects.equals(personnelNumber, other.personnelNumber);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName(), getPassword(), 
                            getEmail(), getRoleId(), isActive(), personnelNumber, isCoordinator);
    }
}
