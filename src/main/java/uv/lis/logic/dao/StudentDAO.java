package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_VALUE;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

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
import uv.lis.logic.contracts.IStudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public class StudentDAO extends UserDAO implements IStudentDAO {
    
    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class.getName());
    private static final int FIRST_INDEX = 1;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_INACTIVE = 0;
    
    private MySQLConnectionManager connectionManager;

    public StudentDAO() {
        connectionManager = new MySQLConnectionManager();
    }
    
    public StudentDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ArrayList<Student> getAllActiveStudents() throws OperationException {
        ArrayList<Student> students = new ArrayList<>();
        String studentQuery = "SELECT a.matricula, u.nombre, u.apellidos, a.fechaNacimiento, a.genero "
                            + "FROM Alumno a "
                            + "JOIN Usuario u ON a.idUsuario = u.idUsuario "
                            + "WHERE u.estado = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, STATUS_ACTIVE);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                    student.setBirthDate(resultSet.getDate("fechaNacimiento"));
                    student.setGender(resultSet.getString("genero"));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos sin EE asignada", e);
            throw new OperationException("No se pudo obtener los alumnos disponibles. Intente más tarde", e);
        }
        return students;
    }

    @Override
    public Optional<Student> getStudentById(int idStudent) throws OperationException { 
        Optional<Student> validateStudent = Optional.empty();
        String studentQuery = "SELECT e.matricula, u.nombre, u.apellidos, e.fechaNacimiento, e.genero "
                            + "FROM Alumno e INNER JOIN Usuario u ON e.idUsuario = u.idUsuario "
                            + "WHERE e.idUsuario = ?"; 

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Student student = new Student();
                    student.setId(idStudent);
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                    student.setBirthDate(resultSet.getDate("fechaNacimiento"));
                    student.setGender(resultSet.getString("genero"));
                    validateStudent = Optional.of(student);

                    LOGGER.log(Level.INFO, "Busqueda de alumno con matricula {0} exitosa.", idStudent);
                } else {
                    LOGGER.log(Level.INFO, "No se encontro un alumno con la matricula {0}.", idStudent);
                    throw new OperationException("No se encontró un alumno con la matricula: " + idStudent, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo buscar el alumno. Intentelo mas tarde", e);
        }
        return validateStudent;
    }

    @Override
    public Optional<Integer> getIdUserByStudentId(String studentId) throws OperationException {
        Optional<Integer> validateUserId = Optional.empty();
        String studentQuery = "SELECT idUsuario FROM Alumno WHERE matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, studentId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int userId = resultSet.getInt("idUsuario");
                    validateUserId = Optional.of(userId);
                } else {
                    LOGGER.log(Level.INFO, "No se encontro estudiante con la matricula: {0}", studentId);
                    throw new OperationException("No se encontró un alumno con la matricula: " + studentId, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            throw new OperationException("No se pudo buscar el alumno. Intentelo mas tarde", e);
        }
        return validateUserId;
    }

    @Override
    public ArrayList<Student> getActiveStudentsNotInSubject() throws OperationException {
        ArrayList<Student> students = new ArrayList<>();
        String studentQuery = "SELECT a.matricula, u.nombre, u.apellidos "
                            + "FROM Alumno a "
                            + "JOIN Usuario u ON a.idUsuario = u.idUsuario "
                            + "WHERE u.estado = ? "
                            + "AND a.matricula NOT IN ("
                            + "SELECT matricula FROM Alumno_Esta_EE)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, STATUS_ACTIVE);

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
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos sin EE asignada", e);
            throw new OperationException("No se pudo obtener los alumnos disponibles. Intente más tarde", e);
        }
        return students;
    }

    @Override
    public boolean registerStudent(Student student) throws OperationException {
        boolean isRegistered = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try {
                persistStudentRegistration(student, databaseConnection);
                isRegistered = true;
            } catch (OperationException operationException) {
                databaseConnection.rollback();
                throw operationException;
            } finally {
                databaseConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo registrar al alumno. Intentelo mas tarde", e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyStudent(Student student) throws OperationException {
        boolean isModified = false;
        String studentQuery = "UPDATE Alumno a " 
                            + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario SET u.nombre = ?, u.apellidos = ?, " 
                            + "a.fechanacimiento = ?, a.genero = ? "
                            + "WHERE a.matricula = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, student.getFirstName());
            preparedStatement.setString(2, student.getLastName());
            preparedStatement.setDate(3, student.getBirthDate());
            preparedStatement.setString(4, student.getGender());
            preparedStatement.setString(5, student.getIdStudent());

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isModified = true;
                LOGGER.log(Level.INFO, "Modificacion de alumno con matricula {0} exitosa.", student.getIdStudent());
            } else {
                throw new OperationException("No se pudo modificar al alumno. Intentelo mas tarde", 
                    null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo modificar al alumno. Intentelo mas tarde", e);
        }
        return isModified;
    }

    @Override
    public boolean inactivateStudent(String studentId) throws OperationException {
        boolean isInactive = false;
        String studentQuery = "UPDATE Alumno a INNER JOIN Usuario u ON a.idUsuario = u.idUsuario SET u.estado = ?" 
                            + " WHERE a.matricula = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, STATUS_INACTIVE);
            preparedStatement.setString(2, studentId);

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Inactivacion de alumno con matricula exitosa.", studentId);
            } else {
                throw new OperationException("No se pudo inactivar al alumno. Intentelo mas tarde", 
                null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo inactivar al alumno. Intentelo mas tarde", e);
        }
        return isInactive;
    }

    @Override
    public boolean isStudentInactive(String studentId) throws OperationException {
        boolean isActive = false;
        String studentQuery = "SELECT u.estado FROM Alumno a INNER JOIN Usuario u ON a.idUsuario = u.idUsuario"  
                            + " WHERE a.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isActive = resultSet.getInt("estado") == 0;
                } else {
                    throw new OperationException("No se encontró un alumno con la matrícula: " + studentId, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar estado del alumno", e);
            throw new OperationException("No se pudo verificar el estado del alumno. Intente más tarde", e);
        }
        return isActive;
    }

    @Override
    public ArrayList<String> searchStudentIds(String prefix) throws OperationException {
        ArrayList<String> studentIds = new ArrayList<>();
        String studentQuery = "SELECT matricula FROM Alumno WHERE matricula LIKE ? LIMIT 10";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {

            preparedStatement.setString(1, prefix + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    studentIds.add(resultSet.getString("matricula"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar matriculas", e);
            throw new OperationException("No se pudieron obtener las matriculas", e);
        }
        return studentIds;
    }

    @Override
    public boolean hasProjectAssigned(String studentId) throws OperationException {
        boolean hasProject = false;
        String studentQuery = "SELECT COUNT(*) FROM Solicita_Proyecto WHERE matricula = ? AND estatus = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, studentId);
            preparedStatement.setInt(2, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasProject = resultSet.getInt(FIRST_INDEX) > NO_VALUE;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al verificar asignacion de proyecto", sqlException);
            throw new OperationException("No se pudo verificar la asignación de proyecto. Intente más tarde", 
                sqlException);
        }
        return hasProject;
    }

    @Override
    public List<Student> getStudentsByIds(List<String> studentIds) throws OperationException {
        List<Student> students = new ArrayList<>();
        String studentQuery = "SELECT a.matricula, u.nombre, u.apellidos "
                            + "FROM Alumno a "
                            + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                            + "WHERE a.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {

            for (String studentId : studentIds) {
                preparedStatement.setString(1, studentId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Student student = new Student();
                        student.setIdStudent(resultSet.getString("matricula"));
                        student.setFirstName(resultSet.getString("nombre"));
                        student.setLastName(resultSet.getString("apellidos"));
                        students.add(student);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos por matrícula", e);
            throw new OperationException("No se pudieron obtener los alumnos. Intente más tarde", e);
        }
        return students;
    }

    private void persistStudentRegistration(Student student, Connection databaseConnection) throws OperationException {
        try {
            int generatedUserId = registerUser(student);
            student.setId(generatedUserId);
            insertStudent(student, databaseConnection);
            databaseConnection.commit();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Transacción de registro de alumno cancelada", e);
            throw new OperationException("Error al guardar la informacion dell alumno. Intentelo mas tarde", e);
        }
    }

    private void insertStudent(Student student, Connection databaseConnection) throws SQLException, OperationException {
        String studentQuery = "INSERT INTO Alumno (idUsuario, matricula, fechaNacimiento, genero)"
                            + " VALUES (?, ?, ?, ?);";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setString(2, student.getIdStudent());
            preparedStatement.setDate(3, student.getBirthDate());
            preparedStatement.setString(4, student.getGender());

            if (preparedStatement.executeUpdate() <= NO_VALUE) {
                throw new OperationException("El registro del alumno no afectó ninguna fila", null);
            }
        }
    }
}