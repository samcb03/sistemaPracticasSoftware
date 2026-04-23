package src.test.java;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class AutoevaluationDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private AutoevaluationDAO autoevaluationDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getConnection()).thenReturn(connection);
        autoevaluationDAO = new AutoevaluationDAO(connectionManager); 
    }

    private void mockQueryExecution() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetAutoevaluation() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idAutoevaluacion")).thenReturn(1);
        when(resultSet.getString("matricula")).thenReturn("S200123");
        when(resultSet.getInt("participacionProductiva")).thenReturn(5);
        when(resultSet.getInt("conocimientoAplicado")).thenReturn(5);
        when(resultSet.getInt("confianzaEnActividades")).thenReturn(5);
        when(resultSet.getInt("interesEnActividades")).thenReturn(5);
        when(resultSet.getInt("apoyoOrganizacional")).thenReturn(5);
        when(resultSet.getInt("conocimientoDeReglas")).thenReturn(5);
        when(resultSet.getInt("orientacionSupervisor")).thenReturn(5);
        when(resultSet.getInt("seguimientoEfectivo")).thenReturn(5);
        when(resultSet.getInt("alineacionCarrera")).thenReturn(5);
        when(resultSet.getInt("importanciaPracticas")).thenReturn(5);
        when(resultSet.getDouble("puntuacionFinal")).thenReturn(100.0);
    }

    private Autoevaluation buildExpectedAutoevaluation() {
        int[] answers = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        return new Autoevaluation("S200123", answers);
    }

    @Test
    void registerAutoevaluation_successful_returnsTrue() throws Exception {
        mockUpdateExecution(1);

        assertTrue(autoevaluationDAO.registerAutoevaluation(buildExpectedAutoevaluation()));
    }

    @Test
    void registerAutoevaluation_failure_returnsFalse() throws Exception {
        mockUpdateExecution(0);

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.registerAutoevaluation(buildExpectedAutoevaluation())
        );
    }

    @Test
    void registerAutoevaluation_sqlError_returnsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.registerAutoevaluation(buildExpectedAutoevaluation())
        );
    }

    @Test
    void existsByStudent_succesful_returnsTrue() throws Exception {
        mockQueryExecution();
        mockResultSetAutoevaluation();

        assertTrue(autoevaluationDAO.existsByStudent("S200123"));
    }

    @Test
    void existsByStudent_failure_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(autoevaluationDAO.existsByStudent("S200123"));
    }

    @Test
    void existsByStudent_sqlError_returnsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class, () ->
            autoevaluationDAO.existsByStudent("S200123")
        );
    }
}