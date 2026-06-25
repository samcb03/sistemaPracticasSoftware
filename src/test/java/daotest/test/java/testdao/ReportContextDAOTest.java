package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.exceptions.OperationException;

class ReportContextDAOTest {

    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";
    private static final String CONNECTION_ERROR = "Fallo de conexión";

    private static final String STUDENT_ID = "S24013305";
    private static final String ACTIVITY_NAME = "Análisis de requerimientos";
    private static final String MONTH = "Enero";
    private static final int MONTH_NUMBER = 1;
    private static final int YEAR = 2025;

    private static final String STUDENT_FIRST_NAME = "Ana";
    private static final String STUDENT_LAST_NAME = "López";
    private static final String PROFESSOR_FIRST_NAME = "Carlos";
    private static final String PROFESSOR_LAST_NAME = "Ramírez";
    private static final String ACADEMIC_FIRST_NAME = "Luis";
    private static final String ACADEMIC_LAST_NAME = "Torres";
    private static final String COORDINATOR_NAME = "María Soto";
    private static final String NRC = "12345";
    private static final String PERIOD = "Enero-Junio 2025";
    private static final String PROJECT_NAME = "Sistema de Gestión";
    private static final String PROJECT_OBJECTIVE = "Automatizar procesos administrativos";
    private static final String PROJECT_METHODOLOGY = "SCRUM";
    private static final String ORGANIZATION = "Empresa XYZ";
    private static final String SUPERVISOR_NAME = "Roberto Díaz";
    private static final String SECTION = "A";
    private static final String MONTH_LABEL = "Enero";

    private static final String ACTIVITY_DESCRIPTION = "Levantamiento de requerimientos del sistema";
    private static final int ACTIVITY_ID = 1;
    private static final int HOURS_REPORTED = 8;
    private static final int TOTAL_HOURS = 40;
    private static final int REPORT_NUMBER = 3;
    private static final int PROJECT_ID = 7;
    private static final int REPORT_ID = 15;
    private static final int COUNT_ONE = 1;
    private static final int COUNT_ZERO = 0;
    private static final int FIRST_COLUMN_INDEX = 1;
    private static final String INITIAL_TOTAL_HOURS = "0";
    private static final String TOTAL_HOURS_STRING = "40";

