package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IReportContextDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;;

public class ReportContextDAO implements IReportContextDAO {
    private static final Logger LOGGER = Logger.getLogger(ReportContextDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ReportContextDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ReportContextDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Report getReportContextByStudentId(String studentId) throws OperationException {
        Report report = new Report();

        String contextQuery = "SELECT u.nombre AS nombreAlumno, u.apellidos AS apellidosAlumno, "
                            + "ee.NRC AS nrc, "
                            + "pe.nombre AS periodo, "
                            + "uProf.nombre AS nombreProfesor, uProf.apellidos AS apellidosProfesor, "
                            + "p.nombre AS nombreProyecto, p.objetivo AS objetivoProyecto, "
                            + "p.metodologiaProyecto AS metodologiaProyecto, "
                            + "ov.nombreOV AS organizacion "
                            + "FROM Alumno a "
                            + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                            + "INNER JOIN Alumno_Esta_EE aee ON a.matricula = aee.matricula "
                            + "INNER JOIN ExperienciaEducativa ee ON aee.NRC = ee.NRC "
                            + "INNER JOIN PeriodoEscolar pe ON ee.idPeriodoEscolar = pe.idPeriodoEscolar "
                            + "INNER JOIN Profesor_Imparte_Experiencia pie ON ee.NRC = pie.NRC "
                            + "INNER JOIN Profesor prof ON pie.numeroPersonal = prof.numeroPersonal "
                            + "INNER JOIN Usuario uProf ON prof.idUsuario = uProf.idUsuario "
                            + "INNER JOIN Solicita_Proyecto sp ON a.matricula = sp.matricula "
                            + "INNER JOIN Proyecto p ON sp.idProyecto = p.idProyecto "
                            + "INNER JOIN OrganizacionVinculada ov ON p.idOrganizacionVinculada = ov.idOrganizacionVinculada "
                            + "WHERE a.matricula = ? AND sp.estatus = " + STATUS_ASSIGNED + ";";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(contextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String studentFullName = resultSet.getString("nombreAlumno") + " "
                        + resultSet.getString("apellidosAlumno");
                    String professorFullName = resultSet.getString("nombreProfesor") + " "
                        + resultSet.getString("apellidosProfesor");

                    report.setStudentName(studentFullName);
                    report.setProfessorName(professorFullName);
                    report.setNrcSubject(resultSet.getString("nrc"));
                    report.setSchoolPeriod(resultSet.getString("periodo"));
                    report.setProjectName(resultSet.getString("nombreProyecto"));
                    report.setProjectObjective(resultSet.getString("objetivoProyecto"));
                    report.setProjectMethodology(resultSet.getString("metodologiaProyecto"));
                    report.setAffiliatedOrganization(resultSet.getString("organizacion"));
                } else {
                    LOGGER.log(Level.INFO, "No se encontró contexto de reporte.",
                        studentId);
                    throw new OperationException("No se encontraron datos asociados al alumno", null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos", e);
            throw new OperationException("Error al obtener los datos contextuales del reporte", e);
        }

        return report;
    }

    @Override
    public String getTotalReportedHoursByStudentId(String studentId) throws OperationException {
        String totalHours = "0";
        String hoursQuery = "SELECT COALESCE(SUM(rm.horasReportadas), 0) AS total "
            + "FROM Reporte r "
            + "INNER JOIN ReporteMensual rm ON r.idReporte = rm.idReporte "
            + "WHERE r.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(hoursQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalHours = String.valueOf(resultSet.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al calcular las horas totales reportadas", e);
            throw new OperationException("Error al calcular las horas totales reportadas", e);
        }

        return totalHours;
    }

    @Override 
    public MonthlyReport getMonthlyReportData(String studentId, int reportId) throws OperationException {
        MonthlyReport monthlyReport = new MonthlyReport();
        String monthlyReportQuery =   "SELECT "
                                    + "u.nombre AS nombreAlumno, "
                                    + "u.apellidos AS apellidosAlumno, "
                                    + "r.idReporte AS numeroReporte, "
                                    + "rm.mes AS mes, "
                                    + "rm.horasReportadas AS horasReportadas, "
                                    + "pe.nombre AS periodoPrincipal, "
                                    + "uProf.nombre AS nombreAcademico, "
                                    + "uProf.apellidos AS apellidosAcademico, "
                                    + "ee.NRC AS nrc, "
                                    + "(SELECT CONCAT(uCoord.nombre, ' ', uCoord.apellidos) "
                                    + " FROM Profesor pCoord "
                                    + " INNER JOIN Usuario uCoord ON pCoord.idUsuario = uCoord.idUsuario "
                                    + " WHERE pCoord.idRol = 3 LIMIT 1) AS nombreCoordinador "
                                    + "FROM Reporte r "
                                    + "INNER JOIN Alumno a ON r.matricula = a.matricula "
                                    + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                                    + "INNER JOIN ReporteMensual rm ON r.idReporte = rm.idReporte "
                                    + "INNER JOIN Alumno_Esta_EE aee ON a.matricula = aee.matricula "
                                    + "INNER JOIN ExperienciaEducativa ee ON aee.NRC = ee.NRC "
                                    + "INNER JOIN PeriodoEscolar pe ON ee.idPeriodoEscolar = pe.idPeriodoEscolar "
                                    + "INNER JOIN Profesor_Imparte_Experiencia pie ON ee.NRC = pie.NRC "
                                    + "INNER JOIN Profesor prof ON pie.numeroPersonal = prof.numeroPersonal "
                                    + "INNER JOIN Usuario uProf ON prof.idUsuario = uProf.idUsuario "
                                    + "WHERE r.matricula = ? AND r.idReporte = ?";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(monthlyReportQuery)) {

            preparedStatement.setString(1, studentId); 
            preparedStatement.setInt(2, reportId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String studentFullName = resultSet.getString("nombreAlumno") + " "
                        + resultSet.getString("apellidosAlumno");
                    
                    monthlyReport.setStudentName(studentFullName);
                    monthlyReport.setReportNumber(resultSet.getInt("numeroReporte"));
                    monthlyReport.setMonth(resultSet.getString("mes"));
                    monthlyReport.setReportedHours(resultSet.getInt("horasReportadas"));
                    monthlyReport.setPeriod(resultSet.getString("periodoPrincipal"));
                    monthlyReport.setProfessorName(
                        resultSet.getString("nombreAcademico") + " "
                        + resultSet.getString("apellidosAcademico"));
                        
                    monthlyReport.setCoordinadorName(resultSet.getString("nombreCoordinador"));
                        
                } else {
                    throw new OperationException(
                        "No se encontraron datos del reporte para la matrícula: "
                        + studentId, null);
                }
            }

        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al obtener encabezado del reporte mensual", e);
            throw new OperationException("Error al obtener los datos del reporte mensual.", e);
        }
        return monthlyReport;
    }


    @Override
    public String getHoursAccumulate(String studentId) throws OperationException {
        String accumulateHours= "0";
        String hoursQuery = "SELECT COALESCE(SUM(rm.horasReportadas), 0) AS total "
                          + "FROM Reporte r "
                          + "INNER JOIN ReporteMensual rm ON r.idReporte = rm.idReporte "
                          + "WHERE r.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(hoursQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hoursQuery = String.valueOf(resultSet.getInt("total"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al calcular horas acumuladas", e);
            throw new OperationException("Error al calcular las horas acumuladas", e);
        }

        return accumulateHours;
    }

}
