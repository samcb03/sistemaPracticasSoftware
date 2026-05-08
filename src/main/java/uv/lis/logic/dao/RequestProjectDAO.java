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

    private static final String COLUMN_AVAILABLE = "disponibles";
    private static final String ERROR_ALREADY_ASSIGNED = "ALUMNO_YA_ASIGNADO";
    private static final String ERROR_FULL_CAPACITY = "CUPO_LLENO";
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
        String query = "SELECT p.cupo, COUNT(sp.matricula) AS solicitudes " +
                        "FROM Proyecto p " +
                        "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto " +
                        "AND sp.estatus = " + STATUS_ASSIGNED + " " +
                        "WHERE p.idProyecto = ? " +
                        "GROUP BY p.idProyecto, p.cupo";

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
            throw new OperationException("Error al verificar el cupo del proyecto", e);
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
            }

            if (hasAlreadyRequested(idStudent, idProject)) {
                validationError = Optional.of("Ya solicitaste este proyecto anteriormente");
            }

            if (!hasAvailableCapacity(idProject)) {
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
        
        String queryCheckRequestStudent = "SELECT COUNT(*) FROM Solicita_Proyecto WHERE matricula = ? AND estatus = " + STATUS_ASSIGNED + ";";
        
        String queryCount = "SELECT (p.cupo - COUNT(sp.idSolicitud)) as " + COLUMN_AVAILABLE + " " +
                            "FROM Proyecto p LEFT JOIN Solicita_Proyecto sp " +
                            "ON p.idProyecto = sp.idProyecto AND sp.estatus = " + STATUS_ASSIGNED + " " +
                            "WHERE p.idProyecto = ? GROUP BY p.cupo;";

        String queryAssign = "UPDATE Solicita_Proyecto SET estatus = " + STATUS_ASSIGNED + " WHERE matricula = ? AND idProyecto = ?;";
        String queryClean = "DELETE FROM Solicita_Proyecto WHERE matricula = ? AND estatus = " + STATUS_REQUESTED + ";";

        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement preparedStatementCheck = connection.prepareStatement(queryCheckRequestStudent)) {
                    preparedStatementCheck.setString(1, idStudent);
                    try (ResultSet resultSet = preparedStatementCheck.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) > NO_RESULTS) {
                            throw new SQLException(ERROR_ALREADY_ASSIGNED);
                        }
                    }
                }

                try (PreparedStatement preparedStatementCount = connection.prepareStatement(queryCount)) {
                    preparedStatementCount.setInt(1, idProject);
                    try (ResultSet resultSet = preparedStatementCount.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(COLUMN_AVAILABLE) <= NO_RESULTS) {
                            throw new SQLException(ERROR_FULL_CAPACITY);
                        }
                    }
                }

                try (PreparedStatement preparedStatementAssign = connection.prepareStatement(queryAssign)) {
                    preparedStatementAssign.setString(1, idStudent);
                    preparedStatementAssign.setInt(2, idProject);
                    preparedStatementAssign.executeUpdate();
                }

                try (PreparedStatement psClean = connection.prepareStatement(queryClean)) {
                    psClean.setString(1, idStudent);
                    psClean.executeUpdate();
                }

                connection.commit();
                isAssigned = true;

            } catch (SQLException e) {
                connection.rollback();

                if (ERROR_ALREADY_ASSIGNED.equals(e.getMessage())) {
                    throw new OperationException("El estudiante ya cuenta con un proyecto asignado.", e);
                }
                if (ERROR_FULL_CAPACITY.equals(e.getMessage())) {
                    throw new OperationException("El cupo del proyecto se ha agotado.", e);
                }
                throw new OperationException("Error al ejecutar la transacción de asignación", e);
            }
        } catch (SQLException e) {
            throw new OperationException("Error de conexión a la base de datos", e);
        }
        return isAssigned;
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
                String fullName = resultSet.getString("nombre") 
                + " " + resultSet.getString("apellidos") 
                + " (" + resultSet.getString("matricula") + ")";
                applicants.add(fullName);
            }
        }
    } catch (SQLException e) {
        throw new OperationException("Error al obtener los solicitantes del proyecto", e);
    }

    return applicants;
}
    
}
