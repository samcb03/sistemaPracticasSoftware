package daotest.test.java.testdao;

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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class SchoolPeriodDAOTest {

    private static final int VALID_PERIOD_ID = 1;
    private static final int INVALID_PERIOD_ID = 99;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final String EXPECTED_PERIOD_ID_STRING = "1";
    private static final String VALID_PERIOD_NAME = "Febrero-Julio 2026";
    private static final String SECOND_PERIOD_NAME = "Agosto 2026-Enero 2027";
    private static final String INVALID_PERIOD_NAME = "NoExiste";
    private static final String START_DATE = "2025-01-20";
    private static final String END_DATE = "2025-06-20";
    private static final String CONNECTION_ERROR = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private SchoolPeriodDAO schoolPeriodDAO;

    @BeforeEach
    void setUp() throws Exception {
        schoolPeriodDAO = new SchoolPeriodDAO(connectionManager);
        when(connectionManager.getConnection()).thenReturn(connection);
    }

    private SchoolPeriod builderSchoolPeriod() {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setId(VALID_PERIOD_ID);
        schoolPeriod.setStartDate(Date.valueOf(START_DATE));
        schoolPeriod.setEndDate(Date.valueOf(END_DATE));
        return schoolPeriod;
    }

    @Test
    void getAllSchoolPeriodsNames_withResults_returnsPopulatedList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn(VALID_PERIOD_NAME, SECOND_PERIOD_NAME);

        assertEquals(EXPECTED_LIST_SIZE, schoolPeriodDAO.getAllSchoolPeriodsNames().size());
    }

    @Test
    void getAllSchoolPeriodsNames_emptyResultSet_returnsEmptyList() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(schoolPeriodDAO.getAllSchoolPeriodsNames().isEmpty());
    }

    @Test
    void getAllSchoolPeriodsNames_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getAllSchoolPeriodsNames());
    }

    @Test
    void getSchoolPeriodIdByName_found_returnsPeriodId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("idPeriodoEscolar")).thenReturn(EXPECTED_PERIOD_ID_STRING);

        Optional<String> result = schoolPeriodDAO.getSchoolPeriodIdByName(VALID_PERIOD_NAME);

        assertEquals(EXPECTED_PERIOD_ID_STRING, result.get());
    }

    @Test
    void getSchoolPeriodIdByName_notFound_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodIdByName(INVALID_PERIOD_NAME));
    }

    @Test
    void getSchoolPeriodIdByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodIdByName(VALID_PERIOD_NAME));
    }

    @Test
    void registerSchoolPeriod_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void registerSchoolPeriod_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void registerSchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void existsSchoolPeriod_exists_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(schoolPeriodDAO.existsSchoolPeriod(VALID_PERIOD_ID));
    }

    @Test
    void existsSchoolPeriod_notExists_returnsFalse() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(schoolPeriodDAO.existsSchoolPeriod(INVALID_PERIOD_ID));
    }

    @Test
    void existsSchoolPeriod_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.existsSchoolPeriod(VALID_PERIOD_ID));
    }
}