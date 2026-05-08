package src.test.java;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;


@ExtendWith(MockitoExtension.class)
class AutoevaluationDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private AutoevaluationDAO autoevaluationDAO;

    @BeforeEach
    void setUp() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        autoevaluationDAO = new AutoevaluationDAO(connectionManager);
    }

    private Autoevaluation buildAutoevaluation() {
        int[] answers = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        return new Autoevaluation("S200123", answers);
    }

    @Test
    void registerAutoevaluation_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void registerAutoevaluation_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void registerAutoevaluation_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.registerAutoevaluation(buildAutoevaluation()));
    }

    @Test
    void existsByStudent_exists_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(autoevaluationDAO.existsByStudent("S200123"));
    }

    @Test
    void existsByStudent_notExists_returnsFalse() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(autoevaluationDAO.existsByStudent("S200123"));
    }

    @Test
    void existsByStudent_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.existsByStudent("S200123"));
    }
}