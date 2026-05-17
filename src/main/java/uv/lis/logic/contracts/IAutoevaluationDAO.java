package uv.lis.logic.contracts;

import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;

public interface IAutoevaluationDAO {
    boolean registerAutoevaluation(Autoevaluation autoevaluation) throws OperationException;

    boolean existsByStudent(String idStudent) throws OperationException;

    Autoevaluation getAutoevaluationData(String studentId) throws OperationException;
}
