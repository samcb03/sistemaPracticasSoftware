package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Project;


public interface IProjectDAO {
    List<Project> getProjects() throws SQLException;

    Project getProjectById(int idProject) throws SQLException;

    boolean registerProject(Project project) throws SQLException;

    boolean modifyProject(Project project) throws SQLException;

    boolean inactivateProject(Project project) throws SQLException;
}
