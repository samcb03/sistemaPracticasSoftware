package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.ProjectSupervisor;


public interface IProjectSupervisorDAO {
    ProjectSupervisor getProjectSupervisorById (int idProject) throws SQLException;

    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException;

    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException;

    boolean inactiveProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException;
}
