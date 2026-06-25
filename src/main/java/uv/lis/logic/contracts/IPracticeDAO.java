package uv.lis.logic.contracts;

import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for student internship practice records.
 */
public interface IPracticeDAO {

    /**
     * Retrieves the practice record associated with a student.
     *
     * @param idStudent the identifier of the student whose practice is retrieved
     * @return the practice record for the student
     * @throws OperationException if the practice cannot be retrieved or does not exist
     */
    Practice getPracticeByStudent(String idStudent) throws OperationException;

    /**
     * Registers a new practice record with the student's final grade.
     *
     * @param practice the practice data to register, including the student identifier and grade
     * @return true if the practice was registered successfully, false otherwise
     * @throws OperationException if the practice cannot be registered
     */
    boolean registerPractice(Practice practice) throws OperationException;

    /**
     * Indicates whether a practice record already exists for a student.
     *
     * @param idStudent the identifier of the student to verify
     * @return true if a practice record exists for the student, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean existsByStudent(String idStudent) throws OperationException;

    /**
     * Retrieves the final grade of a student's internship practice as a formatted string.
     *
     * @param idStudent the identifier of the student whose grade is retrieved
     * @return the final grade as a string for display purposes
     * @throws OperationException if the grade cannot be retrieved or does not exist
     */
    String getFinalGrade(String idStudent) throws OperationException;
}