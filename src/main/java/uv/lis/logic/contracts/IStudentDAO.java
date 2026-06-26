package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for students.
 */
public interface IStudentDAO {

    /**
     * Retrieves all active students registered in the system.
     *
     * @return the list of active students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    public ArrayList<Student> getAllActiveStudents() throws OperationException;

    /**
     * Retrieves the student identified by the given id.
     *
     * @param idStudent the identifier of the student to retrieve
     * @return the student if it exists, empty otherwise
     * @throws OperationException if the student cannot be retrieved
     */
    Optional<Student> getStudentById(int idStudent) throws OperationException;

    /**
     * Retrieves the active students that are not enrolled in any subject.
     *
     * @return the list of unenrolled active students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    ArrayList<Student> getActiveStudentsNotInSubject() throws OperationException;

    /**
     * Registers a new student in the system.
     *
     * @param student the student data to register
     * @return true if the student was registered, false otherwise
     * @throws OperationException if the student cannot be registered
     */
    boolean registerStudent(Student student) throws OperationException;

    /**
     * Updates the data of an existing student.
     *
     * @param student the student data to update
     * @return true if the student was updated, false otherwise
     * @throws OperationException if the student cannot be updated
     */
    boolean modifyStudent(Student student) throws OperationException;

    /**
     * Marks a student as inactive.
     *
     * @param studentId the identifier of the student to inactivate
     * @return true if the student was inactivated, false otherwise
     * @throws OperationException if the student cannot be inactivated
     */
    boolean inactivateStudent(String studentId) throws OperationException;

    /**
     * Retrieves the user identifier associated with a student.
     *
     * @param studentId the identifier of the student to look up
     * @return the associated user identifier if it exists, empty otherwise
     * @throws OperationException if the user identifier cannot be retrieved
     */
    Optional<Integer> getIdUserByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the student identifiers that start with the given prefix.
     *
     * @param prefix the prefix used to filter student identifiers
     * @return the list of matching student identifiers, empty if there are none
     * @throws OperationException if the student identifiers cannot be retrieved
     */
    ArrayList<String> searchStudentIds(String prefix) throws OperationException;

    /**
     * Indicates whether a student is inactive.
     *
     * @param studentId the identifier of the student to verify
     * @return true if the student is inactive, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isStudentInactive(String studentId) throws OperationException;

    /**
     * Indicates whether a student has a project assigned.
     *
     * @param studentId the identifier of the student to verify
     * @return true if the student has a project assigned, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasProjectAssigned(String studentId) throws OperationException;

    /**
     * Retrieves the students identified by the given list of ids.
     *
     * @param studentIds the list of student identifiers to look up
     * @return the list of matching students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    List<Student> getStudentsByIds(List<String> studentIds) throws OperationException;
}