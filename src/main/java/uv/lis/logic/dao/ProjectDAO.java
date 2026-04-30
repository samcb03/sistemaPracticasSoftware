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
import uv.lis.logic.exceptions.OperationException;


public class ProjectDAO implements IProjectDAO{
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ProjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public List<Project> getProjects() throws OperationException {
        
        List<Project> projects = new ArrayList<>(); 
        String projectQuery = "SELECT * FROM Proyecto;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Project project = new Project();
                project.setId(resultSet.getInt("idProyecto"));
                project.setName(resultSet.getString("nombre"));
                project.setMethodology(resultSet.getString("metodologiaProyecto"));
                project.setCapacity(resultSet.getInt("cupo"));
                project.setObjective(resultSet.getString("objetivo"));
                project.setDescription(resultSet.getString("descripcion"));
                
                projects.add(project);
            }
            databaseConnection.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener los proyectos", e);
        }
        return projects;
    }

    @Override
    public Project getProjectById(int idProject) throws OperationException {
        Project project = null; 
        String projectQuery = "SELECT * FROM Proyecto WHERE idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, idProject);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    project = new Project();
                    project.setId(resultSet.getInt("idProyecto"));
                    project.setName(resultSet.getString("nombre"));
                    project.setDescription(resultSet.getString("descripcion"));
                    project.setCapacity(resultSet.getInt("cupo")); 
                    project.setMethodology(resultSet.getString("metodologiaProyecto"));
                    project.setObjective(resultSet.getString("objetivo"));
                } else {
                    LOGGER.log(Level.INFO, "No se encontró el proyecto con ID {0}.", idProject);
                    throw new OperationException("No se encontró el proyecto con ID: " + idProject, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al consultar proyecto", e);
            throw new OperationException("Error al consultar el proyecto", e);
        }
        return project;
    }

    @Override
    public boolean registerProject(Project project) throws OperationException {
        boolean isRegistered = false;

        String projectQuery = "INSERT INTO Proyecto(nombre, "  
        + "descripcion, cupo, metodologiaProyecto, objetivo,estado)" 
        + " VALUES(?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setInt(3, project.getCapacity());
            preparedStatement.setString(4, project.getMethodology());
            preparedStatement.setString(5, project.getObjective());
            preparedStatement.setBoolean(6, true);
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED){
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        int generatedId = resultSet.getInt(1);
                        project.setId(generatedId);
                    }
                }
                isRegistered = true;
                LOGGER.log(Level.INFO, "Proyecto con ID {0} registrado con éxito.", project.getId());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar el proyecto.");
                throw new OperationException("No se pudo registrar el proyecto. Intentelo mas tarde", 
                    null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo registrar el proyecto. Intentelo más tarde", e);

        }
        return isRegistered;
    }

    @Override
    public boolean modifyProject(Project project) throws OperationException {
        boolean isModified = false;

        String projectQuery = "UPDATE Proyecto " 
            + " SET nombre = ?, descripcion = ?, cupo = ?, "
            + " metodologiaProyecto = ?, objetivo = ?, estado = ? " 
            + " WHERE idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {
            
            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setInt(3, project.getCapacity());
            preparedStatement.setString(4, project.getMethodology());
            preparedStatement.setString(5, project.getObjective());
            preparedStatement.setBoolean(6, true); 
            preparedStatement.setInt(7, project.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Proyecto con ID {0} modificado con éxito.", project.getId());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar el proyecto con ID {0}.", project.getId());
                throw new OperationException("No se pudo modificar el proyecto con ID: " + project.getId(), null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo modificar el proyecto. Intentelo más tarde", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProject(Project project) throws OperationException {
        boolean isInactive = false;

        String query = "UPDATE Proyecto SET estado = 0 WHERE idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setInt(1, project.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo inactivar el proyecto con ID {0}.", project.getId());
                throw new OperationException("No se pudo inactivar el proyecto con ID: " + project.getId(), null);
            }

        } catch (SQLException e) {
               LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
               throw new OperationException("No se pudo inactivar el proyecto. Intentelo más tarde", e);
        }

        return isInactive;
    }

}
