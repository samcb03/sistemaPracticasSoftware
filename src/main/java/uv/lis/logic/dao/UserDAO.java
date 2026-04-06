package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IUserDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;


public class UserDAO implements IUserDAO{
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public UserDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

@Override 
    public int registerUser(User user) {
        int generatedId = -1;

        String userQuery = "INSERT INTO Usuario " 
            + "(nombre, apellidos, contraseña, tipoUsuario, identificador) " 
            + "VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
 
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(4, user.getPassword());
            preparedStatement.setString(5, user.getUserType());
            preparedStatement.setString(6, user.getIdentification());

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                generatedId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return generatedId;
    }

    @Override
    public Professor getProfessorByPersonnelNumber(String personnelNumber) {
        Professor professor = null;
        String professorQuery = "SELECT p.numeroPersonal, p.rol, u.nombre, u.apellidos " 
            + "FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario WHERE p.numeroPersonal = ?";

        try (Connection databasConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databasConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    professor = new Professor();
                    professor.setId(resultSet.getInt("numeroPersonal")); 
                    professor.setFirstName(resultSet.getString("nombre")); 
                    professor.setLastName(resultSet.getString("apellidos"));
                    professor.setIsCoordinator(false); 

                    LOGGER.log(Level.INFO, "Busqueda de Profesor con numero de personal obtenido con exito", 
                        professor.getPersonnelNumber());
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return professor;
    }

@Override
    public boolean registerProfessor(Professor professor) {
        boolean isRegistered = false;
        
        String proffesorQuery = "INSERT INTO profesor (idUsuario, numeroPersonal, rol, estado) VALUES (?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(proffesorQuery)) {
            
            preparedStatement.setInt(1, professor.getId());      
            preparedStatement.setString(2, professor.getPersonnelNumber());     

            if (professor.getIsCoordinator()) { 
                preparedStatement.setString(3, "Coordinador");
            } else {
                preparedStatement.setString(3, "Maestro");
            }

            preparedStatement.setString(4, "1");

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de profesor con ID {0} exitosa.", professor.getPersonnelNumber());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        
        return isRegistered;
    }

@Override
    public boolean modifyProfessor(Professor professor) {
        boolean isModified = false;

        String proffesorQuery = "UPDATE Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario SET p.rol = ?," 
            + "u.nombre = ?, u.apellidos = ? WHERE p.numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStament = databaseConnection.prepareStatement(proffesorQuery)) {

            if (professor.getIsCoordinator()) { 
                preparedStament.setString(1, "Coordinador");
            } else {
                preparedStament.setString(1, "Maestro");
            }

            preparedStament.setString(2, professor.getFirstName());
            preparedStament.setString(3, professor.getLastName());
            
            preparedStament.setInt(4, professor.getId()); 

            if (preparedStament.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Modificacion de profesor con ID {0} exitosa.", professor.getPersonnelNumber());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProfessor(Professor professor) {
        boolean isInactived = false;

        String proffesorQuery = "UPDATE Profesor SET estado = '0' WHERE numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(proffesorQuery)) {
            
            preparedStatement.setString(1, professor.getPersonnelNumber());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactived = true;
                LOGGER.log(Level.INFO, "Inactivacion de profesor con numero de personal exitosa.", 
                    professor.getPersonnelNumber());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isInactived;
    }

    @Override
    public Student getStudentById(String idStudent) { 
        Student student = null;

        String query = "SELECT e.idUsuario, e.matricula, u.nombre, u.apellidos " 
            + "FROM Alumno e INNER JOIN Usuario u ON e.idUsuario = u.idUsuario WHERE e.idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    student = new Student();
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                }
                LOGGER.log(Level.INFO, "Busqueda de responsable de proyecto con ID {0} exitosa.", 
                    student.getId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        
        return student;
    }

    @Override
    public boolean registerStudent(Student student) {
        boolean isRegistered = false;
        
        String studentQuery = "INSERT INTO Alumno (idUsuario, matricula, estado, genero, lenguaIndigena)" 
            + "VALUES (?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setString(2, student.getIdStudent());
            preparedStatement.setString(3, "1");
            preparedStatement.setString(4, student.getGender());
            preparedStatement.setBoolean(5, student.hasIndigenousLanguage());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de estudiante con ID {0} exitoso.", student.getId());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifyStudent(Student student) {
        boolean isModified = false;

        String studentQuery = "UPDATE Alumno e " 
            + "INNER JOIN Usuario u ON e.idUsuario = u.idUsuario SET e.matricula = ?, u.nombre = ?, u.apellidos = ?" 
            + "WHERE e.idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setString(1, student.getIdStudent());
            preparedStatement.setString(2, student.getFirstName());
            preparedStatement.setString(3, student.getLastName());
            preparedStatement.setInt(4, student.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Modificacion de alumno con matricula exitosa.", student.getIdStudent());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateStudent(Student student) {
        boolean isInactive = false;
        
        String studentQuery = "UPDATE Alumno SET estado = 1 WHERE idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, student.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Inactivacion de alumno con matricula exitosa.", student.getIdStudent());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isInactive;
    }

    @Override
    public User authenticate(String identification, String password) {
        String query = "SELECT u.tipoUsuario, p.rol FROM Usuario u LEFT JOIN Profesor p ON u.idUsuario = p.idUsuario " 
            + "WHERE u.identificador = ? AND u.contraseña = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setString(1, identification);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                User user = new User();
                user.setIdentification(identification);

                String type = resultSet.getString("userType");
                String rol = resultSet.getString("rol");

                if ("Coordinador".equalsIgnoreCase(rol)) {
                    user.setUserType("Coordinator");
                } else if ("Profesor".equalsIgnoreCase(type)) {
                    user.setUserType("Professor");
                } else {
                    user.setUserType("Student");
                }

                return user;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en autenticación", e);
        }

        return null;
    }
}
