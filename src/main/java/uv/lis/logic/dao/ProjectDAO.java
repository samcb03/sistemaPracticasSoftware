package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProjectDAO;
import uv.lis.logic.dto.Project;


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
                project.setId(resultSet.getInt("idProyecto"));
                project.setName(resultSet.getString("nombre"));
                project.setMethodology(resultSet.getString("metodologiaProyecto"));
                project.setCapacity(resultSet.getInt("Cupo"));
                project.setObjective(resultSet.getString("objetivoProyecto"));
                project.setDescription(resultSet.getString("descripcion"));
                
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
                project.setId(resultSet.getInt("idProyecto"));
                project.setName(resultSet.getString("nombre"));
                project.setMethodology(resultSet.getString("metodologiaProyecto"));
                project.setCapacity(resultSet.getInt("Cupo"));
                project.setObjective(resultSet.getString("objetivoProyecto"));
                project.setDescription(resultSet.getString("descripcion"));
                
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
            String projectQuery = "INSERT INTO Proyecto(idProyecto," +
            "nombre, descripcion, cupo, metodologiaProyecto, objetivoProyecto)" +
            "VALUES(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            
            preparedStatement.setInt(1, project.getId());
            preparedStatement.setString(2, project.getName());
            preparedStatement.setString(3, project.getDescription());
            preparedStatement.setInt(4, project.getCapacity());
            preparedStatement.setString(5, project.getMethodology());
            preparedStatement.setString(6, project.getObjective());
            
            preparedStatement.executeUpdate();
            databaseConnection.close();
            return project.getId();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean modifyProjectById(Project project) {

        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String projectQuery = "UPDATE Proyecto"+ 
            "SET nombre = ?, descripcion = ?, objetivoProyecto = ?" + 
            "WHERE idProyecto = ?;";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery);
            
            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setString(3, project.getObjective());
            preparedStatement.setInt(4, project.getId());
            
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