    private static final String COLUMN_STUDENT_NAME = "nombreAlumno";
    private static final String COLUMN_STUDENT_LASTNAME = "apellidosAlumno";
    private static final String COLUMN_PROFESSOR_NAME = "nombreProfesor";
    private static final String COLUMN_PROFESSOR_LASTNAME = "apellidosProfesor";
    private static final String COLUMN_ACADEMIC_NAME = "nombreAcademico";
    private static final String COLUMN_ACADEMIC_LASTNAME = "apellidosAcademico";
    private static final String COLUMN_NRC = "nrc";
    private static final String COLUMN_PERIOD = "periodo";
    private static final String COLUMN_PRINCIPAL_PERIOD = "periodoPrincipal";
    private static final String COLUMN_PROJECT_NAME = "nombreProyecto";
    private static final String COLUMN_PROJECT_OBJECTIVE = "objetivoProyecto";
    private static final String COLUMN_METHODOLOGY = "metodologiaProyecto";
    private static final String COLUMN_ORGANIZATION = "organizacion";
    private static final String COLUMN_RESPONSIBLE_NAME = "nombreResponsable";
    private static final String COLUMN_COORDINATOR_NAME = "nombreCoordinador";
    private static final String COLUMN_MONTH = "mes";
    private static final String COLUMN_REPORT_NUMBER = "numeroReporte";
    private static final String COLUMN_ID_PROJECT = "idProyecto";
    private static final String COLUMN_ID_REPORT = "idReporte";
    private static final String COLUMN_SECTION = "seccion";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_ID_ACTIVITY = "idActividad";
    private static final String COLUMN_ACTIVITY_NAME = "nombreActividad";
    private static final String COLUMN_ACTIVITY_DESCRIPTION = "descripcionActividad";
    private static final String COLUMN_START_DATE = "FechaInicio";
    private static final String COLUMN_END_DATE = "FechaFin";
    private static final String COLUMN_HOURS = "horas";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ReportContextDAO reportContextDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        reportContextDAO = new ReportContextDAO();
        Field field = ReportContextDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(reportContextDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void getFinalReportContextByStudentId_found_returnsExpectedFinalReport() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_STUDENT_NAME)).thenReturn(STUDENT_FIRST_NAME);
        when(resultSet.getString(COLUMN_STUDENT_LASTNAME)).thenReturn(STUDENT_LAST_NAME);
        when(resultSet.getString(COLUMN_PROFESSOR_NAME)).thenReturn(PROFESSOR_FIRST_NAME);
        when(resultSet.getString(COLUMN_PROFESSOR_LASTNAME)).thenReturn(PROFESSOR_LAST_NAME);
        when(resultSet.getString(COLUMN_NRC)).thenReturn(NRC);
        when(resultSet.getString(COLUMN_PERIOD)).thenReturn(PERIOD);
        when(resultSet.getString(COLUMN_PROJECT_NAME)).thenReturn(PROJECT_NAME);
        when(resultSet.getString(COLUMN_PROJECT_OBJECTIVE)).thenReturn(PROJECT_OBJECTIVE);
        when(resultSet.getString(COLUMN_METHODOLOGY)).thenReturn(PROJECT_METHODOLOGY);
        when(resultSet.getString(COLUMN_ORGANIZATION)).thenReturn(ORGANIZATION);

        FinalReport result = reportContextDAO.getFinalReportContextByStudentId(STUDENT_ID);

        assertEquals(STUDENT_FIRST_NAME + " " + STUDENT_LAST_NAME, result.getStudentName());
    }

    @Test
    void getFinalReportContextByStudentId_notFound_throwsOperationException() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> reportContextDAO.getFinalReportContextByStudentId(STUDENT_ID));
    }

    @Test
    void getFinalReportContextByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getFinalReportContextByStudentId(STUDENT_ID));
    }

    @Test
    void getPartialReportContextByStudentId_found_returnsExpectedPartialReport() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_STUDENT_NAME)).thenReturn(STUDENT_FIRST_NAME);
        when(resultSet.getString(COLUMN_STUDENT_LASTNAME)).thenReturn(STUDENT_LAST_NAME);
        when(resultSet.getString(COLUMN_PROFESSOR_NAME)).thenReturn(PROFESSOR_FIRST_NAME);
        when(resultSet.getString(COLUMN_PROFESSOR_LASTNAME)).thenReturn(PROFESSOR_LAST_NAME);
        when(resultSet.getString(COLUMN_NRC)).thenReturn(NRC);
        when(resultSet.getString(COLUMN_PERIOD)).thenReturn(PERIOD);
        when(resultSet.getString(COLUMN_PROJECT_NAME)).thenReturn(PROJECT_NAME);
        when(resultSet.getString(COLUMN_PROJECT_OBJECTIVE)).thenReturn(PROJECT_OBJECTIVE);
        when(resultSet.getString(COLUMN_METHODOLOGY)).thenReturn(PROJECT_METHODOLOGY);
        when(resultSet.getString(COLUMN_ORGANIZATION)).thenReturn(ORGANIZATION);
        when(resultSet.getString(COLUMN_RESPONSIBLE_NAME)).thenReturn(SUPERVISOR_NAME);

        PartialReport result = reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID);

        assertEquals(STUDENT_FIRST_NAME + " " + STUDENT_LAST_NAME, result.getStudentName());
    }

    @Test
    void getPartialReportContextByStudentId_notFound_throwsOperationException() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID));
    }

    @Test
    void getPartialReportContextByStudentId_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getPartialReportContextByStudentId(STUDENT_ID));
    }

    @Test
    void getTotalReportedHoursByStudentId_withHours_returnsExpectedTotal() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(TOTAL_HOURS);

        assertEquals(TOTAL_HOURS_STRING,
            reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID));
    }

    @Test
    void getTotalReportedHoursByStudentId_noRows_returnsInitialValue() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertEquals(INITIAL_TOTAL_HOURS,
            reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID));
    }

    @Test
    void getTotalReportedHoursByStudentId_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getTotalReportedHoursByStudentId(STUDENT_ID));
    }

    @Test
    void getMonthlyReportData_found_returnsExpectedMonthlyReport() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_STUDENT_NAME)).thenReturn(STUDENT_FIRST_NAME);
        when(resultSet.getString(COLUMN_STUDENT_LASTNAME)).thenReturn(STUDENT_LAST_NAME);
        when(resultSet.getString(COLUMN_ACADEMIC_NAME)).thenReturn(ACADEMIC_FIRST_NAME);
        when(resultSet.getString(COLUMN_ACADEMIC_LASTNAME)).thenReturn(ACADEMIC_LAST_NAME);
        when(resultSet.getString(COLUMN_COORDINATOR_NAME)).thenReturn(COORDINATOR_NAME);
        when(resultSet.getString(COLUMN_NRC)).thenReturn(NRC);
        when(resultSet.getString(COLUMN_PRINCIPAL_PERIOD)).thenReturn(PERIOD);
        when(resultSet.getString(COLUMN_MONTH)).thenReturn(MONTH_LABEL);
        when(resultSet.getInt(COLUMN_REPORT_NUMBER)).thenReturn(REPORT_NUMBER);
        when(resultSet.getInt(COLUMN_ID_PROJECT)).thenReturn(PROJECT_ID);
        when(resultSet.getInt(COLUMN_ID_REPORT)).thenReturn(REPORT_ID);
        when(resultSet.getString(COLUMN_RESPONSIBLE_NAME)).thenReturn(SUPERVISOR_NAME);
        when(resultSet.getString(COLUMN_SECTION)).thenReturn(SECTION);

        MonthlyReport result = reportContextDAO.getMonthlyReportData(STUDENT_ID);

        assertEquals(STUDENT_FIRST_NAME + " " + STUDENT_LAST_NAME, result.getStudentName());
    }

    @Test
    void getMonthlyReportData_nullSection_setsDefaultSectionText() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_STUDENT_NAME)).thenReturn(STUDENT_FIRST_NAME);
        when(resultSet.getString(COLUMN_STUDENT_LASTNAME)).thenReturn(STUDENT_LAST_NAME);
        when(resultSet.getString(COLUMN_ACADEMIC_NAME)).thenReturn(ACADEMIC_FIRST_NAME);
        when(resultSet.getString(COLUMN_ACADEMIC_LASTNAME)).thenReturn(ACADEMIC_LAST_NAME);
        when(resultSet.getString(COLUMN_COORDINATOR_NAME)).thenReturn(COORDINATOR_NAME);
        when(resultSet.getString(COLUMN_NRC)).thenReturn(NRC);
        when(resultSet.getString(COLUMN_PRINCIPAL_PERIOD)).thenReturn(PERIOD);
        when(resultSet.getString(COLUMN_MONTH)).thenReturn(MONTH_LABEL);
        when(resultSet.getInt(COLUMN_REPORT_NUMBER)).thenReturn(REPORT_NUMBER);
        when(resultSet.getInt(COLUMN_ID_PROJECT)).thenReturn(PROJECT_ID);
        when(resultSet.getInt(COLUMN_ID_REPORT)).thenReturn(REPORT_ID);
        when(resultSet.getString(COLUMN_RESPONSIBLE_NAME)).thenReturn(SUPERVISOR_NAME);
        when(resultSet.getString(COLUMN_SECTION)).thenReturn(null);

        MonthlyReport result = reportContextDAO.getMonthlyReportData(STUDENT_ID);

        assertEquals("Sin sección", result.getSection());
    }

    @Test
    void getMonthlyReportData_notFound_throwsOperationException() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> reportContextDAO.getMonthlyReportData(STUDENT_ID));
    }

    @Test
    void getMonthlyReportData_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getMonthlyReportData(STUDENT_ID));
    }

    @Test
    void getRecordedActivities_withResults_returnsExpectedList() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt(COLUMN_ID_ACTIVITY)).thenReturn(ACTIVITY_ID);
        when(resultSet.getString(COLUMN_ACTIVITY_NAME)).thenReturn(ACTIVITY_NAME);
        when(resultSet.getString(COLUMN_ACTIVITY_DESCRIPTION)).thenReturn(ACTIVITY_DESCRIPTION);
        when(resultSet.getDate(COLUMN_START_DATE))
            .thenReturn(Date.valueOf(LocalDate.of(YEAR, MONTH_NUMBER, 1)));
        when(resultSet.getDate(COLUMN_END_DATE))
            .thenReturn(Date.valueOf(LocalDate.of(YEAR, MONTH_NUMBER, 31)));

        assertEquals(1, reportContextDAO.getRecordedActivities(STUDENT_ID).size());
    }

    @Test
    void getRecordedActivities_emptyResultSet_returnsEmptyList() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertTrue(reportContextDAO.getRecordedActivities(STUDENT_ID).isEmpty());
    }

    @Test
    void getRecordedActivities_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getRecordedActivities(STUDENT_ID));
    }

    @Test
    void getRecordedActivitiesByMonth_withResults_returnsExpectedList() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(COLUMN_ACTIVITY_NAME)).thenReturn(ACTIVITY_NAME);
        when(resultSet.getString(COLUMN_ACTIVITY_DESCRIPTION)).thenReturn(ACTIVITY_DESCRIPTION);
        when(resultSet.getInt(COLUMN_HOURS)).thenReturn(HOURS_REPORTED);

        List<Activity> result = reportContextDAO.getRecordedActivitiesByMonth(
            STUDENT_ID, MONTH_NUMBER, YEAR);

        assertEquals(1, result.size());
    }

    @Test
    void getRecordedActivitiesByMonth_emptyResultSet_returnsEmptyList() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertTrue(reportContextDAO.getRecordedActivitiesByMonth(
            STUDENT_ID, MONTH_NUMBER, YEAR).isEmpty());
    }

    @Test
    void getRecordedActivitiesByMonth_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getRecordedActivitiesByMonth(STUDENT_ID, MONTH_NUMBER, YEAR));
    }

    @Test
    void getActivityByName_found_returnsExpectedActivity() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_ACTIVITY_NAME)).thenReturn(ACTIVITY_NAME);

        Optional<Activity> result = reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME);

        assertEquals(ACTIVITY_NAME, result.get().getName());
    }

    @Test
    void getActivityByName_notFound_returnsEmptyActivity() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Optional<Activity> result = reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME);

        assertTrue(result.isEmpty());
    }

    @Test
    void getActivityByName_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getActivityByName(STUDENT_ID, ACTIVITY_NAME));
    }

    @Test
    void getSumOfReportedHours_withHours_returnsExpectedTotal() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(TOTAL_HOURS);

        assertEquals(TOTAL_HOURS,
            reportContextDAO.getSumOfReportedHours(STUDENT_ID, MONTH_NUMBER, YEAR));
    }

    @Test
    void getSumOfReportedHours_noRows_returnsZero() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertEquals(0,
            reportContextDAO.getSumOfReportedHours(STUDENT_ID, MONTH_NUMBER, YEAR));
    }

    @Test
    void getSumOfReportedHours_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.getSumOfReportedHours(STUDENT_ID, MONTH_NUMBER, YEAR));
    }


    @Test
    void hasReportAlreadyBeenGenerated_withExistingReport_returnsTrue() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(COUNT_ONE);

        assertTrue(reportContextDAO.hasReportAlreadyBeenGenerated(STUDENT_ID, MONTH));
    }

    @Test
    void hasReportAlreadyBeenGenerated_withNoExistingReport_returnsFalse() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(COUNT_ZERO);

        assertTrue(!reportContextDAO.hasReportAlreadyBeenGenerated(STUDENT_ID, MONTH));
    }

    @Test
    void hasReportAlreadyBeenGenerated_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> reportContextDAO.hasReportAlreadyBeenGenerated(STUDENT_ID, MONTH));
    }
}