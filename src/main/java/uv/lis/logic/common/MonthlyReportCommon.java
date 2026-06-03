package uv.lis.logic.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class MonthlyReportCommon {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportCommon.class.getName());
    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/MonthlyReport.jasper";
    private static final String IMAGE_PATH = "/uv/lis/GUI/view/images/ReporteMensual.jpg";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte mensual.";
    private static final String IMAGE_NOT_FOUND_MESSAGE = "No se encontró la imagen de fondo del reporte mensual.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte mensual.";
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final int MAX_ACTIVITIES = 7;

    private final ReportContextDAO reportContextDAO;
    private final ReportDAO reportDAO;

    public MonthlyReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
        this.reportDAO = new ReportDAO();
    }

    public JasperPrint generateMonthlyReport(MonthlyReport monthlyReport) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoMonthlyReport(monthlyReport, currentStudent.getIdStudent());
        reportDAO.registerMonthlyReport(monthlyReport);
        return fillReportTemplate(monthlyReport);
    }

    private JasperPrint fillReportTemplate(MonthlyReport monthlyReport) throws JRException, OperationException {
        JasperPrint jasperPrint;

        try (InputStream reportStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (reportStream == null) {
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

            Map<String, Object> parameters = buildReportParameters(monthlyReport);
            parameters.put("REPORT_REPOSITORY", new ClasspathImageRepositoryCommon());

            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        } catch (IOException e) {
            throw new OperationException(TEMPLATE_READ_ERROR_MESSAGE, e);
        }
        return jasperPrint;
    }
    private void mergeContextIntoMonthlyReport(MonthlyReport report, String studentId) throws OperationException {
        MonthlyReport context = reportContextDAO.getMonthlyReportData(studentId);
        report.setStudentName(context.getStudentName());
        report.setCoordinatorName(context.getCoordinatorName());
        report.setMonth(context.getMonth());
        report.setReportNumber(context.getReportNumber());
        report.setBlock(context.getBlock());
        report.setSection(context.getSection());
        report.setPeriod(context.getPeriod());
        report.setProfessorName(context.getProfessorName());

        String totalHours = reportContextDAO.getTotalReportedHoursByStudentId(studentId);
        report.setAccumulatedHours(Integer.parseInt(totalHours));
    }

    private Map<String, Object> buildReportParameters(MonthlyReport report) {
        Map<String, Object> parameters = new HashMap<>();

        addStaticParameters(parameters, report);
        addDynamicActivityParameters(parameters, report);

        return parameters;
    }

    private void addStaticParameters(Map<String, Object> parameters, MonthlyReport report) {
        parameters.put("Mes", report.getMonth());
        parameters.put("HorasReportadas", String.valueOf(report.getReportedHours()));
        parameters.put("NombreAlumno", report.getStudentName());
        parameters.put("NombreResponsable", report.getCoordinatorName());
        parameters.put("numeroReporte", String.valueOf(report.getReportNumber()));
        parameters.put("HorasAcumuladas", String.valueOf(report.getAccumulatedHours()));
        parameters.put("Bloque", report.getBlock());
        parameters.put("Seccion", report.getSection());
        parameters.put("PeriodoPrincipal", report.getPeriod());
        parameters.put("Academico", report.getProfessorName());
    }

    private void addDynamicActivityParameters(Map<String, Object> parameters, MonthlyReport report) {
        for (int activityIndex = 1; activityIndex <= MAX_ACTIVITIES; activityIndex++) {
            parameters.put("Periodo" + activityIndex, report.getPeriodAt(activityIndex));
            parameters.put("Actividad" + activityIndex, report.getActivityAt(activityIndex));
            parameters.put("Observacion" + activityIndex,
                report.getObservationAt(activityIndex));
        }
    }
}