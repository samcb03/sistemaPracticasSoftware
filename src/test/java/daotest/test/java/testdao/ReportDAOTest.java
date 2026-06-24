package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

class ReportDAOTest {

    private static final int FIRST_REPORT_ID = 6;
    private static final int SECOND_REPORT_ID = 2;
    private static final int SEARCHED_REPORT_ID = 3;
    private static final int GENERATED_ID = 4;
    private static final int ROWS_AFFECTED = 1;

    private static final int PLANNED_HOURS = 10;
    private static final int REAL_HOURS = 8;
    private static final int REPORTED_HOURS = 40;
    private static final float CALIFICATION = 9.0f;

    private static final int FIRST_COLUMN = 1;
    private static final int FINAL_REPORT_TYPE_ID = 2;
    private static final int NON_REPORT_TYPE_ID = 5;
    private static final int REPORT_COUNT = 1;
    private static final int NO_REPORT_COUNT = 0;
    private static final int UPLOADABLE_REPORT_ID = 7;

    private static final String UPLOADABLE_MONTH = "Mayo";
    private static final String REPORT_ID_COLUMN = "idReporte";
    private static final String MONTH_COLUMN = "mes";

    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";
    private static final String FIRST_DESCRIPTION = "Descripcion test 1";
    private static final String FIRST_OBSERVATIONS = "Observacion test 1";
    private static final String FIRST_ACTIVITY = "Actividad test 1";
    private static final String FIRST_STUDENT_ID = "S23013127";

    private static final String SECOND_DESCRIPTION = "Descripcion test 2";
    private static final String SECOND_OBSERVATIONS = "Observacion test 2";
    private static final String SECOND_ACTIVITY = "Actividad test 2";
    private static final String SECOND_STUDENT_ID = "S23013128";

    private static final String MONTH = "Mayo";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";

