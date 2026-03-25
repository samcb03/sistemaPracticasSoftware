package cuarto.construccion.domain;


public class Professor extends User {
    private int professorId;
    private boolean isCoordinator;
   
    public Professor(int idUser, String firstName, String lastName, int professorId, boolean isCoordinator) {
        super(idUser, firstName, lastName);
        this.professorId = professorId;
        this.isCoordinator = isCoordinator;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }
}