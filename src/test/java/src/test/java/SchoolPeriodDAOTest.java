package src.test.java;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class SchoolPeriodDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private SchoolPeriodDAO schoolPeriodDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getConnection()).thenReturn(connection);
        schoolPeriodDAO = new SchoolPeriodDAO(connectionManager);
    }

    private void mockQueryExecution() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSchoolPeriod() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idPeriodoEscolar")).thenReturn(1);
        when(resultSet.getDate("FechaInicio")).thenReturn(Date.valueOf("2025-01-20"));
        when(resultSet.getDate("FechaFin")).thenReturn(Date.valueOf("2025-06-20"));
    }

    private SchoolPeriod buildExpectedSchoolPeriod() {
        SchoolPeriod expected = new SchoolPeriod();
        expected.setId(1);
        expected.setStartDate(Date.valueOf("2025-01-20"));
        expected.setEndDate(Date.valueOf("2025-06-20"));
        return expected;
    }

    @Test
    void testGetSchoolPeriodByIdSuccess() throws Exception {
        mockQueryExecution();
        mockResultSetSchoolPeriod();

        SchoolPeriod expectedSchoolPeriod = buildExpectedSchoolPeriod();

        assertEquals(expectedSchoolPeriod, schoolPeriodDAO.getSchoolPeriodbyId(1));
    }

    @Test
    void testGetSchoolPeriodByIdNotFound() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodbyId(99));
    }

    @Test
    void testGetSchoolPeriodByIdDatabaseError() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodbyId(1));
    }

    @Test
    void testRegisterSchoolPeriodSuccess() throws Exception {
        mockUpdateExecution(1);

        SchoolPeriod schoolPeriodToRegister = buildExpectedSchoolPeriod();

        assertTrue(schoolPeriodDAO.registerSchoolPeriod(schoolPeriodToRegister));
    }

    @Test
    void testRegisterSchoolPeriodFailed() throws Exception {
        mockUpdateExecution(0);

        SchoolPeriod schoolPeriodToRegister = buildExpectedSchoolPeriod();

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(schoolPeriodToRegister));
    }

    @Test
    void testRegisterSchoolPeriodDatabaseError() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        SchoolPeriod schoolPeriodToRegister = buildExpectedSchoolPeriod();

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(schoolPeriodToRegister));
    }

    @Test
    void testModifySchoolPeriodSuccess() throws Exception {
        mockUpdateExecution(1);

        SchoolPeriod schoolPeriodToModify = buildExpectedSchoolPeriod();

        assertTrue(schoolPeriodDAO.modifySchoolPeriod(schoolPeriodToModify));
    }

    @Test
    void testModifySchoolPeriodFailed() throws Exception {
        mockUpdateExecution(0);

        SchoolPeriod schoolPeriodToModify = buildExpectedSchoolPeriod();

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(schoolPeriodToModify));
    }

    @Test
    void testModifySchoolPeriodDatabaseError() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        SchoolPeriod schoolPeriodToModify = buildExpectedSchoolPeriod();

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(schoolPeriodToModify));
    }

    @Test
    void testExistsSchoolPeriodTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        assertTrue(schoolPeriodDAO.existsSchoolPeriod(1));
    }

    @Test
    void testExistsSchoolPeriodFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(schoolPeriodDAO.existsSchoolPeriod(99));
    }

    @Test
    void testExistsSchoolPeriodDatabaseError() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.existsSchoolPeriod(1));
    }
}