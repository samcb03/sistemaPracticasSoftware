package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for school periods.
 */
public interface ISchoolPeriodDAO {

    /**
     * Retrieves the names of all the registered school periods.
     *
     * @return the list of school period names, empty if there are none
     * @throws OperationException if the school period names cannot be retrieved
     */
    ArrayList<String> getAllSchoolPeriodsNames() throws OperationException;

    /**
     * Retrieves the school period a student is currently enrolled in.
     *
     * @param studentId the enrollment of the student to look up
     * @return the enrolled school period with its date range if it exists, empty otherwise
     * @throws OperationException if the school period cannot be retrieved
     */
    Optional<SchoolPeriod> getSchoolPeriodByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the identifier of a school period from its name.
     *
     * @param periodName the name of the school period to look up
     * @return the school period identifier if it exists, empty otherwise
     * @throws OperationException if the identifier cannot be retrieved
     */
    Optional<String> getSchoolPeriodIdByName(String periodName) throws OperationException;

    /**
     * Registers a new school period in the system.
     *
     * @param schoolPeriod the school period data to register
     * @return true if the school period was registered, false otherwise
     * @throws OperationException if the school period cannot be registered
     */
    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    /**
     * Updates the data of an existing school period.
     *
     * @param schoolPeriod the school period data to update
     * @return true if the school period was updated, false otherwise
     * @throws OperationException if the school period cannot be updated
     */
    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    /**
     * Indicates whether a school period exists.
     *
     * @param idPeriod the identifier of the school period to verify
     * @return true if the school period exists, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean existsSchoolPeriod(int idPeriod) throws OperationException;
}