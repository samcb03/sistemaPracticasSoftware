package uv.lis.logic.utils;


import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Professor;


public class SessionManager {
    private static Student currentStudent;
    private static Professor currentProfessor;

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static void setCurrentStudent(Student student) {
        SessionManager.currentStudent = student;
    }

    public static Professor getCurrentProfessor() {
        return currentProfessor;
    }

    public static void setCurrentProfessor(Professor professor) {
        SessionManager.currentProfessor = professor;
    }

    public static void clearSession() {
        currentStudent = null;
        currentProfessor = null;
    }
}