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
import uv.lis.logic.contracts.IRequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;


public class RequestProjectDAO implements IRequestProjectDAO {
    private static final int NO_ROWS_AFFECTED = 0;
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
                    hasRequested = resultSet.getInt("total") > 0;
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
            + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto WHERE p.idProyecto = ? "
            + "GROUP BY p.idProyecto, p.cupo;";

        try (Connection databaseConnection = connectionManager.getConnection();
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

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar solicitud de proyecto", e);
            throw new OperationException("Error al registrar solicitud", e);
        }
        return isRegistered;
    }

    @Override
    public boolean validateProjectRequest(String idStudent, int idProject) throws OperationException {
        boolean isValid = true;
        try {
            if (getActiveRequestCountByStudentId(idStudent) >= MAX_REQUESTS) {
                LOGGER.log(Level.WARNING, "El practicante ya tiene {1} solicitudes activas", MAX_REQUESTS);
                isValid = false;
            }

            if (hasAlreadyRequested(idStudent, idProject)) {
                LOGGER.log(Level.WARNING, "El practicante ya solicitó este proyecto");
                isValid = false;
            }

            if (!hasAvailableCapacity(idProject)) {
                LOGGER.log(Level.WARNING, "El proyecto {0} no tiene cupo disponible", idProject);
                isValid = false;
            }
  
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error en validación de solicitud", e);
            throw new OperationException("Error al validar solicitud", e);
        } 
        return isValid;
    }

@Override
public boolean assignStudentToProject(String idStudent, int idProject) throws OperationException {
    boolean isAssigned = false;
    
    // Consulta para contar cuántos ya están asignados (estatus = 1)
    String queryCount = "SELECT (p.cupo - COUNT(sp.idSolicitud)) as disponibles " +
                        "FROM Proyecto p LEFT JOIN Solicita_Proyecto sp " +
                        "ON p.idProyecto = sp.idProyecto AND sp.estatus = 1 " +
                        "WHERE p.idProyecto = ? GROUP BY p.cupo;";

    String queryAssign = "UPDATE Solicita_Proyecto SET estatus = 1 WHERE matricula = ? AND idProyecto = ?;";
    String queryClean = "DELETE FROM Solicita_Proyecto WHERE matricula = ? AND estatus = 0;";

    try (Connection connection = connectionManager.getConnection()) {
        connection.setAutoCommit(false);

        try {
            // PASO 0: ¡VALIDACIÓN DE ÚLTIMO MOMENTO!
            try (PreparedStatement psCount = connection.prepareStatement(queryCount)) {
                psCount.setInt(1, idProject);
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next() && rs.getInt("disponibles") <= 0) {
                        throw new SQLException("CUPO_LLENO"); // Forzamos el error
                    }
                }
            }

            // PASO A: Asignar (estatus = 1)
            try (PreparedStatement psAssign = connection.prepareStatement(queryAssign)) {
                psAssign.setString(1, idStudent);
                psAssign.setInt(2, idProject);
                psAssign.executeUpdate();
            }

            // PASO B: Limpiar otras solicitudes del alumno
            try (PreparedStatement psClean = connection.prepareStatement(queryClean)) {
                psClean.setString(1, idStudent);
                psClean.executeUpdate();
            }

            connection.commit();
            isAssigned = true;

        } catch (SQLException e) {
            connection.rollback();
            if ("CUPO_LLENO".equals(e.getMessage())) {
                throw new OperationException("Lo sentimos, otro coordinador acaba de llenar el cupo de este proyecto.", e);
            }
            throw e;
        }
    } catch (SQLException e) {
        throw new OperationException("Error de base de datos", e);
    }
    return isAssigned;
}
    
    public int getAssignedCount(int projectId) throws OperationException {
    int count = 0;

    String query = "SELECT COUNT(*) AS Total " +
                   "FROM Solicita_Proyecto " +
                   "WHERE idProyecto = ? AND estatus = ?;";

    try (Connection databaseConnection = connectionManager.getConnection();
         PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

        preparedStatement.setInt(1, projectId);
        preparedStatement.setInt(2, STATUS_ASSIGNED);

        try(ResultSet resultSet = preparedStatement.executeQuery()) {
            if(resultSet.next()) {
                count = resultSet.getInt("Total");
            }
        }

    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error al obtener el recuento de asignaciones", e);
        throw new OperationException("Error al obtener el recuento de asignaciones", e);
    }

    return count;
    }

    @Override
    public List<String> getApplicantsByProjectId(int idProject) throws OperationException {
    List<String> applicants = new ArrayList<>();
    String query = "SELECT u.nombre, u.apellidos, a.matricula " +
                   "FROM Usuario u " +
                   "INNER JOIN Alumno a ON u.idUsuario = a.idUsuario " +
                   "INNER JOIN Solicita_Proyecto sp ON a.matricula = sp.matricula " +
                   "WHERE sp.idProyecto = ?";

    try (Connection databaseConnection = connectionManager.getConnection();
         PreparedStatement statement = databaseConnection.prepareStatement(query)) {
         
        statement.setInt(1, idProject);
        
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String fullName = resultSet.getString("nombre") + " " + 
                                  resultSet.getString("apellidos") + 
                                  " (" + resultSet.getString("matricula") + ")";
                applicants.add(fullName);
            }
        }
    } catch (SQLException e) {
        throw new OperationException("Error al obtener los solicitantes del proyecto", e);
    }

    return applicants;
}
    
}
