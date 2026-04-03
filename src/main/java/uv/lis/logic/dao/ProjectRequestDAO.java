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
import uv.lis.logic.contracts.IProjectRequestDAO;
import uv.lis.logic.dto.Project;


public class ProjectRequestDAO implements IProjectRequestDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final int MAX_REQUESTS = 3;
    private static final Logger LOGGER = Logger.getLogger(ProjectRequestDAO.class.getName());

    @Override
    public int getActiveRequestCountByStudentId(String idStudent) throws SQLException {
        int count = 0;
        String query = "SELECT COUNT(*) as total FROM Solicita_Proyecto WHERE idStudent = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar solicitudes del practicante", e);
            throw e;
        }

        return count;
    }

    @Override
    public List<Project> getAvailableProjects() {
         List<Project> projects = new ArrayList<>();
        String query = "SELECT p.*, " 
            + "(p.cupo - COALESCE(COUNT(sp.matricula), 0)) as cupoDisponible FROM Proyecto p " 
            + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto WHERE p.estado IS NULL OR p.estado = 0 " 
            + "GROUP BY p.idProyecto HAVING cupoDisponible > 0;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = new Project();
                    project.setId(resultSet.getInt("idProyecto"));
                    project.setName(resultSet.getString("nombre"));
                    project.setDescription(resultSet.getString("descripcion"));
                    project.setCapacity(resultSet.getInt("cupo"));
                    project.setMethodology(resultSet.getString("metodologiaProyecto"));
                    project.setObjective(resultSet.getString("objetivoProyecto"));
                    
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos disponibles", e);
        }

        return projects;
    }

    @Override
    public List<Project> getRequestedProjectsByStudentId(String idStudent) {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT p.* FROM Proyecto p INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto " 
            + "WHERE sp.matricula = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = new Project();
                    project.setId(resultSet.getInt("idProyecto"));
                    project.setName(resultSet.getString("nombre"));
                    project.setDescription(resultSet.getString("descripcion"));
                    project.setCapacity(resultSet.getInt("cupo"));
                    project.setMethodology(resultSet.getString("metodologiaProyecto"));
                    project.setObjective(resultSet.getString("objetivoProyecto"));
                    
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos solicitados", e);
        }

        return projects;
    }

    @Override
    public boolean hasAlreadyRequested(String idStudent, int idProject) throws SQLException {
        boolean hasRequested = false;
        String query = "SELECT COUNT(*) as total FROM Solicita_Proyecto WHERE matricula = ? AND idProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idProject);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasRequested = resultSet.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar solicitud existente", e);
            throw e;
        }

        return hasRequested;
    }

    @Override
    public boolean hasAvailableCapacity(int idProject) throws SQLException {
        boolean hasCapacity = false;
        String query = "SELECT p.cupo, COUNT(sp.idStudent) as solicitudes FROM Proyecto p "
            + "LEFT JOIN Solicita_Proyecto sp ON p.idProject = sp.idProject WHERE p.idProject = ? "
            + "GROUP BY p.idProject, p.cupo;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, idProject);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int capacity = resultSet.getInt("cupo");
                    int requests = resultSet.getInt("solicitudes");
                    hasCapacity = requests < capacity;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar cupo del proyecto", e);
            throw e;
        }
        
        return hasCapacity;
    }

    @Override
    public boolean requestProject(String idStudent, int idProject) {
        boolean isRegistered = false;
        
        if (validateProjectRequest(idStudent, idProject)) {
            String query = "INSERT INTO Solicita_Proyecto (idProject, idStudent, estatus) VALUES (?, ?, FALSE);";

            try (Connection databaseConnection = MySQLConnectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
                
                preparedStatement.setInt(1, idProject);
                preparedStatement.setString(2, idStudent);

                if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                    isRegistered = true;
                    LOGGER.log(Level.INFO, "Solicitud de proyecto {0} por practicante {1} registrada exitosamente", 
                        new Object[]{idProject, idStudent});
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al registrar solicitud de proyecto", e);
            }
        }

        return isRegistered;
    }

    @Override
    public boolean validateProjectRequest(String idStudent, int idProject) {
        boolean isValid = true;
        try {
            if (getActiveRequestCountByStudentId(idStudent) >= MAX_REQUESTS) {
                LOGGER.log(Level.WARNING, "El practicante {0} ya tiene {1} solicitudes activas", 
                    new Object[]{idStudent, MAX_REQUESTS});
                isValid = false;
            }

            if (hasAlreadyRequested(idStudent, idProject)) {
                LOGGER.log(Level.WARNING, "El practicante {0} ya solicitó el proyecto {1}", 
                    new Object[]{idStudent, idProject});
                isValid = false;
            }

            if (!hasAvailableCapacity(idProject)) {
                LOGGER.log(Level.WARNING, "El proyecto {0} no tiene cupo disponible", idProject);
                isValid = false;
            }
  
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en validación de solicitud", e);
        } 
        return isValid;
    }

    @Override
    public boolean assignStudentToProject(String idStudent, int idProject) {
        boolean isAssigned = false;
        String query = "UPDATE Solicita_Proyecto SET estatus = TRUE WHERE idStudent = ? AND idProject = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idProject);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isAssigned = true;
                LOGGER.log(Level.INFO, "Practicante {0} asignado al proyecto {1} exitosamente", 
                    new Object[]{idStudent, idProject});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al asignar practicante al proyecto", e);
        }

        return isAssigned;
    } 
    
}
