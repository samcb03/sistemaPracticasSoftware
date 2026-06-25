package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_VALUE;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.PasswordHasher;

class UserDAOTest {

    private static final int EXPECTED_GENERATED_ID = 7;
    private static final int DEFAULT_ROLE_ID = 8;
    private static final int PROFESSOR_ROLE_ID = 2;
    private static final int COORDINATOR_ROLE_ID = 3;
    private static final int ADMIN_ROLE_ID = 4;
    private static final int FIRST_VALUE = 1;
    private static final int DEFAULT_ID = 99;
    private static final int STUDENT_USER_ID = 10;
    private static final int PROFESSOR_USER_ID = 12;
    private static final int COORDINATOR_USER_ID = 13;
    private static final int ADMIN_USER_ID = 14;
       
    private static final boolean IS_ACTIVE = true;
    private static final boolean EMAIL_AUTHENTICATION_DISABLED = false;

    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Pérez";
    private static final String DEFAULT_PASSWORD = "password12";
    private static final String DEFAULT_EMAIL = "juan@example.com";
    private static final String VALID_EMAIL = "gom03@gmail.com";
    private static final String VALID_PASSWORD = "Gom_Ram002";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String HASHED_PASSWORD = "hashedPassword";
    private static final String INVALID_EMAIL = "S99999";
    private static final String INVALID_PASSWORD = "malpassword";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_PASSWORD = "contraseña";
    private static final String COLUMN_USER_ID = "idUsuario";
    private static final String COLUMN_ROLE_ID = "idRol";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        userDAO = new UserDAO(connectionManager);
        Field field = UserDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(userDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecutionWithGeneratedKey(int rowsAffected, int generatedId) throws SQLException {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(generatedId);
    }

    private void mockResultSetAuthenticate(int userId, int roleId) throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(resultSet.getInt(COLUMN_USER_ID)).thenReturn(userId);
        when(resultSet.getInt(COLUMN_ROLE_ID)).thenReturn(roleId);
    }

    private User builderUser() {
        User user = new User(DEFAULT_ID, FIRST_NAME, LAST_NAME, DEFAULT_PASSWORD, DEFAULT_EMAIL, DEFAULT_ROLE_ID, 
            IS_ACTIVE, IS_ACTIVE);
        return user;
    }

    private static Stream<Arguments> provideUsersByRole() {
        Stream<Arguments> arguments = Stream.of(
            arguments(STUDENT_USER_ID, DEFAULT_ROLE_ID),
            arguments(PROFESSOR_USER_ID, PROFESSOR_ROLE_ID),
            arguments(COORDINATOR_USER_ID, COORDINATOR_ROLE_ID),
            arguments(ADMIN_USER_ID, ADMIN_ROLE_ID));
        return arguments;
    }

    @Test
    void registerUser_successful_returnsGeneratedId() throws Exception {
        mockUpdateExecutionWithGeneratedKey(FIRST_VALUE, EXPECTED_GENERATED_ID);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString())).thenReturn(HASHED_PASSWORD);

            assertEquals(EXPECTED_GENERATED_ID, userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_noRowsAffected_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_VALUE);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString())).thenReturn(HASHED_PASSWORD);

            assertThrows(OperationException.class,
                () -> userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_noGeneratedKeyReturned_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(FIRST_VALUE);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString())).thenReturn(HASHED_PASSWORD);

            assertThrows(OperationException.class,
                () -> userDAO.registerUser(builderUser()));
        }
    }

    @Test
    void registerUser_sqlError_throwsOperationException() throws SQLException {
        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hashPassword(anyString())).thenReturn(HASHED_PASSWORD);
            when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

            assertThrows(OperationException.class,
                () -> userDAO.registerUser(builderUser()));
        }
    }

    @ParameterizedTest
    @MethodSource("provideUsersByRole")
    void authenticate_validCredentialsByRole_returnsUser(int userId, int roleId) throws Exception {
        mockResultSetAuthenticate(userId, roleId);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword(VALID_PASSWORD, HASHED_PASSWORD)).
                thenReturn(true);

            assertTrue(userDAO.authenticate(VALID_EMAIL, VALID_PASSWORD).isPresent());
        }
    }

   @Test
    void authenticate_notFound_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(userDAO.authenticate(INVALID_EMAIL, INVALID_PASSWORD).isEmpty());
    }

    @Test
    void authenticate_wrongPassword_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_PASSWORD)).thenReturn(HASHED_PASSWORD);

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword(WRONG_PASSWORD, HASHED_PASSWORD))
                .thenReturn(false);

            assertTrue(userDAO.authenticate(VALID_EMAIL, WRONG_PASSWORD).isEmpty());
        }
    }

    @Test
    void authenticate_sqlError_throwsAuthenticateException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(AuthenticateException.class,
            () -> userDAO.authenticate(INVALID_EMAIL, INVALID_PASSWORD));
    }

    @Test
    void existActiveCoordinator_coordinatorExists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(FIRST_VALUE);

        assertTrue(userDAO.existActiveCoordinator());
    }

    @Test
    void existActiveCoordinator_noCoordinatorExists_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(NO_VALUE);

        assertFalse(userDAO.existActiveCoordinator());
    }

    @Test
    void existActiveCoordinator_resultSetEmpty_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(userDAO.existActiveCoordinator());
    }

    @Test
    void existActiveCoordinator_sqlError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> userDAO.existActiveCoordinator());
    }

    @Test
    void updateEmailAuthenticationPreference_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(FIRST_VALUE);

        assertTrue(userDAO.updateEmailAuthenticationPreference(DEFAULT_ID,
            EMAIL_AUTHENTICATION_DISABLED));
    }

    @Test
    void updateEmailAuthenticationPreference_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_VALUE);

        assertFalse(userDAO.updateEmailAuthenticationPreference(DEFAULT_ID,
            EMAIL_AUTHENTICATION_DISABLED));
    }

    @Test
    void updateEmailAuthenticationPreference_sqlError_throwsOperationException() throws SQLException {
        when(databaseConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> userDAO.updateEmailAuthenticationPreference(DEFAULT_ID,
                EMAIL_AUTHENTICATION_DISABLED));
    }
}