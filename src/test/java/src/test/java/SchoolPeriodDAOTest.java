package src.test.java;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


@ExtendWith(MockitoExtension.class)
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
        schoolPeriodDAO = new SchoolPeriodDAO(connectionManager);
        when(connectionManager.getConnection()).thenReturn(connection);
    }

    @Test
    void getAllSchoolPeriodsNames_withResults_returnsPopulatedList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn("2025-1", "2025-2");

        ArrayList<String> result = schoolPeriodDAO.getAllSchoolPeriodsNames();

        assertEquals(2, result.size());
        assertEquals("2025-1", result.get(0));
        assertEquals("2025-2", result.get(1));
    }

    @Test
    void getAllSchoolPeriodsNames_emptyResultSet_returnsEmptyList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ArrayList<String> result = schoolPeriodDAO.getAllSchoolPeriodsNames();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSchoolPeriodsNames_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.getAllSchoolPeriodsNames()
        );
        assertTrue(exception.getMessage().contains("No se pudieron obtener los periodos"));
    }

    @Test
    void getSchoolPeriodIdByName_found_returnsPeriodId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("idPeriodoEscolar")).thenReturn("1");

        assertEquals("1", schoolPeriodDAO.getSchoolPeriodIdByName("2025-1"));
    }

    @Test
    void getSchoolPeriodIdByName_notFound_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.getSchoolPeriodIdByName("NoExiste")
        );
        assertTrue(exception.getMessage().contains("No se encontró el periodo escolar: NoExiste"));
    }

    @Test
    void getSchoolPeriodIdByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.getSchoolPeriodIdByName("2025-1")
        );
        assertTrue(exception.getMessage().contains("Error al obtener el ID del periodo escolar"));
    }

    @Test
    void registerSchoolPeriod_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(schoolPeriodDAO.registerSchoolPeriod(buildSchoolPeriod()));
    }

    @Test
    void registerSchoolPeriod_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.registerSchoolPeriod(buildSchoolPeriod())
        );
        assertTrue(exception.getMessage().contains("No se pudo registrar el periodo escolar"));
    }

    @Test
    void registerSchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.registerSchoolPeriod(buildSchoolPeriod())
        );
        assertTrue(exception.getMessage().contains("Error al registrar el periodo escolar"));
    }

    @Test
    void modifySchoolPeriod_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(schoolPeriodDAO.modifySchoolPeriod(buildSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.modifySchoolPeriod(buildSchoolPeriod())
        );
        assertTrue(exception.getMessage().contains("No se pudo modificar el periodo escolar"));
    }

    @Test
    void modifySchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.modifySchoolPeriod(buildSchoolPeriod())
        );
        assertTrue(exception.getMessage().contains("Error al modificar el periodo escolar"));
    }

    @Test
    void existsSchoolPeriod_exists_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(schoolPeriodDAO.existsSchoolPeriod(1));
    }

    @Test
    void existsSchoolPeriod_notExists_returnsFalse() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(schoolPeriodDAO.existsSchoolPeriod(99));
    }

    @Test
    void existsSchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            schoolPeriodDAO.existsSchoolPeriod(1)
        );
        assertTrue(exception.getMessage().contains("Error al validar el periodo escolar"));
    }

    private SchoolPeriod buildSchoolPeriod() {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setId(1);
        schoolPeriod.setStartDate(Date.valueOf("2025-01-20"));
        schoolPeriod.setEndDate(Date.valueOf("2025-06-20"));
        return schoolPeriod;
    }
}