package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.MAX_REQUESTS;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REQUESTED;
import static uv.lis.logic.utils.InputValidator.STATUS_REJECTED;

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
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class RequestProjectDAO implements IRequestProjectDAO {

    private static final String DEFAULT_NO_PROJECT_MESSAGE = "Sin proyecto asignado";
    private static final Logger LOGGER = Logger.getLogger(RequestProjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public RequestProjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public RequestProjectDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public int getActiveRequestCountByStudentId(String idStudent) throws OperationException {
        int count = 0;
        String requestProjectQuery = "SELECT COUNT(*) AS total FROM Solicita_Proyecto "
                                   + "WHERE matricula = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement
                = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setString(1, idStudent);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar solicitudes del practicante", e);
            throw new OperationException("Error al contar solicitudes del practicante", e);
        }

        return count;
    }

    @Override
    public List<Project> getAvailableProjects() throws OperationException {
        List<Project> projects = new ArrayList<>();
        String requestProjectQuery = "SELECT p.*, "
                                   + "(p.cupo - COALESCE(COUNT(CASE WHEN sp.estatus = ? THEN 1 END), 0)) "
                                   + "AS cupoDisponible "
                                   + "FROM Proyecto p "
                                   + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                   + "WHERE p.estado IS NULL OR p.estado = ? "
                                   + "GROUP BY p.idProyecto HAVING cupoDisponible > 0;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setInt(1, STATUS_ASSIGNED);
            preparedStatement.setInt(2, STATUS_REQUESTED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(buildProjectFromResultSet(resultSet));
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
        String requestProjectQuery = "SELECT COUNT(*) AS total FROM Solicita_Proyecto "
                                   + "WHERE matricula = ? AND idProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idProject);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasRequested = resultSet.getInt("total") > NO_ROWS_AFFECTED;
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
        String requestProjectQuery = "SELECT p.cupo, COUNT(sp.matricula) AS solicitudes "
                                   + "FROM Proyecto p "
                                   + "LEFT JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                   + "AND sp.estatus = ? "
                                   + "WHERE p.idProyecto = ? "
                                   + "GROUP BY p.idProyecto, p.cupo;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

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
            throw new OperationException("Error al verificar el cupo del proyecto", e);
        }

        return hasCapacity;
    }

    @Override
    public boolean requestProject(String idStudent, int idProject) throws OperationException {
        boolean isRegistered = false;
        String requestProjectQuery = "INSERT INTO Solicita_Proyecto "
                                   + "(idProyecto, matricula, estatus) VALUES (?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

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
    public Optional<String> validateProjectRequest(String idStudent, int idProject) throws OperationException {
        Optional<String> validationError = Optional.empty();

        if (getActiveRequestCountByStudentId(idStudent) >= MAX_REQUESTS) {
            validationError = Optional.of("Ya tienes " + MAX_REQUESTS + " solicitudes activas");
        } else if (hasAlreadyRequested(idStudent, idProject)) {
            validationError = Optional.of("Ya solicitaste este proyecto anteriormente");
        } else if (!hasAvailableCapacity(idProject)) {
            validationError = Optional.of("Este proyecto ya no tiene cupo disponible");
        }

        return validationError;
    }

    @Override
    public boolean assignStudentToProject(String idStudent, int idProject) throws OperationException {
        boolean isAssigned = false;
        isAssigned = runAssignmentInTransaction(idStudent, idProject, false);
        return isAssigned;
    }

    @Override
    public void unassignStudentFromProject(String idStudent) throws OperationException {
        String requestProjectQuery = "UPDATE Proyecto p "
                                   + "INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                   + "SET p.cupo = p.cupo + 1 "
                                   + "WHERE sp.matricula = ? AND sp.estatus = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, STATUS_ASSIGNED);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al desasignar estudiante del proyecto", e);
            throw new OperationException("Error al desasignar estudiante del proyecto", e);
        }
    }

    @Override
    public List<Student> getApplicantsByProjectId(int idProject) throws OperationException {
        List<Student> applicants = new ArrayList<>();
        String requestProjectQuery = "SELECT u.nombre, u.apellidos, a.matricula "
                                   + "FROM Usuario u "
                                   + "INNER JOIN Alumno a ON u.idUsuario = a.idUsuario "
                                   + "INNER JOIN Solicita_Proyecto sp ON a.matricula = sp.matricula "
                                   + "WHERE sp.idProyecto = ? AND sp.estatus = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setInt(1, idProject);
            preparedStatement.setInt(2, STATUS_REQUESTED);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    applicants.add(buildStudentFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los solicitantes del proyecto", e);
            throw new OperationException("Error al obtener los solicitantes del proyecto", e);
        }

        return applicants;
    }

    @Override
    public String getProjectAssignedToStudent(String idStudent) throws OperationException {
        String projectName = DEFAULT_NO_PROJECT_MESSAGE;
        String requestProjectQuery = "SELECT p.nombre FROM Proyecto p "
                                   + "INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                   + "WHERE sp.matricula = ? AND sp.estatus = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    projectName = resultSet.getString("nombre");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener el proyecto asignado al estudiante", e);
            throw new OperationException("Error al obtener el proyecto asignado al estudiante", e);
        }

        return projectName;
    }

    @Override
    public ArrayList<Student> getAssignedStudentsByProjectId(int idProject) throws OperationException {
        ArrayList<Student> assignedStudents = new ArrayList<>();
        String requestProjectQuery = "SELECT a.matricula, u.nombre, u.apellidos "
                                   + "FROM Solicita_Proyecto sp "
                                   + "INNER JOIN Alumno a ON sp.matricula = a.matricula "
                                   + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                                   + "WHERE sp.idProyecto = ? AND sp.estatus = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setInt(1, idProject);
            preparedStatement.setInt(2, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    assignedStudents.add(buildStudentFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos asignados al proyecto", e);
            throw new OperationException("Error al obtener los alumnos asignados al proyecto", e);
        }

        return assignedStudents;
    }

    private void executeAssignmentTransaction(Connection databaseConnection, String idStudent,  
        int idProject) throws SQLException, OperationException {
        ensureStudentNotAlreadyAssigned(databaseConnection, idStudent);
        hasAvailableCapacity(idProject);
        assignRequest(databaseConnection, idStudent, idProject);
        cleanPendingRequests(databaseConnection, idStudent);
    }

    private void ensureStudentNotAlreadyAssigned(Connection databaseConnection, String idStudent)
        throws SQLException, OperationException {
        String requestProjectQuery = "SELECT COUNT(*) FROM Solicita_Proyecto "
                                   + "WHERE matricula = ? AND estatus = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > NO_ROWS_AFFECTED) {
                    throw new OperationException(
                        "El estudiante ya cuenta con un proyecto asignado.", null);
                }
            }
        }
    }

    private void assignRequest(Connection databaseConnection, String idStudent, int idProject) throws SQLException {
        String requestProjectQuery = "UPDATE Solicita_Proyecto SET estatus = ? "
                                   + "WHERE matricula = ? AND idProyecto = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {
            preparedStatement.setInt(1, STATUS_ASSIGNED);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, idProject);
            preparedStatement.executeUpdate();
        }
    }

    private void cleanPendingRequests(Connection databaseConnection, String idStudent) throws SQLException {
        String requestProjectQuery = "UPDATE Solicita_Proyecto SET estatus = ? "
                                   + "WHERE matricula = ? AND estatus = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {
            preparedStatement.setInt(1, STATUS_REJECTED);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, STATUS_REQUESTED);
            preparedStatement.executeUpdate();
        }
    }

    private Project buildProjectFromResultSet(ResultSet resultSet) throws SQLException {
        Project project = new Project();
        project.setId(resultSet.getInt("idProyecto"));
        project.setName(resultSet.getString("nombre"));
        project.setDescription(resultSet.getString("descripcion"));
        project.setCapacity(resultSet.getInt("cupo"));
        project.setMethodology(resultSet.getString("metodologiaProyecto"));
        project.setObjective(resultSet.getString("objetivo"));
        return project;
    }

    private Student buildStudentFromResultSet(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setIdStudent(resultSet.getString("matricula"));
        student.setFirstName(resultSet.getString("nombre"));
        student.setLastName(resultSet.getString("apellidos"));
        return student;
    }
    
    @Override
    public ArrayList<Student> getStudentsWithoutAssignedProject() throws OperationException {
        ArrayList<Student> students = new ArrayList<>();
        String requestProjectQuery = "SELECT a.matricula, u.nombre, u.apellidos "
                                   + "FROM Alumno a "
                                   + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                                   + "WHERE NOT EXISTS ( "
                                   + "SELECT 1 FROM Solicita_Proyecto sp "
                                   + "WHERE sp.matricula = a.matricula AND sp.estatus = ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(requestProjectQuery)) {

            preparedStatement.setInt(1, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos sin proyecto asignado", e);
            throw new OperationException("Error al obtener alumnos sin proyecto asignado", e);
        }
        return students;
    }

    @Override
    public boolean assignStudentToProjectAlternative(String idStudent, int idProject) throws OperationException {
        boolean isAssigned = false;
        isAssigned = runAssignmentInTransaction(idStudent, idProject, true);
        return isAssigned;
    }

    private void insertAssignment(Connection databaseConnection, String idStudent, int idProject) throws SQLException {
        String query = "INSERT INTO Solicita_Proyecto (idProyecto, matricula, estatus) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setInt(1, idProject);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, STATUS_ASSIGNED);
            preparedStatement.executeUpdate();
        }
    }
    
    private void executeAlternativeAssignment(Connection databaseConnection, String idStudent, int idProject) 
        throws SQLException, OperationException {
        ensureStudentNotAlreadyAssigned(databaseConnection, idStudent);
        insertAssignment(databaseConnection, idStudent, idProject);
    }

    private boolean runAssignmentInTransaction(String idStudent, int idProject, boolean isAlternative) 
        throws OperationException {
        boolean isAssigned = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try {
                if (isAlternative) {
                    executeAlternativeAssignment(databaseConnection, idStudent, idProject);
                } else {
                    executeAssignmentTransaction(databaseConnection, idStudent, idProject);
                }
                databaseConnection.commit();
                isAssigned = true;
            } catch (SQLException sqlException) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Transacción de asignación cancelada", sqlException);
                throw new OperationException("Error al ejecutar la transacción de asignación", sqlException);
            } catch (OperationException operationException) {
                databaseConnection.rollback();
                throw operationException;
            } finally {
                databaseConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión a la base de datos", e);
            throw new OperationException("Error al asignar un proyecto", e);
        }

        return isAssigned;
    }
}