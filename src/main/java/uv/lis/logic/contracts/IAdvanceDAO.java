package uv.lis.logic.contracts;

import java.util.ArrayList;
import uv.lis.logic.dto.Advance;
import uv.lis.logic.exceptions.OperationException;

public interface IAdvanceDAO {
    boolean registerAdvance(Advance advance) throws OperationException;
    
    ArrayList<Advance> getAdvancesByProject(int projectId) throws OperationException;

    int getAccumulatedHoursByProject(int projectId) throws OperationException;

    boolean existsAdvanceForReport(int reportId) throws OperationException;
}
