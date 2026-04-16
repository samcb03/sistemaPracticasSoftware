package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;


public interface IAutoevaluationDAO {
    boolean registerAutoevaluation(Autoevaluation autoevaluation) throws SQLException, OperationException;

    boolean existsByStudent(String idStudent) throws OperationException;
    
}
