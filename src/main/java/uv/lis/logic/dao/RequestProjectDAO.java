package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IRequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public class RequestProjectDAO implements IRequestProjectDAO {
    private static final int NO_RESULTS = 0;
    private static final int MAX_REQUESTS = 3;
    private static final int STATUS_REQUESTED = 1;
    private static final int STATUS_ASSIGNED = 2;
    private static final Logger LOGGER = Logger.getLogger(RequestProjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public RequestProjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public int getActiveRequestCountByStudentId(String idStudent) throws OperationException {
        int count = 0;
        String query = "SELECT COUNT(*) as total FROM Solicita_Proyecto WHERE matricula = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar solicitudes del practicante", e);
            throw new OperationException("Error al contar solicitudes del practicante",e);
        }

        return count;
    }
 
    @Override
    public List<Project> getAvailableProjects() throws OperationException{
        List<Project> projects = new ArrayList<>();
        String query = "SELECT p.*, "
            + "(p.cupo - COALESCE(COUNT(CASE WHEN sp.estatus = 2 THEN 1 END), 0)) as cupoDisponible "
            + "FROM Proyecto p "
            + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
            + "WHERE p.estado IS NULL OR p.estado = 1 "
            + "GROUP BY p.idProyecto HAVING cupoDisponible > 0;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = new Project();
                    project.setId(resultSet.getInt("idProyecto"));
                    project.setName(resultSet.getString("nombre"));
                    project.setDescription(resultSet.getString("descripcion"));
                    project.setCapacity(resultSet.getInt("cupo"));
                    project.setMethodology(resultSet.getString("metodologiaProyecto"));
                    project.setObjective(resultSet.getString("objetivo"));
                    
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos disponibles", e);
            throw new OperationException("Error al obtener proyectos disponibles", e);
        }

        return projects;
    }   

    @Override
    public boolean hasAlreadyRequested(String idStudent, int idProject) throws OperationException {
        boolean hasRequested = false;
        String query = "SELECT COUNT(*) as total FROM Solicita_Proyecto WHERE matricula = ? AND idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idProject);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasRequested = resultSet.getInt("total") > NO_RESULTS;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar solicitud existente", e);
            throw new OperationException("Error al verificar solicitud", e);
        }

        return hasRequested;
    }

    @Override
    public boolean hasAvailableCapacity(int idProject) throws OperationException {
        boolean hasCapacity = false;
        String query = "SELECT p.cupo, COUNT(sp.matricula) as solicitudes FROM Proyecto p "
            + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
            + "AND sp.estatus = ? "
            + "WHERE p.idProyecto = ? "
            + "GROUP BY p.idProyecto, p.cupo;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, STATUS_ASSIGNED); 
            preparedStatement.setInt(2, idProject);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int capacity = resultSet.getInt("cupo");
                    int requests = resultSet.getInt("solicitudes");
                    hasCapacity = requests < capacity;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar cupo del proyecto", e);
            throw new OperationException("No hay capacidad", e);
        }
        
        return hasCapacity;
    }

    @Override
    public boolean requestProject(String idStudent, int idProject) throws OperationException {
        boolean isRegistered = false;
        String query = "INSERT INTO Solicita_Proyecto (idProyecto, matricula, estatus) VALUES (?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, idProject);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, STATUS_REQUESTED);

            if (preparedStatement.executeUpdate() > NO_RESULTS) {
                isRegistered = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar solicitud de proyecto", e);
            throw new OperationException("Error al registrar solicitud", e);
        }
        return isRegistered;
    }

    @Override
    public Optional<String> validateProjectRequest(String idStudent, int idProject) throws OperationException {
        Optional<String> validationError = Optional.empty();
        try {
            if (getActiveRequestCountByStudentId(idStudent) >= MAX_REQUESTS) {
                validationError = Optional.of("Ya tienes " + MAX_REQUESTS + " solicitudes activas");
            } else if (hasAlreadyRequested(idStudent, idProject)) {
                validationError = Optional.of("Ya solicitaste este proyecto anteriormente");
            } else if (!hasAvailableCapacity(idProject)) {
                validationError = Optional.of("Este proyecto ya no tiene cupo disponible");
            }

        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error en validación de solicitud", e);
            throw new OperationException("Error al validar solicitud", e);
        }

        return validationError;
    }

    @Override
    public boolean assignStudentToProject(String idStudent, int idProject) throws OperationException {
        boolean isAssigned = false;
        String query = "UPDATE Solicita_Proyecto SET estatus = ? WHERE matricula = ? AND idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, STATUS_ASSIGNED);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, idProject);

            if (preparedStatement.executeUpdate() > NO_RESULTS) {
                isAssigned = true;
                LOGGER.log(Level.INFO, "Practicante {0} asignado al proyecto {1} exitosamente", 
                    new Object[]{idStudent, idProject});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al asignar practicante al proyecto", e);
            throw new OperationException("Error al asignar practicante al proyecto", e);
        }

        return isAssigned;
    } 
    
}
