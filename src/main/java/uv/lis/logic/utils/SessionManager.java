package uv.lis.logic.utils;

import java.util.Optional;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;

public class SessionManager {
    private static SessionManager instance;
    private Student currentStudent;
    private Professor currentProfessor;

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

    public Professor getCurrentProfessor() {
        return currentProfessor;
    }

    public void setCurrentProfessor(Professor professor) {
        this.currentProfessor = professor;
    }

    public Optional<Professor> getCurrentCoordinator() {
        Optional<Professor> currentProfessorOpt = Optional.empty();
        if (currentProfessor != null && currentProfessor.getIsCoordinator()) {
            currentProfessorOpt = Optional.of(currentProfessor);
        }
        return currentProfessorOpt;
    }

    public void setCurrentCoordinator(Professor coordinator) {
        if (coordinator != null) {
            coordinator.setIsCoordinator(true);
        }
        this.currentProfessor = coordinator;
    }


    public void clearSession() {
        this.currentStudent = null;
        this.currentProfessor = null;
    }
}