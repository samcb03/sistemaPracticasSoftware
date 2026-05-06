package uv.lis.logic.dto;


import java.util.Objects;


public class Expedient {
    private String name;
    private String typeDocument;
    private String url;
    private String idStudent;
    private int idTypeDocument;

    public Expedient(String name, String typeDocument, 
        String url, String idStudent, int idTypeDocument) {
        this.name = name;
        this.typeDocument = typeDocument;
        this.url = url;
        this.idStudent = idStudent;
        this.idTypeDocument = idTypeDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    
    public int getIdTypeDocument() {
        return idTypeDocument;
    }

    public void setIdTypeDocument(int idTypeDocument) {
        this.idTypeDocument = idTypeDocument;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Expedient other = (Expedient) object;
        return name == other.name
        && Objects.equals(typeDocument, other.typeDocument)
        && Objects.equals(url, other.url)
        && Objects.equals(idStudent, other.idStudent);
    }
}
