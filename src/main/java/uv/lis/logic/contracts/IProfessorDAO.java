package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for professors.
 */
public interface IProfessorDAO {

    /**
     * Retrieves the personnel number of a professor from their name.
     *
     * @param firstName the first name of the professor to look up
     * @param lastName the last name of the professor to look up
     * @return the professor personnel number if it exists, empty otherwise
     * @throws OperationException if the personnel number cannot be retrieved
     */
    Optional<String> getProfessorPersonnelNumberByName(String firstName, String lastName) throws OperationException;

    /**
     * Registers a new professor in the system.
     *
     * @param professor the professor data to register
     * @return true if the professor was registered, false otherwise
     * @throws OperationException if the professor cannot be registered
     */
    boolean registerProfessor(Professor professor) throws OperationException;

    /**
     * Updates the data of an existing professor.
     *
     * @param professor the professor data to update
     * @return true if the professor was updated, false otherwise
     * @throws OperationException if the professor cannot be updated
     */
    boolean modifyProfessor(Professor professor) throws OperationException;

    /**
     * Retrieves the active professors mapped by personnel number and full name.
     *
     * @return the map of active professors, empty if there are none
     * @throws OperationException if the professors cannot be retrieved
     */
    LinkedHashMap<String, String> getAllActiveProfessorsMap() throws OperationException;

    /**
     * Retrieves the professor identified by the given id.
     *
     * @param id the identifier of the professor to retrieve
     * @return the professor if it exists, empty otherwise
     * @throws OperationException if the professor cannot be retrieved
     */
    Optional<Professor> getProfessorById(int id) throws OperationException;

    /**
     * Retrieves the user identifier associated with a professor personnel number.
     *
     * @param personnelNumber the personnel number of the professor to look up
     * @return the associated user identifier
     * @throws OperationException if the user identifier cannot be retrieved
     */
    int getIdUserByProfessorPersonnelNumber(String personnelNumber) throws OperationException;

    /**
     * Retrieves the personnel numbers that start with the given prefix.
     *
     * @param prefix the prefix used to filter professor personnel numbers
     * @return the list of matching personnel numbers, empty if there are none
     * @throws OperationException if the personnel numbers cannot be retrieved
     */
    ArrayList<String> searchProfessorPersonalNumbers(String prefix) throws OperationException;

    /**
     * Indicates whether a professor is inactive.
     *
     * @param personnelNumber the personnel number of the professor to verify
     * @return true if the professor is inactive, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isProfessorInactive(String personnelNumber) throws OperationException;

    /**
     * Marks a professor as inactive.
     *
     * @param personalNumber the personnel number of the professor to inactivate
     * @return true if the professor was inactivated, false otherwise
     * @throws OperationException if the professor cannot be inactivated
     */
    boolean inactivateProfessor(String personalNumber) throws OperationException;

    /**
     * Indicates whether a professor has a subject assigned.
     *
     * @param personnelNumber the personnel number of the professor to verify
     * @return true if the professor has a subject assigned, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasSubjectAssigned(String personnelNumber) throws OperationException;

    /**
     * Retrieves the subjects assigned to a professor.
     *
     * @param personnelNumber the personnel number of the professor to query
     * @return the list of subjects for the professor, empty if there are none
     * @throws OperationException if the subjects cannot be retrieved
     */
    ArrayList<String> getSubjectsByProfessor(String personnelNumber) throws OperationException;

    /**
     * Indicates whether another coordinator is active besides the given professor.
     *
     * @param personnelNumber the personnel number of the professor to exclude
     * @return true if another coordinator is active, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isAnotherCoordinatorActive(String personnelNumber) throws OperationException;
}