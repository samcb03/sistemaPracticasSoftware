package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IUserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.PasswordHasher;

public class UserDAO implements IUserDAO {
    private static final int ROL_COORDINATOR = 3;
    private static final int ROLE_PROFESSOR = 2;
    private static final int STATUS_ACTIVE = 1;
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public UserDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public UserDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override 
    public int registerUser(User user) throws OperationException {
        int generatedId = -1;

        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        String userQuery = "INSERT INTO Usuario" 
                         + "(nombre, apellidos, contraseña, email, idRol, estado) "
                         + "VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery,
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (user.getRoleId() == ROL_COORDINATOR && existActiveCoordinator()) {
                throw new OperationException("Ya existe un coordinador activo en el sistema", null);
            }
 
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setInt(5, user.getRoleId());
            preparedStatement.setBoolean(6, user.isActive());

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
                         + "WHERE u.email = ? AND u.estado = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery)) {

            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, STATUS_ACTIVE);
            
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
            LOGGER.log(Level.SEVERE, "Error al autenticar", e);
            throw new AuthenticateException("Error al autenticar: " + e.getMessage(), e);
        }
        return validateUser;
    }

    @Override
    public boolean existActiveCoordinator() throws OperationException {
        boolean exists = false;
        String userQuery = "SELECT COUNT(*) FROM Usuario WHERE idRol = ? AND estado = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery)) {

            preparedStatement.setInt(1, ROLE_PROFESSOR);
            preparedStatement.setInt(2, STATUS_ACTIVE);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    exists = resultSet.getInt(1) > NO_ROWS_AFFECTED; 
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar coordinador activo", e);
            throw new OperationException("Error al verificar coordinador activo", e);
        }
        return exists;
    }
}
