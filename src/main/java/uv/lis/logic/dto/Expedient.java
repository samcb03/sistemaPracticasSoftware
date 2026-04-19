package uv.lis.logic.dto;

public class Expedient {
    private int id;
    private String enrollment;
    private String name;
    private String typeDocument;
    private String url;
    private int idStudent;

    public Expedient(int id,String enrollment, String name, String typeDocument, 
        String url, int idStudent) {
        this.id = id;
        this.enrollment=enrollment;
        this.name = name;
        this.typeDocument = typeDocument;
        this.url = url;
        this.idStudent = idStudent;
    }

    public Expedient(String name, String typeDocument, String url) {
    this.name = name;
    this.typeDocument = typeDocument;
    this.url = url;
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
    public int getIdStudent() {
        return idStudent;
    }
    public void setIdStudent(int idStudent) {
        this.idStudent = idStudent;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }
}
