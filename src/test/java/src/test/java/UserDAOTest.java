package src.test.java;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;

    @Mock
    private Connection databaseConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO();
        Field field = UserDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(userDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void registerUser_successful_returnsGeneratedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        assertEquals(42, userDAO.registerUser(builderUser("Juan", "Pérez", 
            "password12", "Estudiante")));
    }

    @Test
    void registerUser_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            userDAO.registerUser(builderUser("Juan", "Pérez", "password12",
                 "Estudiante"))
        );
        assertTrue(exception.getMessage().contains("No se pudo registrar al usuario"));
    }

    @Test
    void registerUser_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            userDAO.registerUser(builderUser("Juan", "Pérez", "password12", 
                "Estudiante"))
        );
        assertTrue(exception.getMessage().contains("No se pudo registrar al usuario"));
    }

    @Test
    void authenticate_student_returnsStudentUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn("S24013322");

        assertEquals("Estudiante", userDAO.authenticate("S24013322", "password12").getUserType());
    }

    @Test
    void authenticate_administrator_returnsAdministratorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn(null);
        when(resultSet.getString("usuario")).thenReturn("admin01");

        assertEquals("Administrador", userDAO.authenticate("admin01", "password12").getUserType());
    }

    @Test
    void authenticate_coordinator_returnsCoordinatorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn(null);
        when(resultSet.getString("usuario")).thenReturn(null);
        when(resultSet.getString("rol")).thenReturn("Coordinador");

        assertEquals("Coordinador", userDAO.authenticate("C12345", "password13").getUserType());
    }

    @Test
    void authenticate_professor_returnsProfessorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn(null);
        when(resultSet.getString("usuario")).thenReturn(null);
        when(resultSet.getString("rol")).thenReturn("Maestro");

        assertEquals("Profesor", userDAO.authenticate("UV-001", "password12").getUserType());
    }

    @Test
    void authenticate_notFound_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AuthenticateException exception = assertThrows(AuthenticateException.class, () ->
            userDAO.authenticate("S99999", "malpassword")
        );
        assertEquals("Usuario no encontrado, verifique sus datos", exception.getMessage());
    }

    @Test
    void authenticate_sqlError_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException("Fallo"));

        AuthenticateException exception = assertThrows(AuthenticateException.class, () ->
            userDAO.authenticate("S123", "pass")
        );
        assertEquals("No disponible por el momento. Intentelo mas tarde", exception.getMessage());
    }

    private User builderUser(String firstName, String lastName, String password, String userType) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setUserType(userType);
        return user;
    }
}