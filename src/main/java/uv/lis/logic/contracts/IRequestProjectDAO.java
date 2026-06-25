package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for project requests and assignments.
 */
public interface IRequestProjectDAO {

    /**
     * Retrieves the number of active project requests made by a student.
     *
     * @param id the identifier of the student to query
     * @return the number of active requests of the student
     * @throws OperationException if the request count cannot be retrieved
     */
    int getActiveRequestCountByStudentId(String id) throws OperationException;

    /**
     * Indicates whether a project still has available capacity.
     *
     * @param idProject the identifier of the project to verify
     * @return true if the project has available capacity, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasAvailableCapacity(int idProject) throws OperationException;

    /**
     * Registers a student's request for a project.
     *
     * @param id the identifier of the student who makes the request
     * @param idProject the identifier of the requested project
     * @return true if the request was registered, false otherwise
     * @throws OperationException if the request cannot be registered
     */
    boolean requestProject(String id, int idProject) throws OperationException;

    /**
     * Retrieves the projects that are available to be requested.
     *
     * @return the list of available projects, empty if there are none
     * @throws OperationException if the projects cannot be retrieved
     */
    List<Project> getAvailableProjects() throws OperationException;

    /**
     * Indicates whether a student has already requested a project.
     *
     * @param id the identifier of the student to verify
     * @param idProject the identifier of the project to verify
     * @return true if the student already requested the project, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasAlreadyRequested(String id, int idProject) throws OperationException;

    /**
     * Validates a student's request for a project.
     *
     * @param idStudent the identifier of the student who makes the request
     * @param idProject the identifier of the requested project
     * @return a validation error message if the request is invalid, empty otherwise
     * @throws OperationException if the request cannot be validated
     */
    Optional<String> validateProjectRequest(String idStudent, int idProject) throws OperationException;

    /**
     * Assigns a student to a project.
     *
     * @param idStudent the identifier of the student to assign
     * @param idProject the identifier of the project to assign the student to
     * @return true if the student was assigned, false otherwise
     * @throws OperationException if the student cannot be assigned
     */
    boolean assignStudentToProject(String idStudent, int idProject, boolean isAlternative) throws OperationException;

    /**
     * Retrieves the students that applied to a project.
     *
     * @param idProject the identifier of the project to query
     * @return the list of applicant students, empty if there are none
     * @throws OperationException if the applicants cannot be retrieved
     */
    List<Student> getApplicantsByProjectId(int idProject) throws OperationException;

    /**
     * Retrieves the name of the project assigned to a student.
     *
     * @param idStudent the identifier of the student to query
     * @return the name of the assigned project
     * @throws OperationException if the project cannot be retrieved
     */
    String getProjectAssignedToStudent(String idStudent) throws OperationException;

    /**
     * Removes the project assignment of a student.
     *
     * @param idStudent the identifier of the student to unassign
     * @throws OperationException if the student cannot be unassigned
     */
    void unassignStudentFromProject(String idStudent) throws OperationException;

    /**
     * Retrieves the students assigned to a project.
     *
     * @param idProject the identifier of the project to query
     * @return the list of assigned students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    ArrayList<Student> getAssignedStudentsByProjectId(int idProject) throws OperationException;

    /**
     * Retrieves the students that do not have a project assigned.
     *
     * @return the list of unassigned students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    ArrayList<Student> getStudentsWithoutAssignedProject() throws OperationException;
}