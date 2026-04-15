package uv.lis.logic.contracts;

import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public interface IProjectRequestDAO {
    int getActiveRequestCountByStudentId(String id) throws SQLException, OperationException;
    
    boolean hasAvailableCapacity(int idProject) throws SQLException;

    boolean requestProject(String id, int idProject) throws SQLException;

    List<Project> getRequestedProjectsByStudentId(String id) throws SQLException, OperationException;

    List<Project> getAvailableProjects() throws SQLException, OperationException;

    boolean hasAlreadyRequested(String id, int idProject) throws SQLException, OperationException;

    boolean validateProjectRequest(String idStudent, int idProject) throws SQLException;

    boolean assignStudentToProject(String idStudent, int idProject) throws SQLException;
}

