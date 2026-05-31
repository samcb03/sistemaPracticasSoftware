package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class ReportDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private ReportDAO reportDAO;

    @BeforeEach
    void setUp() throws Exception {
        reportDAO = new ReportDAO();
        Field field = ReportDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(reportDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void getReports_reportsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idReporte")).thenReturn(1);
        when(resultSet.getString("descripcion")).thenReturn("Descripcion test");
        when(resultSet.getString("observaciones")).thenReturn("Observacion test");
        when(resultSet.getString("actividad")).thenReturn("Actividad test");
        when(resultSet.getString("matricula")).thenReturn("S23013127");

        List<Report> result = reportDAO.getReports();

        assertFalse(result.isEmpty());
    }

    @Test
    void getReports_noReports_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Report> result = reportDAO.getReports();

        assertTrue(result.isEmpty());
    }

    @Test
    void getReports_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class, () -> reportDAO.getReports());
    }

    @Test
    void getPartialReportById_reportFound_returnsNonEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idReporte")).thenReturn(1);
        when(resultSet.getString("descripcion")).thenReturn("Descripcion");
        when(resultSet.getString("observaciones")).thenReturn("Observacion");
        when(resultSet.getString("actividad")).thenReturn("Actividad");
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getInt("tiempoPlaneado")).thenReturn(10);
        when(resultSet.getInt("tiempoReal")).thenReturn(8);

        Optional<PartialReport> result = reportDAO.getPartialReportById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void getPartialReportById_reportNotFound_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<PartialReport> result = reportDAO.getPartialReportById(99);

        assertFalse(result.isPresent());
    }

    @Test
    void getPartialReportById_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.getPartialReportById(1));
    }

    @Test
    void getFinalReportById_reportFound_returnsNonEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idReporte")).thenReturn(1);
        when(resultSet.getString("descripcion")).thenReturn("Descripcion");
        when(resultSet.getString("observaciones")).thenReturn("Observacion");
        when(resultSet.getString("actividad")).thenReturn("Actividad");
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getInt("porcentajeAvance")).thenReturn(80);
        when(resultSet.getString("ResultadoEntregable")).thenReturn("Entregable test");

        Optional<FinalReport> result = reportDAO.getFinalReportById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void getFinalReportById_reportNotFound_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<FinalReport> result = reportDAO.getFinalReportById(99);

        assertFalse(result.isPresent());
    }

    @Test
    void getFinalReportById_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.getFinalReportById(1));
    }

    @Test
    void registerPartialReport_validReport_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1);

        boolean result = reportDAO.registerPartialReport(buildPartialReport());

        assertTrue(result);
    }

    @Test
    void registerPartialReport_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.registerPartialReport(buildPartialReport()));
    }

    @Test
    void registerFinalReport_validReport_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1);

        boolean result = reportDAO.registerFinalReport(buildFinalReport());

        assertTrue(result);
    }

    @Test
    void registerFinalReport_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.registerFinalReport(buildFinalReport()));
    }

    @Test
    void registerMonthlyReport_validReport_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1);

        boolean result = reportDAO.registerMonthlyReport(buildMonthlyReport());

        assertTrue(result);
    }

    @Test
    void registerMonthlyReport_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.registerMonthlyReport(buildMonthlyReport()));
    }

    @Test
    void evaluationReport_validReport_executesWithoutException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        reportDAO.evaluationReport(buildReport());

        assertTrue(true);
    }

    @Test
    void evaluationReport_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> reportDAO.evaluationReport(buildReport()));
    }

    private Report buildReport() {
        Report report = new Report();
        report.setId(1);
        report.setDescription("Descripcion test");
        report.setObservations("Observacion test");
        report.setActivity("Actividad test");
        report.setStudentId("S23013127");
        report.setCalification(9.0f);
        return report;
    }

    private PartialReport buildPartialReport() {
        PartialReport report = new PartialReport();
        report.setDescription("Descripcion parcial");
        report.setObservations("Observacion parcial");
        report.setActivity("Actividad parcial");
        report.setStudentId("S23013127");
        report.setPlannedAdvanceWeek(10);
        report.setRealAdvanceWeek(8);
        return report;
    }

    private FinalReport buildFinalReport() {
        FinalReport report = new FinalReport();
        report.setDescription("Descripcion final");
        report.setObservations("Observacion final");
        report.setActivity("Actividad final");
        report.setStudentId("S23013127");
        return report;
    }

    private MonthlyReport buildMonthlyReport() {
        MonthlyReport report = new MonthlyReport();
        report.setDescription("Descripcion mensual");
        report.setObservations("Observacion mensual");
        report.setActivity("Actividad mensual");
        report.setStudentId("S23013127");
        report.setMonth("Mayo");
        report.setReportedHours(40);
        return report;
    }
}