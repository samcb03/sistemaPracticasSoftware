package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for project supervisors.
 */
public interface IProjectSupervisorDAO {

    /**
     * Retrieves the supervisor linked to the given project.
     *
     * @param idProject the identifier of the project to look up
     * @return the project supervisor if it exists, empty otherwise
     * @throws OperationException if the supervisor cannot be retrieved
     */
    Optional<ProjectSupervisor> getProjectSupervisorById(int idProject) throws OperationException;

    /**
     * Registers a new project supervisor in the system.
     *
     * @param projectSupervisor the supervisor data to register
     * @return true if the supervisor was registered, false otherwise
     * @throws OperationException if the supervisor cannot be registered
     */
    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    /**
     * Updates the data of an existing project supervisor.
     *
     * @param projectSupervisor the supervisor data to update
     * @return true if the supervisor was updated, false otherwise
     * @throws OperationException if the supervisor cannot be updated
     */
    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    /**
     * Retrieves the names of all the registered project supervisors.
     *
     * @return the list of supervisor names, empty if there are none
     * @throws OperationException if the supervisor names cannot be retrieved
     */
    ArrayList<String> getAllSupervisorNames() throws OperationException;

    /**
     * Retrieves the supervisor names that start with the given prefix.
     *
     * @param prefix the prefix used to filter supervisor names
     * @return the list of matching supervisor names, empty if there are none
     * @throws OperationException if the supervisor names cannot be retrieved
     */
    ArrayList<String> searchProjectSupervisorName(String prefix) throws OperationException;

    /**
     * Retrieves the project supervisor identified by the given name.
     *
     * @param supervisorName the name of the supervisor to retrieve
     * @return the supervisor if it exists, empty otherwise
     * @throws OperationException if the supervisor cannot be retrieved
     */
    Optional<ProjectSupervisor> getProjectSupervisorByName(String supervisorName) throws OperationException;

    /**
     * Retrieves the names of the projects led by a supervisor.
     *
     * @param supervisorName the name of the supervisor to query
     * @return the list of project names, empty if there are none
     * @throws OperationException if the project names cannot be retrieved
     */
    ArrayList<String> getProjectsBySupervisorName(String supervisorName) throws OperationException;

    /**
     * Retrieves the identifier of a supervisor from their name.
     *
     * @param supervisorName the name of the supervisor to look up
     * @return the identifier of the supervisor
     * @throws OperationException if the identifier cannot be retrieved
     */
    int getSupervisorIdByName(String supervisorName) throws OperationException;

    /**
     * Retrieves the supervisor names that belong to an organization.
     *
     * @param organizationId the identifier of the organization to query
     * @return the list of supervisor names, empty if there are none
     * @throws OperationException if the supervisor names cannot be retrieved
     */
    ArrayList<String> getSupervisorsByOrganizationId(int organizationId) throws OperationException;

    /**
     * Indicates whether a project supervisor is inactive.
     *
     * @param name the name of the supervisor to verify
     * @return true if the supervisor is inactive, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isSupervisorInactive(String name) throws OperationException;

    /**
     * Marks a project supervisor as inactive.
     *
     * @param projectSupervisorName the name of the supervisor to inactivate
     * @return true if the supervisor was inactivated, false otherwise
     * @throws OperationException if the supervisor cannot be inactivated
     */
    boolean inactivateProjectSupervisor(String projectSupervisorName) throws OperationException;

    /**
     * Indicates whether a supervisor has active projects.
     *
     * @param supervisorName the name of the supervisor to verify
     * @return true if the supervisor has active projects, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasProjectsActives(String supervisorName) throws OperationException;
}