package uv.lis.logic.dto;

import java.sql.Timestamp;
import java.util.Objects;

public class Notification {
    private int id;
    private String idStudent;
    private String title;
    private String message;
    private Timestamp creationDate;
    private boolean isRead;

    public Notification() {

    }

    public Notification(int id, String idStudent, String title, String message, Timestamp creationDate, 
            boolean isRead) {
        this.id = id;
        this.idStudent = idStudent;
        this.title = title;
        this.message = message;
        this.creationDate = creationDate;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(String idStudent) {
        this.idStudent = idStudent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object == null || getClass() != object.getClass()) {
            isEqual = false;
        } else {
            Notification other = (Notification) object;
            isEqual = other.id == id
                && other.idStudent == idStudent
                && other.title == title
                && other.message == message
                && other.creationDate == creationDate
                && other.isRead == isRead;
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idStudent);
    }
}