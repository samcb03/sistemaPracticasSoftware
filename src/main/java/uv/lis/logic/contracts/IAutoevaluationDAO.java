package uv.lis.logic.contracts;

import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for student autoevaluations.
 */
public interface IAutoevaluationDAO {

    /**
     * Registers a new autoevaluation in the system.
     *
     * @param autoevaluation the autoevaluation data to register
     * @return true if the autoevaluation was registered, false otherwise
     * @throws OperationException if the autoevaluation cannot be registered
     */
    boolean registerAutoevaluation(Autoevaluation autoevaluation) throws OperationException;

    /**
     * Indicates whether a student already has a registered autoevaluation.
     *
     * @param idStudent the identifier of the student to verify
     * @return true if the student has an autoevaluation, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean existsByStudent(String idStudent) throws OperationException;

    /**
     * Retrieves the autoevaluation data of a student.
     *
     * @param studentId the identifier of the student whose data is retrieved
     * @return the autoevaluation data of the student
     * @throws OperationException if the autoevaluation data cannot be retrieved
     */
    Autoevaluation getAutoevaluationData(String studentId) throws OperationException;
}