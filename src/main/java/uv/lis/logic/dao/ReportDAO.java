package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ReportDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public List<Report> getReports() throws OperationException {
        List<Report> reports = new ArrayList<>();
        String reportQuery = "SELECT idReporte, descripcion, observaciones, actividad, matricula FROM Reporte";
        
        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Report report = new Report();
                report.setId(resultSet.getInt("idReporte"));
                report.setDescription(resultSet.getString("descripcion"));
                report.setObservations(resultSet.getString("observaciones"));
                report.setActivity(resultSet.getString("actividad"));
                report.setStudentId(resultSet.getString("matricula"));
                reports.add(report);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo obtener los reportes. Intentelo mas tarde", null);
        }

        return reports;
    }

    @Override
    public PartialReport getPartialReportById(int idPartialReport) throws OperationException {
        PartialReport report = null;
        String partialReportQuery = "SELECT r.idReporte, r.descripcion, r.observaciones, r.actividad, r.matricula, " 
            + "rp.tiempoPlaneado, rp.tiempoReal FROM ReporteParcial rp " 
            + "INNER JOIN Reporte r ON rp.idReporte = r.idReporte " 
            + "WHERE rp.idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(partialReportQuery)) {

            preparedStatement.setInt(1, idPartialReport);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    report = new PartialReport();
                    report.setId(resultSet.getInt("idReporte"));
                    report.setDescription(resultSet.getString("descripcion"));
                    report.setObservations(resultSet.getString("observaciones"));
                    report.setActivity(resultSet.getString("actividad"));
                    report.setStudentId(resultSet.getString("matricula"));
                    report.setPlannedTime(resultSet.getInt("tiempoPlaneado"));
                    report.setRealTime(resultSet.getInt("tiempoReal"));
                } else {
                    LOGGER.log(Level.INFO, "No se encontró un reporte parcial con id {0}.", idPartialReport);
                    throw new OperationException("No se encontró un reporte parcial con id: " + idPartialReport, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener el reporte parcial", null);
        }

        return report;
    }

    @Override
    public boolean registerPartialReport(PartialReport partialReport) throws OperationException {
        boolean isRegistered = false;

        String reportQuery = "INSERT INTO Reporte (idReporte, descripcion, observaciones, actividad, matricula) "
            + "VALUES (?, ?, ?, ?, ?)";

        String partialQuery = "INSERT INTO ReporteParcial (idReporte, tiempoPlaneado, tiempoReal) " 
            + "VALUES (?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try (PreparedStatement preparedStatementReport = databaseConnection.prepareStatement(reportQuery)) {
                preparedStatementReport.setInt(1, partialReport.getId());
                preparedStatementReport.setString(2, partialReport.getDescription());
                preparedStatementReport.setString(3, partialReport.getObservations());
                preparedStatementReport.setString(4, partialReport.getActivity());
                preparedStatementReport.setString(5, partialReport.getStudentId());

                preparedStatementReport.executeUpdate();

                try (PreparedStatement psPartial = databaseConnection.prepareStatement(partialQuery)) {
                    psPartial.setInt(1, partialReport.getId());
                    psPartial.setInt(2, partialReport.getPlannedTime());
                    psPartial.setInt(3, partialReport.getRealTime());

                    psPartial.executeUpdate();
                }

                databaseConnection.commit();
                isRegistered = true;

            } catch (SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al registrar reporte parcial", e);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar reporte parcial", e);
        }

        return isRegistered;
    }

    public boolean registerMonthlyReport(MonthlyReport monthlyReport) throws OperationException {
        boolean isRegistered = false;

        String reportQuery = "INSERT INTO Reporte (idReporte, descripcion, observaciones, actividad, matricula) " +
                "VALUES (?, ?, ?, ?, ?)";
        String monthlyQuery = "INSERT INTO ReporteMensual (idReporte, mes, horasReportadas) " +
                "VALUES (?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try (PreparedStatement preparedStatementReport = databaseConnection.prepareStatement(reportQuery)) {
                preparedStatementReport.setInt(1, monthlyReport.getId());
                preparedStatementReport.setString(2, monthlyReport.getDescription());
                preparedStatementReport.setString(3, monthlyReport.getObservations());
                preparedStatementReport.setString(4, monthlyReport.getActivity());
                preparedStatementReport.setString(5, monthlyReport.getStudentId());

                preparedStatementReport.executeUpdate();

                try (PreparedStatement preparedStatementMonthly = databaseConnection.prepareStatement(monthlyQuery)) {
                    preparedStatementMonthly.setInt(1, monthlyReport.getId());
                    preparedStatementMonthly.setString(2, monthlyReport.getMonth());
                    preparedStatementMonthly.setInt(3, monthlyReport.getReportedHours());

                    preparedStatementMonthly.executeUpdate();
                }

                databaseConnection.commit();
                isRegistered = true;

            } catch (SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al registrar reporte mensual", e);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar reporte mensual", e);
        }

        return isRegistered;
    }

    @Override
    public boolean modifyPartialReport(PartialReport partialReport) {
        boolean isModified = false;
        
        String reportQuery = "UPDATE Reporte SET descripcion = ?, observaciones = ?, actividad = ?, matricula = ? " +
                "WHERE idReporte = ?";
        
        String partialQuery = "UPDATE ReporteParcial SET tiempoPlaneado = ?, tiempoReal = ? " +
                "WHERE idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try (PreparedStatement psReport = databaseConnection.prepareStatement(reportQuery)) {
                psReport.setString(1, partialReport.getDescription());
                psReport.setString(2, partialReport.getObservations());
                psReport.setString(3, partialReport.getActivity());
                psReport.setString(4, partialReport.getStudentId());
                psReport.setInt(5, partialReport.getId());

                psReport.executeUpdate();

                try (PreparedStatement psPartial = databaseConnection.prepareStatement(partialQuery)) {
                    psPartial.setInt(1, partialReport.getPlannedTime());
                    psPartial.setInt(2, partialReport.getRealTime());
                    psPartial.setInt(3, partialReport.getId());

                    if (psPartial.executeUpdate() > NO_ROWS_AFFECTED) {
                        databaseConnection.commit();
                        isModified = true;
                    } else {
                        databaseConnection.rollback();
                    }
                }

            } catch (SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al modificar reporte parcial", e);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error con la conexión de la base de datos", e);
        }

        return isModified;
    }

    @Override
    public FinalReport getFinalReportById(int idFinalReport) {
        FinalReport report = null;

        String finalReportQuery = "SELECT r.idReporte, r.descripcion, r.observaciones, r.actividad, r.matricula, " +
                "rf.porcentajeAvance, rf.ResultadoEntregable " +
                "FROM ReporteFinal rf " +
                "INNER JOIN Reporte r ON rf.idReporte = r.idReporte " +
                "WHERE rf.idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(finalReportQuery)) {

            preparedStatement.setInt(1, idFinalReport);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    report = new FinalReport();
                    report.setId(resultSet.getInt("idReporte"));
                    report.setDescription(resultSet.getString("descripcion"));
                    report.setObservations(resultSet.getString("observaciones"));
                    report.setActivity(resultSet.getString("actividad"));
                    report.setStudentId(resultSet.getString("matricula"));
                    report.setAdvancePercentage(resultSet.getInt("porcentajeAvance"));
                    report.setResult(resultSet.getString("ResultadoEntregable"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
        }

        return report;
    }

    @Override
    public boolean registerFinalReport(FinalReport finalReport) {
        boolean isRegistered = false;
        
        String reportQuery = "INSERT INTO Reporte (idReporte, descripcion, observaciones, actividad, matricula) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        String finalReportQuery = "INSERT INTO ReporteFinal (idReporte, porcentajeAvance, ResultadoEntregable) " +
                "VALUES (?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try (PreparedStatement psReport = databaseConnection.prepareStatement(reportQuery)) {
                psReport.setInt(1, finalReport.getId());
                psReport.setString(2, finalReport.getDescription());
                psReport.setString(3, finalReport.getObservations());
                psReport.setString(4, finalReport.getActivity());
                psReport.setString(5, finalReport.getStudentId());

                psReport.executeUpdate();

                try (PreparedStatement psFinal = databaseConnection.prepareStatement(finalReportQuery)) {
                    psFinal.setInt(1, finalReport.getId());
                    psFinal.setInt(2, finalReport.getAdvancePercentage());
                    psFinal.setString(3, finalReport.getResult());

                    if (psFinal.executeUpdate() > NO_ROWS_AFFECTED) {
                        databaseConnection.commit();
                        isRegistered = true;
                    } else {
                        databaseConnection.rollback();
                    }
                }

            } catch (SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al registrar reporte final", e);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
        }

        return isRegistered;
    }

    @Override
    public boolean modifyFinalReport(FinalReport finalReport) {
        boolean isModified = false;

        String reportQuery = "UPDATE Reporte SET descripcion = ?, observaciones = ?, actividad = ?, matricula = ? " +
                "WHERE idReporte = ?";
        
        String finalReportQuery = "UPDATE ReporteFinal SET porcentajeAvance = ?, ResultadoEntregable = ? " +
                "WHERE idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection()) {
            databaseConnection.setAutoCommit(false);

            try (PreparedStatement psReport = databaseConnection.prepareStatement(reportQuery)) {
                psReport.setString(1, finalReport.getDescription());
                psReport.setString(2, finalReport.getObservations());
                psReport.setString(3, finalReport.getActivity());
                psReport.setString(4, finalReport.getStudentId());
                psReport.setInt(5, finalReport.getId());

                psReport.executeUpdate();

                try (PreparedStatement psFinal = databaseConnection.prepareStatement(finalReportQuery)) {
                    psFinal.setInt(1, finalReport.getAdvancePercentage());
                    psFinal.setString(2, finalReport.getResult());
                    psFinal.setInt(3, finalReport.getId());

                    if (psFinal.executeUpdate() > NO_ROWS_AFFECTED) {
                        databaseConnection.commit();
                        isModified = true;
                    } else {
                        databaseConnection.rollback();
                    }
                }

            } catch (SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al modificar reporte final", e);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
        }

        return isModified;
    }
}