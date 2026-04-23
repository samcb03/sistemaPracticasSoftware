package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IStudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;


public class StudentDAO extends UserDAO implements IStudentDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public StudentDAO(){
        this.connectionManager = new MySQLConnectionManager();
    }
    
    public StudentDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Student getStudentById(String idStudent) throws OperationException { 
        Student student = null;

    String studentQuery = "SELECT e.idUsuario, e.matricula, u.nombre, u.apellidos, e.fechaNacimiento, e.genero," 
        + "e.lenguaIndigena FROM Alumno e INNER JOIN Usuario u ON e.idUsuario = u.idUsuario WHERE e.matricula = ?;"; 

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    student = new Student();
                    student.setId(resultSet.getInt("idUsuario"));
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                    student.setBirthDate(resultSet.getDate("fechaNacimiento"));
                    student.setGender(resultSet.getString("genero"));

                    LOGGER.log(Level.INFO, "Busqueda de responsable de proyecto con matricula {0} exitosa.", 
                        idStudent);
                } else {
                    LOGGER.log(Level.INFO, "No se encontro un alumno con la matricula {0}.", idStudent);
                    throw new OperationException("No se encontró un alumno con la matricula: " + idStudent, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo buscar el alumno. Intentelo mas tarde", e);
        }
        
        return student;
    }

    @Override
    public boolean registerStudent(Student student) throws OperationException {
        boolean isRegistered = false;

        String studentQuery = "INSERT INTO Alumno (idUsuario, matricula, fechaNacimiento, genero," 
            + "estado) VALUES (?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setString(2, student.getIdStudent());
            java.util.Date birthDate = student.getBirthDate();
            java.sql.Date sqlBirthDate = new java.sql.Date(birthDate.getTime());

            preparedStatement.setDate(3, sqlBirthDate);
            preparedStatement.setString(4, student.getGender());
            preparedStatement.setBoolean(5, student.isInactive());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de alumno con matricula {0} exitoso.", student.getIdStudent());
            } else {
                throw new OperationException("No se pudo registrar al alumno. Intentelo mas tarde", null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo registrar al alumno. Intentelo mas tarde", e);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifyStudent(Student student) throws OperationException {
        boolean isModified = false;

        String studentQuery = "UPDATE Alumno e " 
            + "INNER JOIN Usuario u ON e.idUsuario = u.idUsuario SET e.matricula = ?, u.nombre = ?, u.apellidos = ? " 
            + "WHERE e.idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, student.getIdStudent());
            preparedStatement.setString(2, student.getFirstName());
            preparedStatement.setString(3, student.getLastName());
            preparedStatement.setInt(4, student.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Modificacion de alumno con matricula {0} exitosa.", student.getIdStudent());
            } else {
                throw new OperationException("No se pudo modificar al alumno. Intentelo mas tarde", null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo modificar al alumno. Intentelo mas tarde", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateStudent(Student student) throws OperationException {
        boolean isInactive = false;
        
        String studentQuery = "UPDATE Alumno SET estado = 0 WHERE idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, student.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Inactivacion de alumno con matricula exitosa.", student.getIdStudent());
            } else {
                throw new OperationException("No se pudo inactivar al alumno. Intentelo mas tarde", null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo inactivar al alumno. Intentelo mas tarde", e);
        }

        return isInactive;
    }
}