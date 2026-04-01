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
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;

public class ReportDAO implements IReportDAO{
    private static final Logger logger = Logger.getLogger(ReportDAO.class.getName());

    @Override
    public List<Report> getReports() {
        List<Report> reports = new ArrayList<>();
        String reportQuery = "SELECT p.idReporte, p.tipo,r.observaciones, " + 
                             "r.fechaEntrega, r.matricula " +
                             "FROM Parcial p " +
                             "INNER JOIN Reporte r ON p.idReporte = r.idReporte " +
                             "WHERE p.idReporte = ?;";
        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
            Report report = new Report();
            report.setId(resultSet.getInt("idReporte"));
            reports.add(report);
        }

        } catch (SQLException e){
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);

        }

        return reports;

    }

    @Override
    public PartialReport getReportById(int idPartialReport) {
        PartialReport report = null;
        String finalReportQuery = "SELECT p.idReporte, p.tipo,r.observaciones, " + 
                             "r.fechaEntrega, r.matricula " +
                             "FROM Parcial p" +
                             "INNER JOIN Reporte r ON p.idReporte = r.idReporte " +
                             "WHERE p.idReporte = ?;";

        try (Connection databasConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databasConnection.prepareStatement(finalReportQuery)){

                preparedStatement.setInt(1, idPartialReport);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if(resultSet.next()) {
                        report = new PartialReport();
                        report.setId(resultSet.getInt("idReporte"));
                        report.setObservation(resultSet.getString("observaciones"));
                        report.setDueDate(resultSet.getString("fechaEntrega"));
                        report.setMatricula(resultSet.getString("matricula"));
                        report.setIsMensual(false);

                        String tipo = resultSet.getString("tipo");
                        if (tipo != null && tipo.equalsIgnoreCase("MENSUAL")) {
                            report.setIsMensual(true);
                        } else {
                            report.setIsMensual(false);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            }

        return report;
    }

    @Override
    public boolean registerPartialReport(PartialReport partialReport) {
        boolean isRegistered = false;
        String partialReportQuery = "INSERT INTO ReporteParcial (observacion, fecha_limite, id_estudiante, tipo_reporte) VALUES (?, ?, ?, ?);";

        try(Connection databasConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databasConnection.prepareStatement(partialReportQuery)) {
                preparedStatement.setString(1, partialReport.getObservation());
                preparedStatement.setString(2, partialReport.getDueDate());
                preparedStatement.setString(3, partialReport.getMatricula());

                if(partialReport.getIsMensual()) {
                    preparedStatement.setString(4, "MENSUAL");
                } else {
                    preparedStatement.setString(4, "PARCIAL");
                }

                if (preparedStatement.executeUpdate() > 0) {
                    isRegistered = true;
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            }
            return isRegistered;
        }

    @Override
    public boolean modifyPartialReport(PartialReport partialReport) {
        boolean isModified = false;
        String partialReportQuery = "UPDATE ReporteParcial SET " + 
                                    "observaciones = ?, fechaEntrega = ?," + 
                                    "matricula = ?, tipo = ?" + 
        "WHERE idReporte = ?";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(partialReportQuery)) {
                preparedStatement.setString(1, partialReport.getObservation());
                preparedStatement.setString(2,partialReport.getDueDate());
                preparedStatement.setString(3, partialReport.getMatricula());

                if(partialReport.getIsMensual()) {
                    preparedStatement.setString(4,"MENSUAL");
                } else {
                    preparedStatement.setString(4,"PARCIAL");
                }

                preparedStatement.setInt(5,partialReport.getId());

                if (preparedStatement.executeUpdate() > 0) {
                    isModified = true;
                }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error con la conexión de la base de datos, e");
        }

        return isModified;
    }
    
    @Override
    public FinalReport getFinalReportById(int idFinalReport) {
        FinalReport report = null;

        String finalReportQuery = "SELECT * FROM ReporteFinal "+
        "WHERE idReporte = ?;";

        try (Connection databasConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databasConnection.prepareStatement(finalReportQuery)){

                preparedStatement.setInt(1, idFinalReport);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if(resultSet.next()) {
                        report = new FinalReport();
                        report.setId(resultSet.getInt("idReporte"));
                        report.setPorcentajeAvance(resultSet.getInt("PorcentajeAvance"));
                        report.setResultadoEntregable(resultSet.getString("ResultadoEntregable"));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            }

        return report;
    }

    @Override
    public boolean registerFinalReport(FinalReport finalReport) {
        boolean isRegistered = false;
        String finalReportQuery = "INSERT INTO ReporteFinal (PorcentajeAvance, ResultadoEntregable) VALUES (?, ?);";

        try(Connection databasConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databasConnection.prepareStatement(finalReportQuery)) {
                preparedStatement.setInt(1, finalReport.getPorcentajeAvance());
                preparedStatement.setString(2,finalReport.getResultadoEntregable());

            if (preparedStatement.executeUpdate() > 0) {
                isRegistered = true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            }
            return isRegistered;
        }

    @Override
    public boolean modifyFinalReport(FinalReport finalReport) {
        boolean isModified = false;

        String finalReportQuery = "UPDATE ReporteFinal SET " + 
        "porcentajeAvance = ?, resultadoEntregable = ? WHERE idReporte = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(finalReportQuery)) {
                preparedStatement.setInt(1, finalReport.getPorcentajeAvance());
                preparedStatement.setString(2,finalReport.getResultadoEntregable());
                preparedStatement.setInt(3, finalReport.getId());

                if (preparedStatement.executeUpdate() > 0) {
                    isModified = true;
                }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isModified;
    }
}
    
