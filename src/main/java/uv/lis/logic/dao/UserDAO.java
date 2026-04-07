package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
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
        int generateId = -1;
        String userQuery = "INSERT INTO Usuario" 
            + "(nombre, apellidos, contraseña, identificador, rol) "
            + "VALUES (?, ?, ?,?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
 
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getIdentification());
            preparedStatement.setString(5,user.getUserType());

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                generateId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return generateId;
    }

    @Override
    public Professor getProfessorById(String personnelNumber) {
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
        
        String proffesorQuery = "INSERT INTO Profesor (idUsuario, numeroPersonal, rol, estado) VALUES (?, ?, ?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(proffesorQuery)) {
            
            preparedStatement.setInt(1, professor.getId());      
            preparedStatement.setString(2, professor.getPersonnelNumber());     

            if (professor.getIsCoordinator()) { 
                preparedStatement.setString(3, "Coordinador");
            } else {
                preparedStatement.setString(3, "Maestro");
            }

            preparedStatement.setString(4,"1");

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

        String proffesorQuery = "UPDATE Profesor SET estado = 'Inactivo' WHERE numeroPersonal = ?;";

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

        String query = "SELECT e.idUsuario, e.matricula, u.nombre, u.apellidoMaterno, u.apellidoPaterno " 
            + "FROM Practicante e INNER JOIN Usuario u ON e.idUsuario = u.idUsuario WHERE e.idUsuario = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            
            preparedStatement.setString(1, idStudent);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    student = new Student();
                    student.setIdStudent(rs.getString("matricula"));
                    student.setFirstName(rs.getString("nombre"));
                    student.setLastName(rs.getString("apellidos"));
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

        String studentQuery = "INSERT INTO Practicante (idUsuario, matricula) VALUES (?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(studentQuery)) {
            
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setString(2, student.getIdStudent()); 

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

        String studentQuery = "UPDATE Practicante e " 
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
        
        String studentQuery = "UPDATE Practicante SET estado = 1 WHERE idUsuario = ?;";

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
    public String getUserType(User user) {
        /*try (Connection databaseConnection = MySQLConnectionManager.getConnection()) {
            String query = "SELECT rol FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario WHERE u.idUsuario = ?;";
            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
                preparedStatement.setInt(1, user.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("rol");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
        }
        return null;*/
        String userType = "Coordinador";
        return userType;
    }

@Override
    public User authenticate(String identification, String password) {
        User userAuthenticate = null;
        String userQuery = "SELECT u.idUsuario, u.contraseña, a.matricula, p.numeroPersonal " +
                       "FROM Usuario u " +
                       "LEFT JOIN Alumno a ON u.idUsuario = a.idUsuario " +
                       "LEFT JOIN Profesor p ON u.idUsuario = p.idUsuario " +
                       "WHERE (a.matricula = ? OR p.numeroPersonal = ?) " +
                       "AND u.contraseña = ?";

        try (Connection databasConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databasConnection.prepareStatement(userQuery)){
                preparedStatement.setString(1,identification);
                preparedStatement.setString(2, identification);
                preparedStatement.setString(3, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()) {
                        userAuthenticate = new User();
                        userAuthenticate.setIdentification(identification);

                        if(resultSet.getString("matricula") != null) {
                            userAuthenticate.setUserType("Alumno");
                        } else if (resultSet.getString("numeroPersonal") != null) {
                            userAuthenticate.setUserType("Profesor");
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error de autenticacion", e);
            }
            return userAuthenticate;
    }
}
