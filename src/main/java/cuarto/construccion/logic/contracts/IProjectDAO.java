package cuarto.construccion.logic.contracts;


import java.util.List;

import cuarto.construccion.logic.dto.Project;


public interface IProjectDAO {

    List<Project> getProjects();

    List<Project> getProjectsById(int idProject);

    int registerProject(Project project);

    boolean modifyProjectById(Project project);

    int inactivateProject(Project project);
}
