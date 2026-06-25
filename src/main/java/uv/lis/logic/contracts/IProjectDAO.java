package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.ProjectSummary;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for projects.
 */
public interface IProjectDAO {

    /**
     * Retrieves all the registered projects.
     *
     * @return the list of projects, empty if there are none
     * @throws OperationException if the projects cannot be retrieved
     */
    List<Project> getAllProjects() throws OperationException;

    /**
     * Retrieves the project identified by the given name.
     *
     * @param projectName the name of the project to retrieve
     * @return the project if it exists, empty otherwise
     * @throws OperationException if the project cannot be retrieved
     */
    Optional<Project> getProjectByName(String projectName) throws OperationException;

    /**
     * Registers a new project in the system.
     *
     * @param project the project data to register
     * @return true if the project was registered, false otherwise
     * @throws OperationException if the project cannot be registered
     */
    boolean registerProject(Project project) throws OperationException;

    /**
     * Updates the data of an existing project.
     *
     * @param project the project data to update
     * @return true if the project was updated, false otherwise
     * @throws OperationException if the project cannot be updated
     */
    boolean modifyProject(Project project) throws OperationException;

    /**
     * Marks a project as inactive.
     *
     * @param project the project to inactivate
     * @return true if the project was inactivated, false otherwise
     * @throws OperationException if the project cannot be inactivated
     */
    boolean inactivateProject(Project project) throws OperationException;

    /**
     * Retrieves the names of all the registered projects.
     *
     * @return the list of project names, empty if there are none
     * @throws OperationException if the project names cannot be retrieved
     */
    ArrayList<String> getAllProjectNames() throws OperationException;

    /**
     * Retrieves the name of the project led by a supervisor.
     *
     * @param supervisorName the name of the supervisor to look up
     * @return the project name if it exists, empty otherwise
     * @throws OperationException if the project name cannot be retrieved
     */
    Optional<String> getProjectBySupervisorName(String supervisorName) throws OperationException;

    /**
     * Retrieves the project assigned to a student.
     *
     * @param studentId the identifier of the student to look up
     * @return the project if the student has one assigned, empty otherwise
     * @throws OperationException if the project cannot be retrieved
     */
    Optional<Project> getProjectByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the names of the projects that belong to an organization.
     *
     * @param organizationId the identifier of the organization to query
     * @return the list of project names, empty if there are none
     * @throws OperationException if the project names cannot be retrieved
     */
    ArrayList<String> getProjectNamesByOrganizationId(int organizationId) throws OperationException;

    /**
     * Retrieves all the projects that still have available capacity.
     *
     * @return the list of projects with available capacity, empty if there are none
     * @throws OperationException if the projects cannot be retrieved
     */
    ArrayList<Project> getAllProjectsWithCapacity() throws OperationException;

    /**
     * Retrieves the project summary details assigned to a student.
     *
     * @param studentId the identifier of the student to look up
     * @return the project summary if the student has one assigned, empty otherwise
     * @throws OperationException if the project summary cannot be retrieved
     */
    Optional<ProjectSummary> getProjectDetailsByStudentId(String studentId) throws OperationException;
}