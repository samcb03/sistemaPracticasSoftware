package uv.lis.logic.contracts;

import uv.lis.logic.dto.ProjectSupervisor;

public interface IProjectSupervisorDAO {

    ProjectSupervisor getProjectSupervisorById (int idProject);

    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor);

    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor);

    boolean inactiveProjectSupervisor(ProjectSupervisor projectSupervisor);

}
