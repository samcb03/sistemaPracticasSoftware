package uv.lis.logic.contracts;

import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Project;


public interface IProjectRequestDAO {
    int getActiveRequestCountByStudentId(String id) throws SQLException;
    
    boolean hasAvailableCapacity(int idProject) throws SQLException;

    boolean requestProject(String id, int idProject) throws SQLException;

    List<Project> getRequestedProjectsByStudentId(String id) throws SQLException;

    List<Project> getAvailableProjects() throws SQLException;

    boolean hasAlreadyRequested(String id, int idProject) throws SQLException;

    boolean validateProjectRequest(String idStudent, int idProject) throws SQLException;
}

