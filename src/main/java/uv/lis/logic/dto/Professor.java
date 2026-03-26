package uv.lis.logic.dto;


public class Professor extends User {
    private int id;
    private boolean isCoordinator;
   
    public Professor(int idUser, String firstName, String lastName, int id, boolean isCoordinator) {
        super(idUser, firstName, lastName);
        this.id = id;
        this.isCoordinator = isCoordinator;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIsCoordinator() {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }
}