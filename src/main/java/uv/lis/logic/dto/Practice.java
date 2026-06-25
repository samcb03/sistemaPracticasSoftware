package uv.lis.logic.dto;

public class Practice {
    private int idPractice;
    private int calification;
    private String idStudent;

    public Practice() {

    }

    public int getIdPractice() { 
        return idPractice; 
    }

    public void setIdPractice(int idPractice) { 
        this.idPractice = idPractice; 
    }

    public int getCalification() { 
        return calification; 
    }
    
    public void setCalification(int calification) { 
        this.calification = calification; 
    }

    public String getidStudent() { 
        return idStudent; 
    }

    public void setidStudent(String idStudent) { 
        this.idStudent = idStudent; 
    }
}