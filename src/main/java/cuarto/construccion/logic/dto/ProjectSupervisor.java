package cuarto.construccion.logic.dto;


public class ProjectSupervisor {
    private int idProjectSupervisor;
    private String nameProjectSupervisor;
    private String lastNameProjectSupervisor;
    private String emailProjectSupervisor;
    
    public ProjectSupervisor(int idProjectSupervisor, String nameProjectSupervisor, String lastNameProjectSupervisor,
            String emailProjectSupervisor) {
        this.idProjectSupervisor = idProjectSupervisor;
        this.nameProjectSupervisor = nameProjectSupervisor;
        this.lastNameProjectSupervisor = lastNameProjectSupervisor;
        this.emailProjectSupervisor = emailProjectSupervisor;
    }

    public int getIdProjectSupervisor() {
        return idProjectSupervisor;
    }

    public void setIdProjectSupervisor(int idProjectSupervisor) {
        this.idProjectSupervisor = idProjectSupervisor;
    }

    public String getNameProjectSupervisor() {
        return nameProjectSupervisor;
    }

    public void setNameProjectSupervisor(String nameProjectSupervisor) {
        this.nameProjectSupervisor = nameProjectSupervisor;
    }

    public String getLastNameProjectSupervisor() {
        return lastNameProjectSupervisor;
    }

    public void setLastNameProjectSupervisor(String lastNameProjectSupervisor) {
        this.lastNameProjectSupervisor = lastNameProjectSupervisor;
    }

    public String getEmailProjectSupervisor() {
        return emailProjectSupervisor;
    }

    public void setEmailProjectSupervisor(String emailProjectSupervisor) {
        this.emailProjectSupervisor = emailProjectSupervisor;
    }

      
}
