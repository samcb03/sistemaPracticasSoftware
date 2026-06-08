package uv.lis.logic.common;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.repo.RepositoryService;

import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;


public class MonthlyReportCommon {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportCommon.class.getName());
    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/MonthlyReport.jasper";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte mensual.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte mensual.";
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final int MAX_ACTIVITIES = 7;
    private static final int YEAR_DIGITS = 4;
    private static final int FIRST_INDEX = 0;

    private final ReportContextDAO reportContextDAO;
    private final SimpleJasperReportsContext jasperReportsContext;
    private final ReportDAO reportDAO;

    public MonthlyReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
        this.jasperReportsContext = buildReportsContext();
        this.reportDAO = new ReportDAO();
    }

    private SimpleJasperReportsContext buildReportsContext() {
        SimpleJasperReportsContext reportsContext
            = new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
        List<RepositoryService> repositoryServices = new ArrayList<>();
        repositoryServices.add(new ClasspathImageRepositoryCommon());
        reportsContext.setExtensions(RepositoryService.class, repositoryServices);
        return reportsContext;
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

        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {

            if (templateStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", TEMPLATE_PATH);
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            Map<String, Object> parameters = buildReportParameters(monthlyReport);
            jasperPrint = JasperFillManager.getInstance(jasperReportsContext)
                    .fill(templateStream, parameters, new JREmptyDataSource());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al leer la plantilla del reporte mensual", e);
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
        report.setSection(context.getSection());
        report.setPeriod(context.getPeriod());
        report.setProfessorName(context.getProfessorName());
        report.setYear(extractYearFromPeriod(context.getPeriod()));
 
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

    private int extractYearFromPeriod(String period) {
        int reportYear = FIRST_INDEX;
 
        if (period != null && period.length() >= YEAR_DIGITS) {
            try {
                reportYear = Integer.parseInt(period.substring(FIRST_INDEX, YEAR_DIGITS));
            } catch (NumberFormatException numberFormatException) {
                LOGGER.log(Level.WARNING, "Periodo escolar con año no numérico: {0}", period);
            }
        }
        return reportYear;
    }
}