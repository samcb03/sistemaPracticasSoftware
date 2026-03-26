package cuarto.construccion.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cuarto.construccion.dataaccess.MySQLConnectionManager;
import cuarto.construccion.logic.contracts.IProjectDAO;
import cuarto.construccion.logic.dto.Project;


public class ProjectDAO implements IProjectDAO{

    @Override
    public List<Project> getProjects() {
        
        List<Project> projects = new ArrayList<>(); 
        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String projectQuery = "SELECT * FROM Proyecto;";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Project project = new Project();
                project.setIdProject(resultSet.getInt("idProyecto"));
                project.setProjectName(resultSet.getString("nombre"));
                project.setProjectMethodology(resultSet.getString("metodologiaProyecto"));
                project.setCapacity(resultSet.getInt("Cupo"));
                project.setProjectObjective(resultSet.getString("objetivoProyecto"));
                project.setProjectDescription(resultSet.getString("descripcion"));
                
                projects.add(project);
            }
            databaseConnection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projects;
    }

    @Override
    public List<Project> getProjectsById(int idProject) {
        
        List<Project> projects = new ArrayList<>();
        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String projectQuery = "SELECT * FROM Proyecto WHERE idProyecto = ?;";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            preparedStatement.setInt(1, idProject);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Project project = new Project();
                project.setIdProject(resultSet.getInt("idProyecto"));
                project.setProjectName(resultSet.getString("nombre"));
                project.setProjectMethodology(resultSet.getString("metodologiaProyecto"));
                project.setCapacity(resultSet.getInt("Cupo"));
                project.setProjectObjective(resultSet.getString("objetivoProyecto"));
                project.setProjectDescription(resultSet.getString("descripcion"));
                
                projects.add(project);
            }
            databaseConnection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return projects;
    }

    @Override
    public int registerProject(Project project) {

        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String projectQuery = "INSERT INTO Proyecto(idProyecto, nombre, descripcion, cupo, metodologiaProyecto, objetivoProyecto) VALUES(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            
            preparedStatement.setInt(1, project.getIdProject());
            preparedStatement.setString(2, project.getProjectName());
            preparedStatement.setString(3, project.getProjectDescription());
            preparedStatement.setInt(4, project.getCapacity());
            preparedStatement.setString(5, project.getProjectMethodology());
            preparedStatement.setString(6, project.getProjectObjective());
            
            preparedStatement.executeUpdate();
            databaseConnection.close();
            return project.getIdProject();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean modifyProjectById(Project project) {

        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String projectQuery = "UPDATE Proyecto SET nombre = ?, descripcion = ?, objetivoProyecto = ? WHERE idProyecto = ?;";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            
            preparedStatement.setString(1, project.getProjectName());
            preparedStatement.setString(2, project.getProjectDescription());
            preparedStatement.setString(3, project.getProjectObjective());
            preparedStatement.setInt(4, project.getIdProject());
            
            preparedStatement.executeUpdate();
            databaseConnection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public int inactivateProject(Project project) {
        // TODO inactivateProject logic
        throw new UnsupportedOperationException("Unimplemented method 'inactivateProject'");
    }

}
