package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.PracticeDAO;
import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

class PracticeDAOTest {

    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS = 0;
    private static final int GENERATED_KEY_COLUMN = 1;
    private static final int GENERATED_ID = 7;
    private static final int PRACTICE_ID = 1;
    private static final int PROJECT_ID = 3;
    private static final String PRACTICE_NAME = "Desarrollo de modulo";
    private static final String START_DATE = "2026-02-09";
    private static final String FINAL_DATE = "2026-07-03";
    private static final String CONNECTION_ERROR = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private PracticeDAO practiceDAO;

    @BeforeEach
    void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

        practiceDAO = new PracticeDAO();
        Field field = PracticeDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(practiceDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Practice builderPractice() {
        Practice practice = new Practice();
        practice.setIdPractice(PRACTICE_ID);
        practice.setPracticeName(PRACTICE_NAME);
        practice.setStartDate(START_DATE);
        practice.setFinalDate(FINAL_DATE);
        practice.setProjectId(PROJECT_ID);
        return practice;
    }

    @Test
    void registerPractice_successfulWithGeneratedKey_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(GENERATED_KEY_COLUMN)).thenReturn(GENERATED_ID);

        assertTrue(practiceDAO.registerPractice(builderPractice()));
    }

    @Test
    void registerPractice_withoutGeneratedKey_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(practiceDAO.registerPractice(builderPractice()));
    }

    @Test
    void registerPractice_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> practiceDAO.registerPractice(builderPractice()));
    }

    @Test
    void registerPractice_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> practiceDAO.registerPractice(builderPractice()));
    }
}