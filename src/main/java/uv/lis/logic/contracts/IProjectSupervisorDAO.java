package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;


public interface IProjectSupervisorDAO {
    ProjectSupervisor getProjectSupervisorById (int idProject) throws SQLException, OperationException;

    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException, OperationException;

    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException, OperationException;

    boolean inactivateProjectSupervisor(ProjectSupervisor projectSupervisor) throws SQLException, OperationException;
}
