package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.INVALID_ID;
import static uv.lis.logic.utils.InputValidator.IS_COORDINATOR;
import static uv.lis.logic.utils.InputValidator.NO_VALUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;;

public class ProfessorDAO extends UserDAO implements IProfessorDAO {
    private static final int IS_PROFESSOR = 2;
    private static final int INACTIVE_STATUS = 0;
    private static final int ACTIVE_STATUS = 1;
    private static final Logger LOGGER = Logger.getLogger(ProfessorDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ProfessorDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ProfessorDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public LinkedHashMap<String, String> getAllActiveProfessorsMap() throws OperationException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String professorQuery = "SELECT p.numeroPersonal, u.nombre, u.apellidos"
                              + " FROM Profesor p"
                              + " INNER JOIN Usuario u ON p.idUsuario = u.idUsuario"
                              + " WHERE u.estado = 1";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String fullName = resultSet.getString("nombre") + " " + resultSet.getString("apellidos");
                map.put(fullName, resultSet.getString("numeroPersonal"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener profesores", e);
            throw new OperationException("Error al obtener profesores", e);
        }

        return map;
    }

    @Override
    public Optional<String> getProfessorPersonnelNumberByName(String firstName, String lastName) 
        throws OperationException {
        Optional<String> validatePersonnelNumber = Optional.empty();
        String professorQuery = "SELECT p.numeroPersonal "
                              + "FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario "
                              + "WHERE u.nombre = ? AND u.apellidos = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String personnelNumber = resultSet.getString("numeroPersonal");
                    validatePersonnelNumber = Optional.of(personnelNumber);
                } else {
                    throw new OperationException("No se encontró al profesor", null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos", e);
            throw new OperationException("Error al obtener el número de personal del profesor", e);
        }

        return validatePersonnelNumber;
    }

    @Override
    public Optional<Professor> getProfessorById(int id) throws OperationException {
        Optional<Professor> validateProfessor = Optional.empty();
        String professorQuery = "SELECT p.numeroPersonal, u.nombre, u.apellidos, u.idRol "
                              + "FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario "
                              + "WHERE p.idUsuario = ? AND u.estado = 1";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Professor professor = new Professor();
                    professor.setId(id);
                    professor.setPersonnelNumber(resultSet.getString("numeroPersonal"));
                    professor.setFirstName(resultSet.getString("nombre"));
                    professor.setLastName(resultSet.getString("apellidos"));
                    professor.setIsCoordinator(resultSet.getInt("idRol") == IS_COORDINATOR);
                    validateProfessor = Optional.of(professor);
                } else {
                    throw new OperationException("No se encontró ningun profesor", null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos", e);
            throw new OperationException("Error al obtener el profesor", e);
        }

        return validateProfessor;
    }

    @Override
    public boolean registerProfessor(Professor professor) throws OperationException {
        boolean isRegistered = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try {
                persistProfessorRegistration(professor, databaseConnection);
                isRegistered = true;
            } catch (OperationException operationException) {
                databaseConnection.rollback();
                throw operationException;
            } finally {
                databaseConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos al registrar el profesor", e);
            throw new OperationException("Error al registrar el profesor", e);
        }

        return isRegistered;
    }

    @Override
    public boolean modifyProfessor(Professor professor) throws OperationException {
        if (professor.getIsCoordinator()) {
            if (isAnotherCoordinatorActive(professor.getPersonnelNumber())) {
                throw new OperationException("No es posible asignar el cargo:" 
                    + " ya existe un coordinador activo en el sistema.", null);
            }
        }

        boolean isModified = false;
        String professorQuery = "UPDATE Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario "
                              + "SET u.idRol = ?, u.nombre = ?, u.apellidos = ? "
                              + "WHERE p.numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setInt(1, professor.getIsCoordinator() ? IS_COORDINATOR : IS_PROFESSOR);
            preparedStatement.setString(2, professor.getFirstName());
            preparedStatement.setString(3, professor.getLastName());
            preparedStatement.setString(4, professor.getPersonnelNumber());

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isModified = true;
                LOGGER.log(Level.INFO, "Profesor {0} modificado exitosamente.", professor.getPersonnelNumber());
            } else {
                throw new OperationException("No se pudo modificar al profesor con número: "
                    + professor.getPersonnelNumber(), null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos al modificar el profesor", e);
            throw new OperationException("Error al modificar el profesor", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProfessor(String personalNumber) throws OperationException {
        boolean isInactived = false;
        String professorQuery = "UPDATE Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario SET u.estado = ? " 
                              + "WHERE p.numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personalNumber);
            preparedStatement.setInt(2, INACTIVE_STATUS);

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isInactived = true;
                LOGGER.log(Level.INFO, "Profesor {0} inactivado exitosamente.", personalNumber);
            } else {
                throw new OperationException("No se pudo inactivar al profesor con número: "
                    + personalNumber, null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos al inactivar un profesor", e);
            throw new OperationException("Error al inactivar el profesor", e);
        }

        return isInactived;
    }

    @Override
    public boolean isProfessorInactive(String personnelNumber) throws OperationException {
        boolean isInactive = false;
        String professorQuery = "SELECT u.estado FROM Profesor p INNER JOIN Usuario u " 
                              + "ON p.idUsuario = u.idUsuario WHERE p.numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isInactive = resultSet.getInt("estado") == NO_VALUE;
                } else {
                    throw new OperationException("No se encontró al profesor con número: "
                        + personnelNumber, null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar el estado del profesor", e);
            throw new OperationException("No se pudo verificar al profesor. Intente más tarde", e);
        }

        return isInactive;
    }

    @Override
    public int getIdUserByProfessorPersonnelNumber(String personnelNumber) throws OperationException {
        int idUser = INVALID_ID;
        String professorQuery = "SELECT idUsuario FROM Profesor WHERE numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    idUser = resultSet.getInt("idUsuario");
                } else {
                    LOGGER.log(Level.INFO, "No se encontro un Profesor con el número de personal {0}.", 
                        personnelNumber);
                    throw new OperationException("No se encontró ningun profesor", null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos", e);
            throw new OperationException("No se pudo buscar al profesor. Inténtelo más tarde", e);
        }

        return idUser;
    }

    @Override
    public ArrayList<String> searchProfessorPersonalNumbers(String prefix) throws OperationException {
        ArrayList<String> professorPersonnelNumbers = new ArrayList<>();
        String professorQuery = "SELECT numeroPersonal FROM Profesor"
                              + " WHERE numeroPersonal LIKE ? LIMIT 10";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, prefix + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    professorPersonnelNumbers.add(resultSet.getString("numeroPersonal"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar números de personal", e);
            throw new OperationException("No se pudieron obtener los números de personal", e);
        }

        return professorPersonnelNumbers;
    }

    @Override
    public boolean hasSubjectAssigned(String personnelNumber) throws OperationException {
        boolean hasSubject = false;
        String professorQuery = "SELECT 1 FROM Profesor_Imparte_Experiencia"
                              + " WHERE numeroPersonal = ? AND estaActiva = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);
            preparedStatement.setInt(2, ACTIVE_STATUS);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hasSubject = resultSet.next();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar asignación de experiencia", e);
            throw new OperationException("No se pudo verificar la asignación. Intente más tarde", e);
        }

        return hasSubject;
    }

    @Override
    public ArrayList<String> getSubjectsByProfessor(String personnelNumber) throws OperationException {
        ArrayList<String> history = new ArrayList<>();
        String professorQuery = "SELECT ee.nrc, ee.nombreExperiencia, ee.carrera, pe.nombre AS periodo "
                              + "FROM Profesor_Imparte_Experiencia pie "
                              + "JOIN ExperienciaEducativa ee ON pie.NRC = ee.NRC "
                              + "JOIN PeriodoEscolar pe ON ee.idPeriodoEscolar = pe.idPeriodoEscolar "
                              + "WHERE pie.numeroPersonal = ? "
                              + "ORDER BY pe.FechaInicio DESC";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String entry = "NRC: " + resultSet.getInt("NRC")
                        + " — " + resultSet.getString("nombreExperiencia")
                        + " — " + resultSet.getString("carrera")
                        + " (" + resultSet.getString("periodo") + ")";
                    history.add(entry);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener historial de experiencias", e);
            throw new OperationException("No se pudo obtener el historial del profesor.", e);
        }

        return history;
    }

    @Override
    public boolean isAnotherCoordinatorActive(String personnelNumber) throws OperationException {
        boolean exits = false;
        String professorQuery = "SELECT COUNT(*) "
                              + "FROM Usuario u "
                              + "INNER JOIN Profesor p ON u.idUsuario = p.idUsuario "
                              + "WHERE u.idRol = ? AND u.estado = ? AND p.numeroPersonal <> ?;";
        
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {
            
            preparedStatement.setString(1, personnelNumber);
            preparedStatement.setInt(2, IS_COORDINATOR);
            preparedStatement.setInt(3, ACTIVE_STATUS);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    exits = resultSet.getInt(1) > NO_VALUE;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al validar el coordinador activo", e);
            throw new OperationException("Error al validar el coordinador ", e);
        }
        return exits;
    }

    private void persistProfessorRegistration(Professor professor, Connection databaseConnection) 
        throws OperationException {
        try {
            int generatedUserId = registerUser(professor);
            professor.setId(generatedUserId);
            insertProfessor(professor, databaseConnection);
            databaseConnection.commit();
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Transacción de registro de alumno cancelada", sqlException);
            throw new OperationException("Error al guardar la informacion del profesor. Intentelo mas tarde", 
                sqlException);
        }
    }

    private void insertProfessor(Professor professor, Connection databaseConnection)
        throws SQLException, OperationException {
        String professorQuery = "INSERT INTO Profesor (idUsuario, numeroPersonal) VALUES (?, ?);";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {
            preparedStatement.setInt(1, professor.getId());
            preparedStatement.setString(2, professor.getPersonnelNumber());

            if (preparedStatement.executeUpdate() <= NO_VALUE) {
                throw new OperationException("No se pudo registrar al profesor", null);
            }
        }
    }
}