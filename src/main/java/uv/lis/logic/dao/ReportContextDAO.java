package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IReportContextDAO;
import uv.lis.logic.dto.Activity;
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
    public MonthlyReport getMonthlyReportData(String studentId) throws OperationException {
        MonthlyReport monthlyReport = new MonthlyReport();
        String mothlyReportQuery = "SELECT * FROM v_contexto_academico WHERE matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(mothlyReportQuery)) {
            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    monthlyReport.setStudentName(resultSet.getString("nombreAlumno") + " " 
                        + resultSet.getString("apellidosAlumno"));
                    monthlyReport.setPeriod(resultSet.getString("periodoPrincipal"));
                    monthlyReport.setProfessorName(resultSet.getString("nombreAcademico"));
                    monthlyReport.setCoordinadorName(resultSet.getString("nombreCoordinador"));
                    monthlyReport.setNrcSubject(resultSet.getString("nrc"));
                    monthlyReport.setMonth(resultSet.getString("mes"));
                    monthlyReport.setReportNumber(resultSet.getInt("numeroReporte"));
                    
                } else {
                    throw new OperationException("No se encontró contexto para: " + studentId, null);
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al consultar la vista de contexto.", e);
        }
        return monthlyReport;
    }

    @Override
    public List<Activity> getRecordedActivities(String studentId) throws OperationException {
        List<Activity> list = new ArrayList<>();
        
        String mothlyReportQuery = "SELECT a.nombreActividad, a.descripcionActividad "
                    + "FROM Actividad a "
                    + "JOIN Proyecto p ON a.idActividad = p.idActividad "
                    + "JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                    + "WHERE sp.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(mothlyReportQuery)) {
            
            preparedStatement.setString(1, studentId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Activity activity = new Activity();
                    activity.setName(resultSet.getString("nombreActividad"));
                    activity.setDescription(resultSet.getString("descripcionActividad"));
                    list.add(activity);
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al obtener actividades.", e);
        }
        return list;
    }

    @Override
    public boolean registerMonthlyReport(MonthlyReport monthlyReport) throws OperationException {
        boolean isRegistered = false; 

        String queryReporte = "INSERT INTO Reporte (matricula, actividad) VALUES (?, ?);";
        String queryMensual = "INSERT INTO ReporteMensual (idReporte, mes, horasReportadas) VALUES (?, ?, ?);";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement ps1 = conn.prepareStatement(queryReporte, Statement.RETURN_GENERATED_KEYS)) {
                ps1.setString(1, monthlyReport.getStudentId());
                ps1.setString(2, "Reporte Mensual");
                int rows1 = ps1.executeUpdate();

                try (ResultSet generatedKeys = ps1.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idGenerado = generatedKeys.getInt(1);

                        try (PreparedStatement ps2 = conn.prepareStatement(queryMensual)) {
                            ps2.setInt(1, idGenerado);
                            ps2.setString(2, monthlyReport.getMonth());
                            ps2.setInt(3, monthlyReport.getReportedHours());
                            int rows2 = ps2.executeUpdate();

                            if (rows1 > 0 && rows2 > 0) {
                                isRegistered = true;
                                conn.commit(); 
                            } else {
                                conn.rollback(); 
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al registrar el reporte mensual", e);
        }
        return isRegistered; 
    }

}
