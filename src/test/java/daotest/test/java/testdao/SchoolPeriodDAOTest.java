package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

class SchoolPeriodDAOTest {

    private static final int VALID_PERIOD_ID = 2;
    private static final int INVALID_PERIOD_ID = 99;
    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS_AFFECTED = 0;

    private static final String EXPECTED_PERIOD_ID_STRING = "1";
    private static final String FIRST_PERIOD_NAME = "Febrero-Julio 2026";
    private static final String SECOND_PERIOD_NAME = "Agosto 2026-Enero 2027";
    private static final String INVALID_PERIOD_NAME = "No Existe";
    private static final String PERIOD_CODE = "202701";
    private static final String START_DATE = "2025-01-20";
    private static final String END_DATE = "2025-06-20";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_PERIOD_ID = "idPeriodoEscolar";
    private static final String COLUMN_START_DATE = "FechaInicio";
    private static final String COLUMN_END_DATE = "FechaFin";
    private static final String VALID_STUDENT_ID = "S24013322";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private SchoolPeriodDAO schoolPeriodDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        schoolPeriodDAO = new SchoolPeriodDAO();
        Field field = SchoolPeriodDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(schoolPeriodDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private SchoolPeriod builderSchoolPeriod() {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setId(VALID_PERIOD_ID);
        schoolPeriod.setName(PERIOD_CODE);
        schoolPeriod.setStartDate(Date.valueOf(START_DATE));
        schoolPeriod.setEndDate(Date.valueOf(END_DATE));
        return schoolPeriod;
    }

    @Test
    void getAllSchoolPeriodsNames_withResults_returnsExpectedList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_PERIOD_NAME, SECOND_PERIOD_NAME);

        assertEquals(List.of(FIRST_PERIOD_NAME, SECOND_PERIOD_NAME),
            schoolPeriodDAO.getAllSchoolPeriodsNames());
    }

    @Test
    void getAllSchoolPeriodsNames_emptyResultSet_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(schoolPeriodDAO.getAllSchoolPeriodsNames().isEmpty());
    }

    @Test
    void getAllSchoolPeriodsNames_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class, () -> schoolPeriodDAO.getAllSchoolPeriodsNames());
    }

    @Test
    void getSchoolPeriodIdByName_found_returnsExpectedOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_PERIOD_ID)).thenReturn(EXPECTED_PERIOD_ID_STRING);

        assertEquals(Optional.of(EXPECTED_PERIOD_ID_STRING),    
            schoolPeriodDAO.getSchoolPeriodIdByName(FIRST_PERIOD_NAME));
    }

    @Test
    void getSchoolPeriodIdByName_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodIdByName(INVALID_PERIOD_NAME));
    }

    @Test
    void getSchoolPeriodIdByName_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodIdByName(FIRST_PERIOD_NAME));
    }

    @Test
    void getSchoolPeriodByStudentId_found_returnsExpectedPeriod() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_PERIOD_ID)).thenReturn(VALID_PERIOD_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PERIOD_CODE);
        when(resultSet.getDate(COLUMN_START_DATE)).thenReturn(Date.valueOf(START_DATE));
        when(resultSet.getDate(COLUMN_END_DATE)).thenReturn(Date.valueOf(END_DATE));

        Optional<SchoolPeriod> result = schoolPeriodDAO.getSchoolPeriodByStudentId(VALID_STUDENT_ID);
        assertEquals(Optional.of(builderSchoolPeriod()), result);
    }

    @Test
    void getSchoolPeriodByStudentId_notFound_returnsEmptyOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(schoolPeriodDAO.getSchoolPeriodByStudentId(VALID_STUDENT_ID).isEmpty());
    }

    @Test
    void getSchoolPeriodByStudentId_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.getSchoolPeriodByStudentId(VALID_STUDENT_ID));
    }

    @Test
    void registerSchoolPeriod_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void registerSchoolPeriod_noRowsAffected_throwsOperationException() throws SQLException {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void registerSchoolPeriod_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.registerSchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_noRowsAffected_throwsOperationException() throws SQLException {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void modifySchoolPeriod_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.modifySchoolPeriod(builderSchoolPeriod()));
    }

    @Test
    void existsSchoolPeriod_exists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        assertTrue(schoolPeriodDAO.existsSchoolPeriod(VALID_PERIOD_ID));
    }

    @Test
    void existsSchoolPeriod_notExists_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(schoolPeriodDAO.existsSchoolPeriod(INVALID_PERIOD_ID));
    }

    @Test
    void existsSchoolPeriod_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> schoolPeriodDAO.existsSchoolPeriod(VALID_PERIOD_ID));
    }
}