    private static final String COLUMN_REPORT_ID = "idReporte";
    private static final String COLUMN_DESCRIPTION = "descripcion";
    private static final String COLUMN_OBSERVATIONS = "observaciones";
    private static final String COLUMN_ACTIVITY = "actividad";
    private static final String COLUMN_STUDENT_ID = "matricula";
    private static final String COLUMN_PLANNED_HOURS = "tiempoPlaneado";
    private static final String COLUMN_REAL_HOURS = "tiempoReal";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private ReportDAO reportDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        reportDAO = new ReportDAO();
        Field field = ReportDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(reportDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSingleReport() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_REPORT_ID)).thenReturn(FIRST_REPORT_ID);
        when(resultSet.getString(COLUMN_DESCRIPTION)).thenReturn(FIRST_DESCRIPTION);
        when(resultSet.getString(COLUMN_OBSERVATIONS)).thenReturn(FIRST_OBSERVATIONS);
        when(resultSet.getString(COLUMN_ACTIVITY)).thenReturn(FIRST_ACTIVITY);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(FIRST_STUDENT_ID);
    }

    private void mockResultSetTwoReports() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_REPORT_ID))
            .thenReturn(FIRST_REPORT_ID, SECOND_REPORT_ID);
        when(resultSet.getString(COLUMN_DESCRIPTION))
            .thenReturn(FIRST_DESCRIPTION, SECOND_DESCRIPTION);
        when(resultSet.getString(COLUMN_OBSERVATIONS))
            .thenReturn(FIRST_OBSERVATIONS, SECOND_OBSERVATIONS);
        when(resultSet.getString(COLUMN_ACTIVITY))
            .thenReturn(FIRST_ACTIVITY, SECOND_ACTIVITY);
        when(resultSet.getString(COLUMN_STUDENT_ID))
            .thenReturn(FIRST_STUDENT_ID, SECOND_STUDENT_ID);
    }

    private void mockResultSetPartialReport() throws Exception {
        mockResultSetSingleReport();
        when(resultSet.getInt(COLUMN_PLANNED_HOURS)).thenReturn(PLANNED_HOURS);
        when(resultSet.getInt(COLUMN_REAL_HOURS)).thenReturn(REAL_HOURS);
    }

    private void mockResultSetFinalReport() throws Exception {
        mockResultSetSingleReport();
        when(resultSet.getString("observacionesGenerales")).thenReturn(FIRST_OBSERVATIONS);
    }

    private Report builderFirstReport() {
        Report report = new Report();
        report.setId(FIRST_REPORT_ID);
        report.setDescription(FIRST_DESCRIPTION);
        report.setObservations(FIRST_OBSERVATIONS);
        report.setActivity(FIRST_ACTIVITY);
        report.setStudentId(FIRST_STUDENT_ID);
        return report;
    }

    private Report builderSecondReport() {
        Report report = new Report();
        report.setId(SECOND_REPORT_ID);
        report.setDescription(SECOND_DESCRIPTION);
        report.setObservations(SECOND_OBSERVATIONS);
        report.setActivity(SECOND_ACTIVITY);
        report.setStudentId(SECOND_STUDENT_ID);
        return report;
    }

    private Report builderReport() {
        Report report = builderFirstReport();
        report.setCalification(CALIFICATION);
        return report;
    }

    private PartialReport builderPartialReport() {
        PartialReport report = new PartialReport();
        report.setId(FIRST_REPORT_ID);
        report.setDescription(FIRST_DESCRIPTION);
        report.setObservations(FIRST_OBSERVATIONS);
        report.setActivity(FIRST_ACTIVITY);
        report.setStudentId(FIRST_STUDENT_ID);
        report.setPlannedAdvanceWeek(PLANNED_HOURS);
        report.setRealAdvanceWeek(REAL_HOURS);
        return report;
    }

    private FinalReport builderFinalReport() {
        FinalReport report = new FinalReport();
        report.setId(FIRST_REPORT_ID);
        report.setDescription(FIRST_DESCRIPTION);
        report.setObservations(FIRST_OBSERVATIONS);
        report.setActivity(FIRST_ACTIVITY);
        report.setStudentId(FIRST_STUDENT_ID);
        report.setGeneralObservations(FIRST_OBSERVATIONS);
        return report;
    }

    private MonthlyReport builderMonthlyReport() {
        MonthlyReport report = new MonthlyReport();
        report.setId(FIRST_REPORT_ID);
        report.setDescription(FIRST_DESCRIPTION);
        report.setObservations(FIRST_OBSERVATIONS);
        report.setActivity(FIRST_ACTIVITY);
        report.setStudentId(FIRST_STUDENT_ID);
        report.setMonth(MONTH);
        report.setReportedHours(REPORTED_HOURS);
        return report;
    }

    @Test
    void getAllReports_reportsExist_returnsExpectedReportList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoReports();

        List<Report> expectedReports = List.of(builderFirstReport(), builderSecondReport());

        List<Report> result = reportDAO.getAllReports();

        assertEquals(expectedReports, result);
    }

    @Test
    void getAllReports_noReportsExist_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        List<Report> result = reportDAO.getAllReports();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllReports_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.getAllReports());
    }

    @Test
    void getPartialReportById_reportFound_returnsNonEmpty() throws Exception {
        mockQueryExecution();
        mockResultSetPartialReport();

        Optional<PartialReport> expected = Optional.of(builderPartialReport());

        Optional<PartialReport> result = reportDAO.getPartialReportById(SEARCHED_REPORT_ID);

        assertEquals(expected, result);
    }

    @Test
    void getPartialReportById_reportNotFound_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        Optional<PartialReport> result =
            reportDAO.getPartialReportById(SEARCHED_REPORT_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPartialReportById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.getPartialReportById(SEARCHED_REPORT_ID));
    }

    @Test
    void getFinalReportById_reportFound_returnsNonEmpty() throws Exception {
        mockQueryExecution();
        mockResultSetFinalReport();

        assertEquals(Optional.of(builderFinalReport()),
            reportDAO.getFinalReportById(SEARCHED_REPORT_ID));
    }

    @Test
    void getFinalReportById_reportNotFound_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        Optional<FinalReport> result = reportDAO.getFinalReportById(SEARCHED_REPORT_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getFinalReportById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.getFinalReportById(SEARCHED_REPORT_ID));
    }

    @Test
    void registerPartialReport_validReport_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(GENERATED_ID);

        assertTrue(reportDAO.registerPartialReport(builderPartialReport()));
    }

    @Test
    void registerPartialReport_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.registerPartialReport(builderPartialReport()));
    }

    @Test
    void registerFinalReport_validReport_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(GENERATED_ID);

        assertTrue(reportDAO.registerFinalReport(builderFinalReport()));
    }

    @Test
    void registerFinalReport_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.registerFinalReport(builderFinalReport()));
    }

    @Test
    void registerMonthlyReport_validReport_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(GENERATED_ID);

        assertTrue(reportDAO.registerMonthlyReport(builderMonthlyReport()));
    }

    @Test
    void registerMonthlyReport_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.registerMonthlyReport(builderMonthlyReport()));
    }

    @Test
    void evaluationReport_validReport_executesWithoutException() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        reportDAO.evaluationReport(builderReport());

        assertTrue(true);
    }

    @Test
    void evaluationReport_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(
            new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.evaluationReport(builderReport()));
    }

    @Test
    void hasReportOfType_reportExists_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN)).thenReturn(REPORT_COUNT);

        assertTrue(reportDAO.hasReportOfType(FIRST_STUDENT_ID, FINAL_REPORT_TYPE_ID));
    }

    @Test
    void hasReportOfType_noReport_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN)).thenReturn(NO_REPORT_COUNT);

        assertFalse(reportDAO.hasReportOfType(FIRST_STUDENT_ID, FINAL_REPORT_TYPE_ID));
    }

    @Test
    void hasReportOfType_nonReportType_returnsFalse() throws Exception {
        assertFalse(reportDAO.hasReportOfType(FIRST_STUDENT_ID, NON_REPORT_TYPE_ID));
    }

    @Test
    void hasReportOfType_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.hasReportOfType(FIRST_STUDENT_ID, FINAL_REPORT_TYPE_ID));
    }

    @Test
    void countMonthlyReportsByStudent_reportsExist_returnsCount() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN)).thenReturn(REPORT_COUNT);

        assertEquals(REPORT_COUNT, reportDAO.countMonthlyReportsByStudent(FIRST_STUDENT_ID));
    }

    @Test
    void countMonthlyReportsByStudent_noReports_returnsZero() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN)).thenReturn(NO_REPORT_COUNT);

        assertEquals(NO_REPORT_COUNT, reportDAO.countMonthlyReportsByStudent(FIRST_STUDENT_ID));
    }

    @Test
    void countMonthlyReportsByStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.countMonthlyReportsByStudent(FIRST_STUDENT_ID));
    }

    @Test
    void getUploadableMonthlyReports_pendingReport_returnsReportWithMonth() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt(REPORT_ID_COLUMN)).thenReturn(UPLOADABLE_REPORT_ID);
        when(resultSet.getString(MONTH_COLUMN)).thenReturn(UPLOADABLE_MONTH);

        List<MonthlyReport> uploadableReports = reportDAO.getUploadableMonthlyReports(FIRST_STUDENT_ID);

        assertEquals(UPLOADABLE_MONTH, uploadableReports.get(NO_VALUE).getMonth());
    }

    @Test
    void getUploadableMonthlyReports_noReports_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(reportDAO.getUploadableMonthlyReports(FIRST_STUDENT_ID).isEmpty());
    }

    @Test
    void getUploadableMonthlyReports_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> reportDAO.getUploadableMonthlyReports(FIRST_STUDENT_ID));
    }
}