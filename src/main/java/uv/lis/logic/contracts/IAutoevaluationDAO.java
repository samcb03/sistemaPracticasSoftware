package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.Autoevaluation;


public interface IAutoevaluationDAO {
    boolean registerAutoevaluation(Autoevaluation autoevaluation) throws SQLException;

    
}
