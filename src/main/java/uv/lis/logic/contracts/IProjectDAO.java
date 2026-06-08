package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public interface IProjectDAO {
    List<Project> getAllProjects() throws OperationException;

    Optional<Project> getProjectByName(String projectName) throws OperationException;

    boolean registerProject(Project project) throws OperationException;

    boolean modifyProject(Project project) throws OperationException;

    boolean inactivateProject(Project project) throws OperationException;

    ArrayList<String> getAllProjectNames() throws OperationException;

    Optional<String> getProjectBySupervisorName(String supervisorName) throws OperationException;

    Optional<Project> getProjectByStudentId(String studentId) throws OperationException;

    ArrayList<String> getProjectNamesByOrganizationId(int organizationId) throws OperationException;

    ArrayList<Project> getAllProjectsWithCapacity() throws OperationException;
}
