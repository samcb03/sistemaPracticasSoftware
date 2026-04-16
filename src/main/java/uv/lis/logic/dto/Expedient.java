package uv.lis.logic.dto;

public class Expedient {
    private int id;
    private String name;
    private String typeDocument;
    private String url;

    public Expedient(String name, String typeDocument, String url) {
        generateId();
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

    private int counter = 1;
    private void generateId() {
        this.id = counter;
        counter++;
    }
}
