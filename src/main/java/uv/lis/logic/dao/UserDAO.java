package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.Optional;
import java.util.logging.Level;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IUserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.PasswordHasher;
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

        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        String userQuery = "INSERT INTO Usuario" 
            + "(nombre, apellidos, contraseña, email, idRol) "
            + "VALUES (?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
 
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setInt(5, user.getRoleId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
            
               try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (!resultSet.next()) { 
                        throw new OperationException("No se pudo obtener el ID generado.", null);
                    }
                    generatedId = resultSet.getInt(1);
                } 
            } else {
                throw new OperationException("No se pudo registrar al usuario. Intentelo mas tarde", 
                    null);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo registrar al usuario. Intentelo mas tarde", e);
        }

        return generatedId;
    }

    @Override
    public Optional<User> authenticate(String email, String password) throws AuthenticateException {
        Optional<User> validateUser = Optional.empty();

        String userQuery = "SELECT u.idUsuario, u.email, u.idRol, u.contraseña, ru.nombreRol "
            + "FROM Usuario u "
            + "JOIN Rol_Usuario ru ON u.idRol = ru.idRol "
            + "WHERE u.email = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("contraseña");

                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        User user = new User();
                        user.setId(resultSet.getInt("idUsuario"));
                        user.setEmail(resultSet.getString("email"));
                        user.setRoleId(resultSet.getInt("idRol"));
                        validateUser = Optional.of(user);
                    } else {
                        throw new AuthenticateException("Credenciales incorrectas", null);
                    }

                } else {
                    throw new AuthenticateException("Credenciales incorrectas", null);
                }
            }

        } catch (SQLException e) {
            throw new AuthenticateException("Error al autenticar: " + e.getMessage());
        }
        return validateUser;
    }
}