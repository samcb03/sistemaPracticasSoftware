package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class ProjectDAO implements IProjectDAO{
    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ProjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public ArrayList<Project> getAllProjects() throws OperationException {
        ArrayList<Project> projects = new ArrayList<>(); 
        String projectQuery = "SELECT p.idProyecto, p.nombre, p.metodologiaProyecto, p.cupo, "
                              + "p.objetivo, p.descripcion, p.idOrganizacionVinculada, p.estado, "
                              + "o.nombreOV AS nombreOrganizacion "
                              + "FROM Proyecto p "
                              + "INNER JOIN OrganizacionVinculada o "
                              + "ON p.idOrganizacionVinculada = o.idOrganizacionVinculada";

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
                project.setAffiliatedOrganizationName(resultSet.getString("nombreOrganizacion"));
                project.setActive(resultSet.getBoolean("estado"));
                
                projects.add(project);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener los proyectos", e);
        }
        return projects;
    }

    @Override
    public Optional<Project> getProjectByName(String projectName) throws OperationException {
        Optional<Project> validateProjectName = Optional.empty();
        String projectQuery = "SELECT * FROM Proyecto WHERE nombre = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, projectName);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Project project = new Project();
                    project.setId(resultSet.getInt("idProyecto"));
                    project.setName(resultSet.getString("nombre"));
                    project.setDescription(resultSet.getString("descripcion"));
                    project.setCapacity(resultSet.getInt("cupo")); 
                    project.setMethodology(resultSet.getString("metodologiaProyecto"));
                    project.setObjective(resultSet.getString("objetivo"));
                    project.setIdAffiliatedOrganization(resultSet.getInt("idOrganizacionVinculada"));
                    validateProjectName = Optional.of(project);
                } else {
                    LOGGER.log(Level.INFO, "No se encontró el proyecto con nombre {0}.", projectName);
                    throw new OperationException("No se encontró el proyecto", null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al consultar proyecto", e);
            throw new OperationException("Error al consultar el proyecto", e);
        }
        return validateProjectName;
    }

    @Override
    public boolean registerProject(Project project) throws OperationException {
        boolean isRegistered = false;

        String projectQuery = "INSERT INTO Proyecto(nombre, "  
                            + "descripcion, cupo, metodologiaProyecto, objetivo, estado, idOrganizacionVinculada," 
                            + "idResponsableProyecto) VALUES(?, ?, ?, ?, ?, ?, ?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            preparedStatement.setString(1, project.getName());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setInt(3, project.getCapacity());
            preparedStatement.setString(4, project.getMethodology());
            preparedStatement.setString(5, project.getObjective());
            preparedStatement.setBoolean(6, project.isActive());
            preparedStatement.setInt(7, project.getIdAffiliatedOrganization());
            preparedStatement.setInt(8, project.getIdSupervisor());
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
                throw new OperationException("No se pudo modificar el proyecto", null);
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
        String projectQuery = "UPDATE Proyecto SET estado = 0 WHERE idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setInt(1, project.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                project.setActive(false);
            } else {
                LOGGER.log(Level.WARNING, "No se pudo inactivar el proyecto con ID {0}.", project.getId());
                throw new OperationException("No se pudo inactivar el proyecto", null);
            }

        } catch (SQLException e) {
               LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
               throw new OperationException("No se pudo inactivar el proyecto. Intentelo más tarde", e);
        }

        return isInactive;
    }

    @Override
    public ArrayList<String> getAllProjectNames() throws OperationException{
        String projectQuery = "SELECT nombre FROM Proyecto";
        ArrayList<String> projectNames = new ArrayList<>();

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        projectNames.add(resultSet.getString("nombre"));
                    } 
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al conseguir el nombre del proyecto", e);
        }

        return projectNames;
    }

   @Override
    public Optional<String> getProjectBySupervisorName(String supervisorName) throws OperationException {
        Optional<String> project = Optional.empty();
        
        String supervisorQuery = "SELECT p.nombre FROM Proyecto p"  
                                + " JOIN ResponsableProyecto rp ON p.idResponsableProyecto = rp.idResponsableProyecto"
                                + " WHERE rp.nombre = ?";

            try(Connection databaseConnection = connectionManager.getConnection();
                    PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

                        preparedStatement.setString(1,supervisorName);

                        try(ResultSet resultSet = preparedStatement.executeQuery()) {
                            if(resultSet.next()) {
                               String projectName = resultSet.getString("nombre");
                               project = Optional.of(projectName);
                            }
                        }
                    } catch(SQLException e) {
                        LOGGER.log(Level.SEVERE,"Error de conexión a la base de datos", e);
                        throw new OperationException("Error al cargar el nombre de la organización", e);
                    }
            return project;
        }

    @Override
    public ArrayList<String> getProjectNamesByOrganizationId(int organizationId) throws OperationException {
        ArrayList<String> projectNames = new ArrayList<>();
        String query = "SELECT nombre FROM Proyecto "
                    + "WHERE idOrganizacionVinculada = ? "
                    + "AND estado = 1";
 
    try (Connection databaseConnection = connectionManager.getConnection();
         PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
 
        preparedStatement.setInt(1, organizationId);
 
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                projectNames.add(resultSet.getString("nombre"));
            }
        }
 
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error al obtener proyectos por organización", e);
        throw new OperationException("Error al obtener los proyectos de la organización.", e);
    }
 
    return projectNames;
    }

    @Override
    public Optional<Project> getProjectByStudentId(String studentId) throws OperationException {
        Optional<Project> projectOptional = Optional.empty();
        String projectQuery = "SELECT p.idProyecto, p.nombre FROM Proyecto p"
                     + " JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto"
                     + " WHERE sp.matricula = ? "
                     + " AND sp.estatus = 2";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Project project = new Project();
                    project.setId(resultSet.getInt("idProyecto")); 
                    project.setName(resultSet.getString("nombre"));
                    projectOptional = Optional.of(project);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener el proyecto del alumno con matricula: " + studentId, e);
            throw new OperationException("Error al obtener el proyecto del alumno.", e);
        }
        return projectOptional;
    }

}
