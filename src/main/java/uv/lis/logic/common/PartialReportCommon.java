package uv.lis.logic.common;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;
import uv.lis.logic.utils.WorkProgressCalculator;

public class PartialReportCommon {

    private static final Logger LOGGER = Logger.getLogger(PartialReportCommon.class.getName());
    private static final String REPORT_TEMPLATE_PATH = "/uv/lis/GUI/view/templates/partialReport.jasper";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final String ACTIVITY_NOT_FOUND_MESSAGE = "No se encontró la actividad seleccionada para el alumno";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte parcial.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte parcial.";

    private final ReportContextDAO reportContextDAO;

    public PartialReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
    }

    public JasperPrint generatePartialReport(PartialReport partialReport) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoReport(partialReport, currentStudent.getIdStudent());
        applyPlannedWeeklyAdvance(partialReport, currentStudent.getIdStudent());
        return fillReportTemplate(partialReport);
    }

    private JasperPrint fillReportTemplate(PartialReport partialReport) throws JRException, OperationException {
        JasperPrint jasperPrint;

        try (InputStream templateStream = getClass().getResourceAsStream(REPORT_TEMPLATE_PATH)) {

            if (templateStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", REPORT_TEMPLATE_PATH);
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            Map<String, Object> parameters = buildReportParameters(partialReport);
            jasperPrint = JasperFillManager.fillReport(templateStream, parameters, new JREmptyDataSource());
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al leer la plantilla del reporte parcial", ioException);
            throw new OperationException(TEMPLATE_READ_ERROR_MESSAGE, ioException);
        }
        return jasperPrint;
    }

    private void mergeContextIntoReport(PartialReport partialReport, String studentId) throws OperationException {
        PartialReport context = reportContextDAO.getPartialReportContextByStudentId(studentId);

        partialReport.setStudentName(context.getStudentName());
        partialReport.setProfessorName(context.getProfessorName());
        partialReport.setNrcSubject(context.getNrcSubject());
        partialReport.setSchoolPeriod(context.getSchoolPeriod());
        partialReport.setProjectName(context.getProjectName());
        partialReport.setProjectObjective(context.getProjectObjective());
        partialReport.setProjectMethodology(context.getProjectMethodology());
        partialReport.setAffiliatedOrganization(context.getAffiliatedOrganization());
        partialReport.setProjectSupervisor(context.getProjectSupervisor());

        partialReport.setTotalHours( reportContextDAO.getTotalReportedHoursByStudentId(studentId));
        partialReport.setDateReport(LocalDate.now().format(DATE_FORMATTER));
    }

    private void applyPlannedWeeklyAdvance(PartialReport partialReport, String studentId) throws OperationException {
        Activity activity = reportContextDAO.getActivityByName(studentId, partialReport.getActivityName());

        if (activity == null) {
            throw new OperationException(ACTIVITY_NOT_FOUND_MESSAGE, null);
        }

        int plannedAdvance = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);
        partialReport.setPlannedAdvanceWeek(plannedAdvance);
    }

    private Map<String, Object> buildReportParameters(PartialReport report) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("studentName", report.getStudentName());
        parameters.put("professorName", report.getProfessorName());
        parameters.put("nrcSubject", report.getNrcSubject());
        parameters.put("schoolPeriod", report.getSchoolPeriod());
        parameters.put("projectName", report.getProjectName());
        parameters.put("projectObjective", report.getProjectObjective());
        parameters.put("projectMethodology", report.getProjectMethodology());
        parameters.put("affiliatedOrganization", report.getAffiliatedOrganization());
        parameters.put("projectSupervisor", report.getProjectSupervisor());
        parameters.put("activityName", report.getActivityName());
        parameters.put("reportPeriod", report.getReportPeriod());
        parameters.put("reportNumber", String.valueOf(report.getReportNumber()));
        parameters.put("plannedAdvanceWeek", String.valueOf(report.getPlannedAdvanceWeek()));
        parameters.put("realAdvanceWeek", String.valueOf(report.getRealAdvanceWeek()));
        parameters.put("result", report.getResult());
        parameters.put("observations", report.getObservations());

        return parameters;
    }
}