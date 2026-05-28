package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.exceptions.OperationException;

public class ReportContextDAO implements IReportContextDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportContextDAO.class.getName());

    private static final String REPORT_CONTEXT_NOT_FOUND = "No se encontraron datos asociados al alumno";
    private static final String DATABASE_CONNECTION_ERROR = "Error de conexión con la base de datos";
    private static final String FINAL_CONTEXT_ERROR = "Error al obtener los datos contextuales del reporte";
    private static final String PARTIAL_CONTEXT_ERROR = "Error al obtener los datos contextuales del reporte parcial";
    private static final String MONTHLY_CONTEXT_ERROR = "Error al consultar la vista de contexto";
    private static final String TOTAL_HOURS_ERROR = "Error al calcular las horas totales reportadas";
    private static final String ACTIVITIES_QUERY_ERROR = "Error al obtener actividades";
    private static final String ACTIVITY_BY_NAME_ERROR = "Error al obtener la actividad por nombre";

    private static final String INITIAL_TOTAL_HOURS = "0";

    private final MySQLConnectionManager connectionManager;

    public ReportContextDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ReportContextDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public FinalReport getFinalReportContextByStudentId(String studentId) throws OperationException {
        FinalReport finalReport = new FinalReport();
        String reportContextQuery = buildContextQuery();

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    fillFinalReportContext(finalReport, resultSet);
                } else {
                    LOGGER.log(Level.INFO, "No se encontró contexto de reporte para {0}", studentId);
                    throw new OperationException(REPORT_CONTEXT_NOT_FOUND, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(FINAL_CONTEXT_ERROR, e);
        }
        return finalReport;
    }

    @Override
    public PartialReport getPartialReportContextByStudentId(String studentId) throws OperationException {
        PartialReport partialReport = new PartialReport();
        String reportContextQuery = buildContextQuery();

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    fillPartialReportContext(partialReport, resultSet);
                } else {
                    LOGGER.log(Level.INFO, "No se encontró contexto de reporte parcial para {0}", studentId);
                    throw new OperationException(REPORT_CONTEXT_NOT_FOUND, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(PARTIAL_CONTEXT_ERROR, e);
        }
        return partialReport;
    }

    @Override
    public String getTotalReportedHoursByStudentId(String studentId)
            throws OperationException {
        String totalHours = INITIAL_TOTAL_HOURS;
        String reportContextQuery = "SELECT COALESCE(SUM(rm.horasReportadas), 0) AS total "
                                  + "FROM Reporte r "
                                  + "INNER JOIN ReporteMensual rm ON r.idReporte = rm.idReporte "
                                  + "WHERE r.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalHours = String.valueOf(resultSet.getInt("total"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, TOTAL_HOURS_ERROR, e);
            throw new OperationException(TOTAL_HOURS_ERROR, e);
        }
        return totalHours;
    }

    @Override
    public MonthlyReport getMonthlyReportData(String studentId) throws OperationException {
        MonthlyReport monthlyReport = new MonthlyReport();
        String reportContextQuery = "SELECT * FROM v_contexto_academico WHERE matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    fillMonthlyReportContext(monthlyReport, resultSet);
                } else {
                    throw new OperationException(
                        "No se encontró contexto para: " + studentId, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, DATABASE_CONNECTION_ERROR, e);
            throw new OperationException(MONTHLY_CONTEXT_ERROR, e);
        }
        return monthlyReport;
    }

    @Override
    public List<Activity> getRecordedActivities(String studentId) throws OperationException {
        List<Activity> recordedActivities = new ArrayList<>();
        String reportContextQuery = "SELECT a.idActividad, a.nombreActividad, "
                                  + "a.descripcionActividad, a.FechaInicio, a.FechaFin "
                                  + "FROM Actividad a "
                                  + "INNER JOIN Proyecto p ON a.idActividad = p.idActividad "
                                  + "INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                  + "WHERE sp.matricula = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    recordedActivities.add(mapActivity(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, ACTIVITIES_QUERY_ERROR, e);
            throw new OperationException(ACTIVITIES_QUERY_ERROR, e);
        }
        return recordedActivities;
    }

    @Override
    public List<Activity> getRecordedActivitiesByMonth(int idProyecto, int mes, int anio) throws OperationException {
        List<Activity> activities = new ArrayList<>();
        String reportContextQuery = "SELECT DISTINCT nombreActividad, descripcionActividad, horas "
                    + "FROM Actividad "
                    + "WHERE idProyecto = ? AND MONTH(FechaInicio) = ? AND YEAR(FechaInicio) = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {
            
            preparedStatement.setInt(1, idProyecto);
            preparedStatement.setInt(2, mes);
            preparedStatement.setInt(3, anio);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Activity act = new Activity();
                    act.setName(resultSet.getString("nombreActividad"));
                    act.setDescription(resultSet.getString("descripcionActividad"));
                    act.setHoursReported(resultSet.getInt("horas"));
                    activities.add(act);
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al cargar actividades del mes", e);
        }
        return activities;
    }

    @Override
    public Activity getActivityByName(String studentId, String activityName)
            throws OperationException {
        Activity activity = null;
        String reportContextQuery = "SELECT a.idActividad, a.nombreActividad, "
                                  + "a.descripcionActividad, a.FechaInicio, a.FechaFin "
                                  + "FROM Actividad a "
                                  + "INNER JOIN Proyecto p ON a.idActividad = p.idActividad "
                                  + "INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                  + "WHERE sp.matricula = ? AND a.nombreActividad = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {

            preparedStatement.setString(1, studentId);
            preparedStatement.setString(2, activityName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    activity = mapActivity(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, ACTIVITY_BY_NAME_ERROR, e);
            throw new OperationException(ACTIVITY_BY_NAME_ERROR, e);
        }
        return activity;
    }

    @Override
    public int getSumOfReportedHours(int reportId) throws OperationException {
        int total = 0;
        String query = "SELECT SUM(horas) FROM Actividad WHERE idReporte = ?";
        
        try (Connection conn = connectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reportId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    total = resultSet.getInt(1); 
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al calcular total de horas", e);
        }
        return total;
    }

    @Override
    public boolean hasReportAlreadyBeenGenerated(String studentId, String month) throws OperationException {
        String reportContextQuery = "SELECT COUNT(*) FROM Reporte r "
                    + "INNER JOIN ReporteMensual rm ON r.idReporte = rm.idReporte "
                    + "WHERE r.matricula = ? AND rm.mes = ?";
        
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportContextQuery)) {
            preparedStatement.setString(1, studentId);
            preparedStatement.setString(2, month);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new OperationException("Error al verificar duplicidad de reporte", e);
        }
    }

    private String buildContextQuery() {
        String reportContextQuery = "SELECT u.nombre AS nombreAlumno, u.apellidos AS apellidosAlumno, "
                                 + "ee.NRC AS nrc, "
                                 + "pe.nombre AS periodo, "
                                 + "uProf.nombre AS nombreProfesor, uProf.apellidos AS apellidosProfesor, "
                                 + "p.nombre AS nombreProyecto, p.objetivo AS objetivoProyecto, "
                                 + "p.metodologiaProyecto AS metodologiaProyecto, "
                                 + "ov.nombreOV AS organizacion, "
                                 + "rp.nombre AS nombreResponsable "
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
                                 + "INNER JOIN OrganizacionVinculada ov "
                                 + "ON p.idOrganizacionVinculada = ov.idOrganizacionVinculada "
                                 + "LEFT JOIN ResponsableProyecto rp "
                                 + "ON p.idResponsableProyecto = rp.idResponsableProyecto "
                                 + "WHERE a.matricula = ? AND sp.estatus = " + STATUS_ASSIGNED + ";";
        return reportContextQuery;
    }

    private void fillFinalReportContext(FinalReport finalReport, ResultSet resultSet)
            throws SQLException {
        String studentFullName = resultSet.getString("nombreAlumno") + " " 
        + resultSet.getString("apellidosAlumno");
        String professorFullName = resultSet.getString("nombreProfesor") + " " 
            + resultSet.getString("apellidosProfesor");

        finalReport.setStudentName(studentFullName);
        finalReport.setProfessorName(professorFullName);
        finalReport.setNrcSubject(resultSet.getString("nrc"));
        finalReport.setSchoolPeriod(resultSet.getString("periodo"));
        finalReport.setProjectName(resultSet.getString("nombreProyecto"));
        finalReport.setProjectObjective(resultSet.getString("objetivoProyecto"));
        finalReport.setProjectMethodology(resultSet.getString("metodologiaProyecto"));
        finalReport.setAffiliatedOrganization(resultSet.getString("organizacion"));
    }

    private void fillPartialReportContext(PartialReport partialReport, ResultSet resultSet)
            throws SQLException {
        String studentFullName = resultSet.getString("nombreAlumno") 
        + " " + resultSet.getString("apellidosAlumno");
        String professorFullName = resultSet.getString("nombreProfesor") 
        + " " + resultSet.getString("apellidosProfesor");

        partialReport.setStudentName(studentFullName);
        partialReport.setProfessorName(professorFullName);
        partialReport.setNrcSubject(resultSet.getString("nrc"));
        partialReport.setSchoolPeriod(resultSet.getString("periodo"));
        partialReport.setProjectName(resultSet.getString("nombreProyecto"));
        partialReport.setProjectObjective(resultSet.getString("objetivoProyecto"));
        partialReport.setProjectMethodology(resultSet.getString("metodologiaProyecto"));
        partialReport.setAffiliatedOrganization(resultSet.getString("organizacion"));
        partialReport.setProjectSupervisor(resultSet.getString("nombreResponsable"));
    }

    private void fillMonthlyReportContext(MonthlyReport monthlyReport, ResultSet resultSet) throws SQLException {
        String studentFullName = resultSet.getString("nombreAlumno") + " "
            + resultSet.getString("apellidosAlumno");
        monthlyReport.setStudentName(studentFullName);
        monthlyReport.setPeriod(resultSet.getString("periodoPrincipal"));
        monthlyReport.setProfessorName(resultSet.getString("nombreAcademico"));
        monthlyReport.setCoordinatorName(resultSet.getString("nombreCoordinador"));
        monthlyReport.setNrcSubject(resultSet.getString("nrc"));
        monthlyReport.setMonth(resultSet.getString("mes"));
        monthlyReport.setReportNumber(resultSet.getInt("numeroReporte"));
        monthlyReport.setIdProject(resultSet.getInt("idProyecto"));
        monthlyReport.setIdReport(resultSet.getInt("idReporte"));
    }

    private Activity mapActivity(ResultSet resultSet) throws SQLException {
        Activity activity = new Activity();
        activity.setId(resultSet.getInt("idActividad"));
        activity.setName(resultSet.getString("nombreActividad"));
        activity.setDescription(resultSet.getString("descripcionActividad"));

        Date startDate = resultSet.getDate("FechaInicio");
        Date endDate = resultSet.getDate("FechaFin");

        if (startDate != null) {
            activity.setStartDate(startDate.toLocalDate());
        }
        if (endDate != null) {
            activity.setEndDate(endDate.toLocalDate());
        }
        return activity;
    }

}