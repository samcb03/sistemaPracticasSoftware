package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IUserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.exceptions.AuthenticateException;


public class UserDAO implements IUserDAO{
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public UserDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override 
    public int registerUser(User user) throws OperationException {
        int generatedId = -1;
        String userQuery = "INSERT INTO Usuario" 
            + "(nombre, apellidos, contraseña, rol) "
            + "VALUES (?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
 
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getUserType());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
            
               try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) { 
                        generatedId = resultSet.getInt(1);
                    }
                } 
            } else {
                throw new OperationException("No se pudo registrar al usuario. Intentelo mas tarde", null);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo registrar al usuario. Intentelo mas tarde", e);
        }

        return generatedId;
    }

    @Override
    public User authenticate(String email, String password) throws AuthenticateException {
        User userAuthenticate = null;
        String userQuery = "SELECT u.idUsuario, u.contraseña,u.email, ru.nombreRol "
            + "FROM Usuario u "
            + "LEFT JOIN Alumno a ON u.idUsuario = a.idUsuario "
            + "LEFT JOIN Profesor p ON u.idUsuario = p.idUsuario "
            + "LEFT JOIN Administrador ad ON u.idUsuario = ad.idUsuario " 
            + "LEFT JOIN Rol_Usuario ru ON u.idRol = ru.idRol"
            + "WHERE u.email = ? "
            + "AND u.contraseña = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery)){
             
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()) {
                        userAuthenticate = new User();
                        userAuthenticate.setEmail(email);

                        if(resultSet.getString("nombreRol") != null) {
                            userAuthenticate.setUserType(resultSet.getString("nombreRol")); 
                        }
                    } else {
                        throw new AuthenticateException("Usuario no encontrado, verifique sus datos", null);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error de autenticacion SQL", e);
                throw new AuthenticateException("No disponible por el momento. Intentelo mas tarde", e);
            }   
            return userAuthenticate;
        }
}