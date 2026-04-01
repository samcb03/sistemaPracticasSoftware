package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProjectDAO;
import uv.lis.logic.dto.Project;


public class ProjectDAO implements IProjectDAO{
    private static final Logger logger = Logger.getLogger(ProjectDAO.class.getName());

    @Override
    public List<Project> getProjects() {
        
        List<Project> projects = new ArrayList<>(); 
        String projectQuery = "SELECT * FROM Proyecto;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
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
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return projects;
    }

    @Override
    public List<Project> getProjectsById(int idProject) {
        
        List<Project> projects = new ArrayList<>();
        String projectQuery = "SELECT * FROM Proyecto WHERE idProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();

            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return projects;
    }

    @Override
    public boolean registerProject(Project project) {
        boolean isRegistered = false;

        String projectQuery = "INSERT INTO Proyecto(idProyecto," +
                              "nombre, descripcion, cupo, metodologiaProyecto, objetivoProyecto)" +
                              "VALUES(?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)){
            
            preparedStatement.setInt(1, project.getId());
            preparedStatement.setString(2, project.getName());
            preparedStatement.setString(3, project.getDescription());
            preparedStatement.setInt(4, project.getCapacity());
            preparedStatement.setString(5, project.getMethodology());
            preparedStatement.setString(6, project.getObjective());
            
            if (preparedStatement.executeUpdate() > 0){
                isRegistered = true;
                
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyProjectById(Project project) {
        boolean isModified = false;
        String projectQuery = "UPDATE Proyecto"+ 
                              "SET nombre = ?, descripcion = ?, objetivoProyecto = ?" + 
                              "WHERE idProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)){
            
            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setString(3, project.getObjective());
            preparedStatement.setInt(4, project.getId());
            
            if (preparedStatement.executeUpdate() > 0){
                isModified = true;

            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isModified;
    }

    @Override
    public int inactivateProject(Project project) {
        // TODO inactivateProject logic
        throw new UnsupportedOperationException("Unimplemented method 'inactivateProject'");
    }

}
