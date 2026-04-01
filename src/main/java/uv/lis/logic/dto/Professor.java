package uv.lis.logic.dto;


public class Professor extends User {
    private String numeroPersonal;
    private boolean isCoordinator;

    public Professor(){
        
    }
   


    public Professor(int idUser, String firstName, String lastName,String password, String numeroPersonal, boolean isCoordinator) {
        super(idUser, firstName, lastName, password);
        this.numeroPersonal = numeroPersonal;
        this.isCoordinator = isCoordinator;
    }

    public String getNumeroPersonal() {
        return numeroPersonal;
    }

    public void setNumeroPersonal(String numeroPersonal) {
        this.numeroPersonal = numeroPersonal;
    }

    public boolean getIsCoordinator() {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }
}