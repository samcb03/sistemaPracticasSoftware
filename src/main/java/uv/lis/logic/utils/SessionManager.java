package uv.lis.logic.utils;

import java.util.Optional;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;

/**
 * Holds the identity of the user who is currently logged in,
 * so that any part of the application can know who is operating
 * without passing user data through every method call.
 *
 * Only one session exists at a time, shared across the entire application.
 */
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private Student currentStudent;
    private Professor currentProfessor;

    private SessionManager() {
        // Singleton, instantiation is not allowed outside this class
    }

    /**
     * Returns the single instance shared across the application.
     *
     * @return the global SessionManager instance
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the student who is currently logged in.
     *
     * @return the current student, or null if no student session is active
     */
    public Student getCurrentStudent() {
        return currentStudent;
    }

    /**
     * Registers a student as the active user of the session.
     *
     * @param student the student who has logged in
     */
    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
    }

    /**
     * Returns the professor who is currently logged in.
     *
     * @return the current professor, or null if no professor session is active
     */
    public Professor getCurrentProfessor() {
        return currentProfessor;
    }

    /**
     * Registers a professor as the active user of the session.
     *
     * @param professor the professor who has logged in
     */
    public void setCurrentProfessor(Professor professor) {
        this.currentProfessor = professor;
    }

    /**
     * Returns the current professor only if they have coordinator privileges,
     * so callers can gate coordinator-only features without checking the role manually.
     *
     * @return the current professor wrapped in an Optional if they are a coordinator,
     *         or an empty Optional otherwise
     */
    public Optional<Professor> getCurrentCoordinator() {
        Optional<Professor> currentProfessorOpt = Optional.empty();
        if (currentProfessor != null && currentProfessor.getIsCoordinator()) {
            currentProfessorOpt = Optional.of(currentProfessor);
        }
        return currentProfessorOpt;
    }

    /**
     * Registers a professor as the active user and marks them as coordinator,
     * ensuring the role is always consistent with how they logged in.
     *
     * @param coordinator the professor who has logged in as coordinator
     */
    public void setCurrentCoordinator(Professor coordinator) {
        if (coordinator != null) {
            coordinator.setIsCoordinator(true);
        }
        this.currentProfessor = coordinator;
    }

    /**
     * Removes all user data from the session when the user logs out,
     * so no identity or role information persists after the session ends.
     */
    public void clearSession() {
        this.currentStudent = null;
        this.currentProfessor = null;
    }
}