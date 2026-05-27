package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public interface IProjectSupervisorDAO {
    Optional<ProjectSupervisor> getProjectSupervisorById (int idProject) throws OperationException;

    boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException;

    ArrayList<String> getAllSupervisorNames() throws OperationException;

    ArrayList<String> searchProjectSupervisorName(String prefix) throws OperationException;

    Optional<ProjectSupervisor> getProjectSupervisorByName(String supervisorName) throws OperationException;

    ArrayList<String> getProjectsBySupervisorName(String supervisorName) throws OperationException;

    int getSupervisorIdByName(String supervisorName) throws OperationException;

    ArrayList<String> getSupervisorsByOrganizationId(int organizationId) throws OperationException;

    boolean isSupervisorInactive(String name) throws OperationException;

    boolean inactivateProjectSupervisor(String projectSupervisorName) throws OperationException;

    boolean hasProjectsActives(String supervisorName) throws OperationException;
}
