package uv.lis.logic.contracts;


import java.util.List;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public interface IProjectDAO {
    List<Project> getProjects() throws OperationException;

    Project getProjectById(int idProject) throws OperationException;

    boolean registerProject(Project project) throws OperationException;

    boolean modifyProject(Project project) throws OperationException;

    boolean inactivateProject(Project project) throws OperationException;
}
