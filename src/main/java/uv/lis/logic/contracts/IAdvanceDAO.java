package uv.lis.logic.contracts;

import java.util.ArrayList;

import uv.lis.logic.dto.Advance;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for the advances of a project.
 */
public interface IAdvanceDAO {

    /**
     * Registers an advance for a project report in the system.
     *
     * @param advance the advance data to register
     * @return true if the advance was registered, false otherwise
     * @throws OperationException if the advance cannot be registered
     */
    boolean registerAdvance(Advance advance) throws OperationException;

    /**
     * Retrieves the advances associated with a project.
     *
     * @param projectId the identifier of the project whose advances are retrieved
     * @return the list of advances for the project, empty if there are none
     * @throws OperationException if the advances cannot be retrieved
     */
    ArrayList<Advance> getAdvancesByProject(int projectId) throws OperationException;

    /**
     * Retrieves the latest accumulated hours reported for a project.
     *
     * @param projectId the identifier of the project to query
     * @return the latest accumulated hours, or zero if no advance exists
     * @throws OperationException if the accumulated hours cannot be retrieved
     */
    int getAccumulatedHoursByProject(int projectId) throws OperationException;

    /**
     * Indicates whether an advance is already registered for a report.
     *
     * @param reportId the identifier of the report to verify
     * @return true if an advance exists for the report, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean existsAdvanceForReport(int reportId) throws OperationException;
}