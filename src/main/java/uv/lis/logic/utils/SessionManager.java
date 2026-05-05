package uv.lis.logic.utils;

import uv.lis.logic.dto.Student;

public class SessionManager {

    private static SessionManager instance;
    private Student currentStudent;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Student getCurrentStudent() {
        return currentStudent;
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
    }

    public void clearSession() {
        this.currentStudent = null;
    }
}