package uv.lis.logic.contracts;

import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

public interface IPracticeDAO {

    boolean registerPractice(Practice practice) throws OperationException;
    
    }
