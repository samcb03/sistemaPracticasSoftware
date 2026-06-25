package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_VALUE;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ActivityDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;

class ActivityDAOTest {

    private static final int FIRST_ACTIVITY_ID = 2;
    private static final int SECOND_ACTIVITY_ID = 4;
    private static final int SEARCHED_ACTIVITY_ID = 3;
    private static final int FIRST_PROJECT_ID = 10;
    private static final int SECOND_PROJECT_ID = 11;
    private static final int ROWS_AFFECTED = 1;
    private static final int GENERATED_ID = 5;
    private static final int TOTAL_ACTIVITY_HOURS = 120;
    private static final int NO_HOURS = 0;
    private static final int FIRST_PARAMETER = 1;

    private static final String FIRST_ACTIVITY_NAME = "Actividad 1";
    private static final String SECOND_ACTIVITY_NAME = "Actividad 2";
    private static final String FIRST_ACTIVITY_DESCRIPTION = "Descripción 1";
    private static final String SECOND_ACTIVITY_DESCRIPTION = "Descripción 2";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String STUDENT_ID = "S22011223";

    private static final LocalDate FIRST_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate FIRST_END_DATE = LocalDate.of(2024, 12, 31);
    private static final LocalDate SECOND_START_DATE = LocalDate.of(2024, 2, 1);
    private static final LocalDate SECOND_END_DATE = LocalDate.of(2024, 11, 30);

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ActivityDAO activityDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        activityDAO = new ActivityDAO();
        Field field = ActivityDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(activityDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSingleActivity() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idActividad")).thenReturn(FIRST_ACTIVITY_ID);
        when(resultSet.getString("nombreActividad")).thenReturn(FIRST_ACTIVITY_NAME);
        when(resultSet.getString("descripcionActividad")).thenReturn(FIRST_ACTIVITY_DESCRIPTION);
        when(resultSet.getObject("FechaInicio", LocalDate.class)).thenReturn(FIRST_START_DATE);
        when(resultSet.getObject("FechaFin", LocalDate.class)).thenReturn(FIRST_END_DATE);
        when(resultSet.getInt("idProyecto")).thenReturn(FIRST_PROJECT_ID);
    }

    private void mockResultSetTwoActivities() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("idActividad")).thenReturn(FIRST_ACTIVITY_ID, SECOND_ACTIVITY_ID);
        when(resultSet.getString("nombreActividad"))
            .thenReturn(FIRST_ACTIVITY_NAME, SECOND_ACTIVITY_NAME);
        when(resultSet.getString("descripcionActividad"))
            .thenReturn(FIRST_ACTIVITY_DESCRIPTION, SECOND_ACTIVITY_DESCRIPTION);
        when(resultSet.getObject("FechaInicio", LocalDate.class))
            .thenReturn(FIRST_START_DATE, SECOND_START_DATE);
        when(resultSet.getObject("FechaFin", LocalDate.class))
            .thenReturn(FIRST_END_DATE, SECOND_END_DATE);
        when(resultSet.getInt("idProyecto")).thenReturn(FIRST_PROJECT_ID, SECOND_PROJECT_ID);
    }

    private Activity builderFirstActivity() {
        return new Activity(FIRST_ACTIVITY_ID, FIRST_ACTIVITY_NAME, FIRST_ACTIVITY_DESCRIPTION,
            FIRST_START_DATE, FIRST_END_DATE, FIRST_PROJECT_ID);
    }

    private Activity builderSecondActivity() {
        return new Activity(SECOND_ACTIVITY_ID, SECOND_ACTIVITY_NAME, SECOND_ACTIVITY_DESCRIPTION,
            SECOND_START_DATE, SECOND_END_DATE, SECOND_PROJECT_ID);
    }

    private List<Activity> builderExpectedActivities() {
        return List.of(builderFirstActivity(), builderSecondActivity());
    }

    @Test
    void getAllActivities_successful_returnsActivityList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoActivities();
        List<Activity> expectedActivities = builderExpectedActivities();

        assertEquals(expectedActivities, activityDAO.getAllActivities());
    }

    @Test
    void getAllActivities_emptyResult_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getAllActivities().isEmpty());
    }

    @Test
    void getAllActivities_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class, () -> activityDAO.getAllActivities());
    }

    @Test
    void getActivityById_successful_returnsActivity() throws Exception {
        mockQueryExecution();
        mockResultSetSingleActivity();
        Optional<Activity> expectedActivity = Optional.of(builderFirstActivity());

        assertEquals(expectedActivity, activityDAO.getActivityById(SEARCHED_ACTIVITY_ID));
    }

    @Test
    void getActivityById_emptyResult_returnsEmptyOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getActivityById(SEARCHED_ACTIVITY_ID).isEmpty());
    }

    @Test
    void getActivityById_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> activityDAO.getActivityById(SEARCHED_ACTIVITY_ID));
    }

    @Test
    void registerActivity_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_PARAMETER)).thenReturn(GENERATED_ID);

        assertTrue(activityDAO.registerActivity(builderFirstActivity()));
    }

    @Test
    void registerActivity_noRowsAffected_throwsOperationException() throws SQLException {
        mockUpdateExecution(NO_VALUE);

        assertThrows(OperationException.class,
            () -> activityDAO.registerActivity(builderFirstActivity()));
    }

    @Test
    void registerActivity_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> activityDAO.registerActivity(builderFirstActivity()));
    }

    @Test
    void modifyActivity_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(activityDAO.modifyActivity(builderFirstActivity()));
    }

    @Test
    void modifyActivity_noRowsAffected_throwsOperationException() throws SQLException {
        mockUpdateExecution(NO_VALUE);

        assertThrows(OperationException.class,
            () -> activityDAO.modifyActivity(builderFirstActivity()));
    }

    @Test
    void modifyActivity_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> activityDAO.modifyActivity(builderFirstActivity()));
    }

    @Test
    void getActivitiesByStudentId_successful_returnsActivityList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoActivities();
        when(resultSet.getString("matricula")).thenReturn(STUDENT_ID, STUDENT_ID);
        List<Activity> expectedActivities = builderExpectedActivities();

        assertEquals(expectedActivities, activityDAO.getActivitiesByStudentId(STUDENT_ID));
    }

    @Test
    void getActivitiesByStudentId_emptyResult_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(activityDAO.getActivitiesByStudentId(STUDENT_ID).isEmpty());
    }

    @Test
    void getActivitiesByStudentId_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> activityDAO.getActivitiesByStudentId(STUDENT_ID));
    }

    @Test
    void getTotalActivityHoursByStudent_successful_returnsTotalHours() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(TOTAL_ACTIVITY_HOURS);

        assertEquals(TOTAL_ACTIVITY_HOURS,
            activityDAO.getTotalActivityHoursByStudent(STUDENT_ID));
    }

    @Test
    void getTotalActivityHoursByStudent_emptyResult_returnsZero() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertEquals(NO_HOURS, activityDAO.getTotalActivityHoursByStudent(STUDENT_ID));
    }

    @Test
    void getTotalActivityHoursByStudent_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> activityDAO.getTotalActivityHoursByStudent(STUDENT_ID));
    }
}