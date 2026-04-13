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
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) { 
                    generatedId = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return generatedId;
    }

    @Override
    public User authenticate(String identification, String password) {
        User userAuthenticate = null;
        String userQuery = "SELECT u.idUsuario, u.contraseña, a.matricula, p.numeroPersonal,ad.usuario, p.rol "
            + "FROM Usuario u "
            + "LEFT JOIN Alumno a ON u.idUsuario = a.idUsuario "
            + "LEFT JOIN Profesor p ON u.idUsuario = p.idUsuario "
            + "LEFT JOIN Administrador ad ON u.idUsuario = ad.idUsuario "
            + "WHERE (a.matricula = ? OR p.numeroPersonal = ? OR ad.usuario = ?) "
            + "AND u.contraseña = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(userQuery)){
             
                preparedStatement.setString(1, identification);
                preparedStatement.setString(2, identification);
                preparedStatement.setString(3, identification);
                preparedStatement.setString(4, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()) {
                        userAuthenticate = new User();
                        userAuthenticate.setIdentification(identification);

                        if(resultSet.getString("matricula") != null) {
                            userAuthenticate.setUserType("Estudiante"); 
                        } else if(resultSet.getString("usuario") != null) {
                            userAuthenticate.setUserType("Administrador"); 
                        } else {
                            String rol = resultSet.getString("rol");
                            if("Coordinador".equals(rol)) {
                                userAuthenticate.setUserType("Coordinador");
                            } else {
                                userAuthenticate.setUserType("Profesor");
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error de autenticacion SQL", e);
            }   
            return userAuthenticate;
    }
}