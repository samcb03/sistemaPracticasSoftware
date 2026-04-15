package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public interface IProjectDAO {
    List<Project> getProjects() throws SQLException, OperationException;

    Project getProjectById(int idProject) throws SQLException, OperationException;

    boolean registerProject(Project project) throws SQLException, OperationException;

    boolean modifyProject(Project project) throws SQLException, OperationException;

    boolean inactivateProject(Project project) throws SQLException, OperationException;
}
