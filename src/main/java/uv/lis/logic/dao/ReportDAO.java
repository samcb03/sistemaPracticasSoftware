package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IReportDAO;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

public class ReportDAO implements IReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    private static final String DATABASE_CONNECTION_ERROR = "Error de conexión con la base de datos";
    private static final String GET_REPORTS_ERROR = "No se pudo obtener los reportes. Inténtelo más tarde";
    private static final String GET_PARTIAL_ERROR = "Error al obtener el reporte parcial";
    private static final String REGISTER_PARTIAL_ERROR = "Error al registrar el reporte parcial";
    private static final String MODIFY_PARTIAL_ERROR = "Error al modificar el reporte parcial. Inténtelo más tarde";
    private static final String GET_FINAL_ERROR = "Error al obtener el reporte final";
    private static final String REGISTER_FINAL_ERROR = "Error al registrar el reporte final";
    private static final String MODIFY_FINAL_ERROR = "Error al modificar el reporte final";
    private static final String REGISTER_MONTHLY_ERROR = "Error al registrar reporte mensual";
    private static final String MONTHLY_REPORT_ACTIVITY = "Reporte Mensual";

    private final MySQLConnectionManager connectionManager;

    public ReportDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ReportDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Report> getAllReports() throws OperationException {
        List<Report> reports = new ArrayList<>();
        String reportQuery = "SELECT idReporte, descripcion, observaciones, actividad, matricula FROM Reporte";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapReport(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(GET_REPORTS_ERROR, e);
        }
        return reports;
    }

    @Override
    public Optional<PartialReport> getPartialReportById(int idPartialReport)
            throws OperationException {
        Optional<PartialReport> validReport = Optional.empty();
        String reportQuery = "SELECT r.idReporte, r.descripcion, r.observaciones, r.actividad, "
                           + "r.matricula, rp.tiempoPlaneado, rp.tiempoReal "
                           + "FROM ReporteParcial rp "
                           + "INNER JOIN Reporte r ON rp.idReporte = r.idReporte "
                           + "WHERE rp.idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, idPartialReport);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    validReport = Optional.of(mapPartialReport(resultSet));
                } else {
                    LOGGER.log(Level.INFO,
                        "No se encontró un reporte parcial con id {0}", idPartialReport);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(GET_PARTIAL_ERROR, e);
        }
        return validReport;
    }

    @Override
    public boolean registerPartialReport(PartialReport partialReport) throws OperationException {
        boolean isRegistered = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);
            try {
                insertReportRow(databaseConnection, partialReport);
                isRegistered = insertPartialDetail(databaseConnection, partialReport);
                commitOrRollback(databaseConnection, isRegistered);
            } catch (SQLException innerException) {
                databaseConnection.rollback();
                throw innerException;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, REGISTER_PARTIAL_ERROR, e);
            throw new OperationException(REGISTER_PARTIAL_ERROR, e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyPartialReport(PartialReport partialReport) throws OperationException {
        boolean isModified = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);
            try {
                updateReportRow(databaseConnection, partialReport);
                isModified = updatePartialDetail(databaseConnection, partialReport);
                commitOrRollback(databaseConnection, isModified);
            } catch (SQLException innerException) {
                databaseConnection.rollback();
                throw innerException;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, MODIFY_PARTIAL_ERROR, e);
            throw new OperationException(MODIFY_PARTIAL_ERROR, e);
        }
        return isModified;
    }

    @Override
    public Optional<FinalReport> getFinalReportById(int idFinalReport)
            throws OperationException {
        Optional<FinalReport> validReport = Optional.empty();
        String reportQuery = "SELECT r.idReporte, r.descripcion, r.observaciones, r.actividad, "
                           + "r.matricula, rf.porcentajeAvance, rf.ResultadoEntregable "
                           + "FROM ReporteFinal rf "
                           + "INNER JOIN Reporte r ON rf.idReporte = r.idReporte "
                           + "WHERE rf.idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, idFinalReport);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    validReport = Optional.of(mapFinalReport(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(GET_FINAL_ERROR, e);
        }
        return validReport;
    }

    @Override
    public boolean registerFinalReport(FinalReport finalReport) throws OperationException {
        boolean isRegistered = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);
            try {
                insertReportRow(databaseConnection, finalReport);
                isRegistered = insertFinalDetail(databaseConnection, finalReport);
                commitOrRollback(databaseConnection, isRegistered);
            } catch (SQLException innerException) {
                databaseConnection.rollback();
                throw innerException;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, REGISTER_FINAL_ERROR, e);
            throw new OperationException(REGISTER_FINAL_ERROR, e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyFinalReport(FinalReport finalReport) throws OperationException {
        boolean isModified = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);
            try {
                updateReportRow(databaseConnection, finalReport);
                isModified = updateFinalDetail(databaseConnection, finalReport);
                commitOrRollback(databaseConnection, isModified);
            } catch (SQLException innerException) {
                databaseConnection.rollback();
                throw innerException;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, MODIFY_FINAL_ERROR, e);
            throw new OperationException(MODIFY_FINAL_ERROR, e);
        }
        return isModified;
    }

    @Override
    public boolean registerMonthlyReport(MonthlyReport monthlyReport)
            throws OperationException {
        boolean isRegistered = false;

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);
            try {
                insertReportRow(databaseConnection, monthlyReport);
                isRegistered = insertMonthlyDetail(databaseConnection, monthlyReport);
                commitOrRollback(databaseConnection, isRegistered);
            } catch (SQLException innerException) {
                databaseConnection.rollback();
                throw innerException;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, REGISTER_MONTHLY_ERROR, e);
            throw new OperationException(REGISTER_MONTHLY_ERROR, e);
        }
        return isRegistered;
    }

    @Override
    public void evaluationReport(Report report) throws OperationException {
        String reportQuery = "UPDATE Reporte SET calificacion = ? WHERE idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setFloat(1, report.getCalification());
            preparedStatement.setInt(2, report.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(DATABASE_CONNECTION_ERROR, e);
        }
    }

    private Report mapReport(ResultSet resultSet) throws SQLException {
        Report report = new Report();
        report.setId(resultSet.getInt("idReporte"));
        report.setDescription(resultSet.getString("descripcion"));
        report.setObservations(resultSet.getString("observaciones"));
        report.setActivity(resultSet.getString("actividad"));
        report.setStudentId(resultSet.getString("matricula"));
        return report;
    }

    private PartialReport mapPartialReport(ResultSet resultSet) throws SQLException {
        PartialReport report = new PartialReport();
        report.setId(resultSet.getInt("idReporte"));
        report.setDescription(resultSet.getString("descripcion"));
        report.setObservations(resultSet.getString("observaciones"));
        report.setActivity(resultSet.getString("actividad"));
        report.setStudentId(resultSet.getString("matricula"));
        report.setPlannedAdvanceWeek(resultSet.getInt("tiempoPlaneado"));
        report.setRealAdvanceWeek(resultSet.getInt("tiempoReal"));
        return report;
    }

    private FinalReport mapFinalReport(ResultSet resultSet) throws SQLException {
        FinalReport report = new FinalReport();
        report.setId(resultSet.getInt("idReporte"));
        report.setDescription(resultSet.getString("descripcion"));
        report.setObservations(resultSet.getString("observaciones"));
        report.setActivity(resultSet.getString("actividad"));
        report.setStudentId(resultSet.getString("matricula"));
        report.getFirstActivity().setAdvancePercentage(
            String.valueOf(resultSet.getInt("porcentajeAvance")));
        report.getFirstDeliverable().setResult(resultSet.getString("ResultadoEntregable"));
        return report;
    }

    private void insertReportRow(Connection databaseConnection, Report report) throws SQLException {
        String reportQuery = "INSERT INTO Reporte (descripcion, observaciones, actividad, matricula) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery, 
            PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, report.getDescription());
            preparedStatement.setString(2, report.getObservations());
            preparedStatement.setString(3, report.getActivity());
            preparedStatement.setString(4, report.getStudentId());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setId(generatedKeys.getInt(1)); 
                } else {
                    throw new SQLException("No se pudo obtener el ID del reporte generado.");
                }
            }
        }
    }

    private void updateReportRow(Connection databaseConnection, Report report)
            throws SQLException {
        String reportQuery = "UPDATE Reporte SET descripcion = ?, observaciones = ?, "
                           + "actividad = ?, matricula = ? WHERE idReporte = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setString(1, report.getDescription());
            preparedStatement.setString(2, report.getObservations());
            preparedStatement.setString(3, report.getActivity());
            preparedStatement.setString(4, report.getStudentId());
            preparedStatement.setInt(5, report.getId());
            preparedStatement.executeUpdate();
        }
    }

    private boolean insertPartialDetail(Connection databaseConnection,
            PartialReport partialReport) throws SQLException {
        boolean isInserted = false;
        String reportQuery = "INSERT INTO ReporteParcial (idReporte, tiempoPlaneado, tiempoReal) "
                           + "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, partialReport.getId());
            preparedStatement.setInt(2, partialReport.getPlannedAdvanceWeek());
            preparedStatement.setInt(3, partialReport.getRealAdvanceWeek());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInserted = true;
            }
        }
        return isInserted;
    }

    private boolean updatePartialDetail(Connection databaseConnection, 
            PartialReport partialReport) throws SQLException {
        boolean isUpdated = false;
        String reportQuery = "UPDATE ReporteParcial SET tiempoPlaneado = ?, tiempoReal = ? "
                           + "WHERE idReporte = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, partialReport.getPlannedAdvanceWeek());
            preparedStatement.setInt(2, partialReport.getRealAdvanceWeek());
            preparedStatement.setInt(3, partialReport.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    private boolean insertFinalDetail(Connection databaseConnection, FinalReport finalReport) throws SQLException {
        boolean isInserted = false;
        int advancePercentage = parseAdvancePercentage( finalReport.getFirstActivity().getAdvancePercentage());
        String deliverableResult = finalReport.getFirstDeliverable().getResult();
        String reportQuery = "INSERT INTO ReporteFinal (idReporte, porcentajeAvance, ResultadoEntregable) "
                           + "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, finalReport.getId());
            preparedStatement.setInt(2, advancePercentage);
            preparedStatement.setString(3, deliverableResult);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInserted = true;
            }
        }
        return isInserted;
    }

    private boolean updateFinalDetail(Connection databaseConnection, FinalReport finalReport) throws SQLException {
        boolean isUpdated = false;
        int advancePercentage = parseAdvancePercentage(finalReport.getFirstActivity().getAdvancePercentage());
        String deliverableResult = finalReport.getFirstDeliverable().getResult();
        String reportQuery = "UPDATE ReporteFinal SET porcentajeAvance = ?, ResultadoEntregable = ? "
                           + "WHERE idReporte = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, advancePercentage);
            preparedStatement.setString(2, deliverableResult);
            preparedStatement.setInt(3, finalReport.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    private boolean insertMonthlyDetail(Connection databaseConnection, 
            MonthlyReport monthlyReport) throws SQLException {
        boolean isInserted = false;
        String reportQuery = "INSERT INTO ReporteMensual (idReporte, mes, horasReportadas) "
                           + "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {

            preparedStatement.setInt(1, monthlyReport.getId());
            preparedStatement.setString(2, monthlyReport.getMonth());
            preparedStatement.setInt(3, monthlyReport.getReportedHours());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInserted = true;
            }
        }
        return isInserted;
    }

    private int parseAdvancePercentage(String rawValue) {
        int parsedValue = 0;

        if (rawValue != null && !rawValue.isBlank()) {
            try {
                parsedValue = Integer.parseInt(rawValue.trim());
            } catch (NumberFormatException numberFormatException) {
                LOGGER.log(Level.WARNING, "Valor de avance no numérico: {0}", rawValue);
            }
        }
        return parsedValue;
    }

    private void commitOrRollback(Connection databaseConnection, boolean shouldCommit) throws SQLException {
        if (shouldCommit) {
            databaseConnection.commit();
        } else {
            databaseConnection.rollback();
        }
    }
}