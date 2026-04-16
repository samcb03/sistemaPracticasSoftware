package src.test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserDAOTest {

    private MySQLConnectionManager connectionManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        connectionManager = mock(MySQLConnectionManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        userDAO = new UserDAO();

        Field field = userDAO.getClass().getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(userDAO, connectionManager);
        
        when(connectionManager.getConnection()).thenReturn(connection);
    }

    @Test
    void testRegisterUserSuccess() throws Exception {

        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1); 
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42); 

        User newUser = new User();
        newUser.setFirstName("Juan");
        newUser.setLastName("Perez");
        newUser.setPassword("password123");
        newUser.setUserType("Estudiante");

        int resultId = userDAO.registerUser(newUser);

        assertEquals(42, resultId, "El ID generado debería ser 42");
    }

    @Test
    void testRegisterUserFailNoRows() throws Exception {
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        User newUser = new User();
        
        OperationException exception = assertThrows(OperationException.class, () -> {
            userDAO.registerUser(newUser);
        });
        
        assertEquals("No se pudo registrar al usuario. Intentelo mas tarde", exception.getMessage());
    }

    @Test
    void testAuthenticateStudentSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn("S200123"); 
        
        User authenticatedUser = userDAO.authenticate("S200123", "password123");

        assertNotNull(authenticatedUser);
        assertEquals("S200123", authenticatedUser.getIdentification());
        assertEquals("Estudiante", authenticatedUser.getUserType());
    }

    @Test
    void testAuthenticateCoordinatorSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn(null);
        when(resultSet.getString("usuario")).thenReturn(null);
        when(resultSet.getString("rol")).thenReturn("Coordinador"); 
      
        User authenticatedUser = userDAO.authenticate("C12345", "password123");

        assertNotNull(authenticatedUser);
        assertEquals("C12345", authenticatedUser.getIdentification());
        assertEquals("Coordinador", authenticatedUser.getUserType());
    }

    @Test
    void testAuthenticateNotFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AuthenticateException exception = assertThrows(AuthenticateException.class, () -> {
            userDAO.authenticate("S99999", "malpassword");
        });
        
        assertEquals("Usuario no encontrado, verifique sus datos", exception.getMessage());
    }
    
    @Test
    void testAuthenticateThrowsSqlException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Conexión perdida"));

        AuthenticateException exception = assertThrows(AuthenticateException.class, () -> {
            userDAO.authenticate("S123", "pass");
        });
        
        assertEquals("No disponible por el momento. Intentelo mas tarde", exception.getMessage());
    }
}