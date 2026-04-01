package uv.lis.logic.contracts;


import java.util.List;

import uv.lis.logic.dto.Project;


public interface IProjectDAO {

    List<Project> getProjects();

    List<Project> getProjectsById(int idProject);

    boolean registerProject(Project project);

    boolean modifyProjectById(Project project);

    int inactivateProject(Project project);
}
