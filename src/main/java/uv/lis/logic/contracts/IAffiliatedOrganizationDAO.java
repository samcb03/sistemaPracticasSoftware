package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for affiliated organizations.
 */
public interface IAffiliatedOrganizationDAO {

    /**
     * Retrieves the affiliated organization identified by the given id.
     *
     * @param idAfilliatedOrganization the identifier of the organization to retrieve
     * @return the organization if it exists, empty otherwise
     * @throws OperationException if the organization cannot be retrieved
     */
    Optional<AffiliatedOrganization> getOrganizationById(int idAfilliatedOrganization) throws OperationException;

    /**
     * Registers a new affiliated organization in the system.
     *
     * @param affiliatedOrganization the organization data to register
     * @return true if the organization was registered, false otherwise
     * @throws OperationException if the organization cannot be registered
     */
    boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    /**
     * Updates the data of an existing affiliated organization.
     *
     * @param affiliatedOrganization the organization data to update
     * @return true if the organization was updated, false otherwise
     * @throws OperationException if the organization cannot be updated
     */
    boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    /**
     * Marks an affiliated organization as inactive.
     *
     * @param organizationName the name of the organization to inactivate
     * @return true if the organization was inactivated, false otherwise
     * @throws OperationException if the organization cannot be inactivated
     */
    boolean inactivateOrganization(String organizationName) throws OperationException;

    /**
     * Retrieves the names of all the registered affiliated organizations.
     *
     * @return the list of organization names, empty if there are none
     * @throws OperationException if the organization names cannot be retrieved
     */
    ArrayList<String> getAllOrganizationNames() throws OperationException;

    /**
     * Retrieves the identifier of an affiliated organization from its name.
     *
     * @param name the name of the organization to look up
     * @return the identifier of the organization
     * @throws OperationException if the identifier cannot be retrieved
     */
    int getOrganizationIdByName(String name) throws OperationException;

    /**
     * Retrieves the name of the organization linked to a supervisor.
     *
     * @param supervisorName the name of the supervisor to look up
     * @return the organization name if it exists, empty otherwise
     * @throws OperationException if the organization name cannot be retrieved
     */
    Optional<String> getOrganizationBySupervisorName(String supervisorName) throws OperationException;

    /**
     * Indicates whether an affiliated organization is inactive.
     *
     * @param organizationName the name of the organization to verify
     * @return true if the organization is inactive, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isOrganizationInactive(String organizationName) throws OperationException;

    /**
     * Retrieves the active organization names that start with the given prefix.
     *
     * @param prefix the prefix used to filter active organization names
     * @return the list of matching organization names, empty if there are none
     * @throws OperationException if the organization names cannot be retrieved
     */
    ArrayList<String> searchActiveOrganizationsByNamePrefix(String prefix) throws OperationException;

    /**
     * Retrieves the affiliated organization identified by the given name.
     *
     * @param organizationName the name of the organization to retrieve
     * @return the organization if it exists, empty otherwise
     * @throws OperationException if the organization cannot be retrieved
     */
    Optional<AffiliatedOrganization> getOrganizationByName(String organizationName) throws OperationException;

    /**
     * Retrieves the names of the projects that belong to an organization.
     *
     * @param organizationName the name of the organization to query
     * @return the list of project names, empty if there are none
     * @throws OperationException if the project names cannot be retrieved
     */
    ArrayList<String> getProjectsByOrganization(String organizationName) throws OperationException;

    /**
     * Indicates whether an affiliated organization has active projects.
     *
     * @param organizationName the name of the organization to verify
     * @return true if the organization has active projects, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasActiveProjects(String organizationName) throws OperationException;

    /**
     * Retrieves the complete projects that belong to an organization.
     *
     * @param organizationName the name of the organization to query
     * @return the list of projects, empty if there are none
     * @throws OperationException if the projects cannot be retrieved
     */
    ArrayList<Project> getCompleteProjectsByOrganization(String organizationName) throws OperationException;
}