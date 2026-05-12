package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.PasswordHasher;


@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO();
        Field field = UserDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(userDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private User builderUser(String firstName, String lastName, String password, int roleId) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setRoleId(roleId);
        return user;
    }

    @Test
    void registerUser_successful_returnsGeneratedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        assertEquals(42, userDAO.registerUser(builderUser("Juan", "Pérez", 
            "password12", 1)));
    }

    @Test
    void registerUser_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class, () ->
            userDAO.registerUser(builderUser("Juan", "Pérez", "password12", 1)));
    }

    @Test
    void registerUser_noGeneratedKeyReturned_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () ->
            userDAO.registerUser(builderUser("Juan", "Pérez", "password12", 1)));
    }

    @Test
    void registerUser_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            userDAO.registerUser(builderUser("Juan", "Pérez", "password12", 1)));
    }

    @Test
    void authenticate_student_returnsStudentUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn("hashedPassword");
        when(resultSet.getInt("idUsuario")).thenReturn(1); 
        when(resultSet.getInt("idRol")).thenReturn(1);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("Gom_Ram002", 
                "hashedPassword"))
                .thenReturn(true);



            Optional<User> result = userDAO.authenticate("gom03@gmail.com","Gom_Ram002");   
            assertEquals(1,result.get().getRoleId());
        }
    }

    @Test
    void authenticate_administrator_returnsAdministratorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn("hashedPassword");
        when(resultSet.getInt("idUsuario")).thenReturn(1);
        when(resultSet.getInt("idRol")).thenReturn(4);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("Demos25_rask", 
                "hashedPassword"))
                .thenReturn(true);

                Optional<User> result = userDAO.authenticate("GutJac_03@gmail.com", "Demos25_rask");
                assertEquals(4,result.get().getRoleId());
        }
    }

    @Test
    void authenticate_coordinator_returnsCoordinatorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn("hashedPassword");
        when(resultSet.getInt("idUsuario")).thenReturn(1);
        when(resultSet.getInt("idRol")).thenReturn(3);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("Demos25_rask", 
                "hashedPassword"))
                .thenReturn(true);

                Optional<User> result = userDAO.authenticate("carRG@gmail.com", "Demos25_rask");
                assertEquals(3,result.get().getRoleId());
        }
    }

    @Test
    void authenticate_professor_returnsProfessorUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn("hashedPassword");
        when(resultSet.getInt("idUsuario")).thenReturn(1); 
        when(resultSet.getInt("idRol")).thenReturn(2);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("Bet@04ga", 
                "hashedPassword"))
                .thenReturn(true);

            Optional<User> result = userDAO.authenticate("LopHern@hotmail.com", "Bet@04ga");
            assertEquals(2, result.get().getRoleId());
        }
    }

    @Test
    void authenticate_notFound_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(AuthenticateException.class, () ->
            userDAO.authenticate("S99999", "malpassword"));
    }

    @Test
    void authenticate_wrongPassword_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn("hashedPassword");

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("wrongPassword", 
                "hashedPassword"))
                .thenReturn(false);

            
                assertThrows(AuthenticateException.class, () ->
                userDAO.authenticate("gom03@gmail.com", "wrongPassword"));
        }
    }

    @Test
    void authenticate_sqlError_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException("Fallo"));

        assertThrows(AuthenticateException.class, () ->
            userDAO.authenticate("S123", "pass"));
    }
}