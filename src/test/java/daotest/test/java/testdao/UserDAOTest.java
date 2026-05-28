package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    private static final int EXPECTED_GENERATED_ID = 3;
    private static final int DEFAULT_ROLE_ID = 1;
    private static final String VALID_EMAIL = "gom03@gmail.com";
    private static final String VALID_PASSWORD = "Gom_Ram002";
    private static final String HASHED_PASSWORD = "hashedPassword";
    private static final int PROFESSOR_ROLE_ID = 2;
    private static final int COORDINATOR_ROLE_ID = 3;
    private static final int ADMIN_ROLE_ID = 4;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new UserDAO(connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private User builderUser() {
        User user = new User();
        user.setFirstName("Juan");
        user.setLastName("Pérez");
        user.setPassword("password12");
        user.setRoleId(DEFAULT_ROLE_ID);
        return user;
    }

    @Test
    void registerUser_successful_returnsGeneratedId() throws Exception {
        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString()))
                .thenReturn(HASHED_PASSWORD);
            when(databaseConnection.prepareStatement(anyString(), anyInt()))
                .thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(EXPECTED_GENERATED_ID);

            assertEquals(EXPECTED_GENERATED_ID, userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_noRowsAffected_throwsOperationException() throws Exception {
        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString()))
                .thenReturn(HASHED_PASSWORD);
            when(databaseConnection.prepareStatement(anyString(), anyInt()))
                .thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0);

            assertThrows(OperationException.class, () -> userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_noGeneratedKeyReturned_throwsOperationException() throws Exception {
        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString()))
                .thenReturn(HASHED_PASSWORD);
            when(databaseConnection.prepareStatement(anyString(), anyInt()))
                .thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            assertThrows(OperationException.class, () -> userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_sqlError_throwsOperationException() throws Exception {
        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString()))
                .thenReturn(HASHED_PASSWORD);
            when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

            assertThrows(OperationException.class, () -> userDAO.registerUser(builderUser()));
        }
    }

    private static Stream<Arguments> provideUsersByRole() {
        return Stream.of(
            arguments(1, DEFAULT_ROLE_ID),
            arguments(2, PROFESSOR_ROLE_ID),
            arguments(3, COORDINATOR_ROLE_ID),
            arguments(4, ADMIN_ROLE_ID));
    }

    @ParameterizedTest
    @MethodSource("provideUsersByRole")
    void authenticate_validCredentialsByRole_returnsUser(int userId, int roleId) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn(HASHED_PASSWORD);
        when(resultSet.getInt("idUsuario")).thenReturn(userId);
        when(resultSet.getInt("idRol")).thenReturn(roleId);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword(VALID_PASSWORD, HASHED_PASSWORD))
                .thenReturn(true);
            Optional<User> result = userDAO.authenticate(VALID_EMAIL, VALID_PASSWORD);

            assertTrue(result.isPresent());
        }
    }

    @Test
    void authenticate_notFound_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(AuthenticateException.class,
            () -> userDAO.authenticate("S99999", "malpassword"));
    }

    @Test
    void authenticate_wrongPassword_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("contraseña")).thenReturn(HASHED_PASSWORD);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword("wrongPassword", HASHED_PASSWORD))
                .thenReturn(false);

            assertThrows(AuthenticateException.class,
                () -> userDAO.authenticate(VALID_EMAIL, "wrongPassword"));
        }
    }

    @Test
    void authenticate_sqlError_throwsAuthenticateException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException("Fallo"));

        assertThrows(AuthenticateException.class, () -> userDAO.authenticate("S123", 
            "pass"));
    }
}