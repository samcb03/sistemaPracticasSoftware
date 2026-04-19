package uv.lis.logic.contracts;


import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;


public interface IProjectSupervisorDAO {
    ProjectSupervisor getProjectSupervisorById (int idProject) throws OperationException;

    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    boolean inactivateProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;
}
