package uv.lis.logic.contracts;

import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public interface IRequestProjectDAO {
    int getActiveRequestCountByStudentId(String id) throws OperationException;
    
    boolean hasAvailableCapacity(int idProject) throws OperationException;

    boolean requestProject(String id, int idProject) throws OperationException;

    List<Project> getAvailableProjects() throws OperationException;

    boolean hasAlreadyRequested(String id, int idProject) throws OperationException;

    Optional<String> validateProjectRequest(String idStudent, int idProject) throws OperationException;

    boolean assignStudentToProject(String idStudent, int idProject) throws OperationException;

    List<String> getApplicantsByProjectId(int idProject) throws OperationException;

    String getProjectAssignedToStudent(String idStudent) throws OperationException;

    void unassignStudentFromProject(String idStudent) throws OperationException;
}

