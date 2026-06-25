package uv.lis.logic.dto;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REQUESTED;

import java.util.Objects;

public class Expedient {

    private int id;
    private String name;
    private String typeDocument;
    private String url;
    private String idStudent;
    private int idTypeDocument;
    private int idStatus;
    private String statusName;
    private int idReport;

    public Expedient() {
    }

    public Expedient(String name, String typeDocument, String url, String idStudent, int idTypeDocument) {
        this.name = name;
        this.typeDocument = typeDocument;
        this.url = url;
        this.idStudent = idStudent;
        this.idTypeDocument = idTypeDocument;
        this.idStatus = STATUS_REQUESTED;
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

    public int getIdTypeDocument() {
        return idTypeDocument;
    }

    public void setIdTypeDocument(int idTypeDocument) {
        this.idTypeDocument = idTypeDocument;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public boolean isValidated() {
        return idStatus == STATUS_ASSIGNED;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual;

        if (this == object) {
            isEqual = true;
        } else if (object == null || getClass() != object.getClass()) {
            isEqual = false;
        } else {
            Expedient other = (Expedient) object;
            isEqual = Objects.equals(name, other.name)
                && Objects.equals(typeDocument, other.typeDocument)
                && Objects.equals(url, other.url)
                && Objects.equals(idStudent, other.idStudent);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(name, typeDocument, url, idStudent);
        return hash;
    }
}