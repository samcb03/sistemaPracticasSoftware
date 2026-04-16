package uv.lis.logic.dto;

public class Expedient {
    private int id;
    private String name;
    private String typeDocument;
    private String url;
    private String idStudent;

    public Expedient(int id, String name, String typeDocument, String url, String idStudent) {
        this.id = id;
        this.name = name;
        this.typeDocument = typeDocument;
        this.url = url;
        this.idStudent = idStudent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
