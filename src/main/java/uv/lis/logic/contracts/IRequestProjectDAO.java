package uv.lis.logic.contracts;


import java.util.List;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public interface IRequestProjectDAO {
    int getActiveRequestCountByStudentId(String id) throws OperationException;
    
    boolean hasAvailableCapacity(int idProject) throws OperationException;

    boolean requestProject(String id, int idProject) throws OperationException;

    List<Project> getRequestedProjectsByStudentId(String id) throws OperationException;

    List<Project> getAvailableProjects() throws OperationException;

    boolean hasAlreadyRequested(String id, int idProject) throws OperationException;

    boolean validateProjectRequest(String idStudent, int idProject) throws OperationException;

    boolean assignStudentToProject(String idStudent, int idProject) throws OperationException;
}

