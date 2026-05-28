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

    private static final String ACTIVITY_NAME_PREFIX = "activityName";
    private static final String PLANNED_ADVANCE_PREFIX = "plannedAdvanceWeek";
    private static final String REAL_ADVANCE_PREFIX = "realAdvanceWeek";
    private static final String ACTIVITY_KEY_FRAGMENT = "Activity";
    private static final int FIRST_ACTIVITY_INDEX = 1;

    private final ReportContextDAO reportContextDAO;

    public PartialReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
    }

    public JasperPrint generatePartialReport(PartialReport partialReport)
            throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoReport(partialReport, currentStudent.getIdStudent());
        applyPlannedWeeklyAdvance(partialReport, currentStudent.getIdStudent());
        return fillReportTemplate(partialReport);
    }

    private JasperPrint fillReportTemplate(PartialReport partialReport)
            throws JRException, OperationException {
        JasperPrint jasperPrint;

        try (InputStream templateStream = getClass().getResourceAsStream(REPORT_TEMPLATE_PATH)) {

            if (templateStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", REPORT_TEMPLATE_PATH);
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            Map<String, Object> parameters = buildReportParameters(partialReport);
            jasperPrint = JasperFillManager.fillReport(
                templateStream, parameters, new JREmptyDataSource());
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE,
                "Error al leer la plantilla del reporte parcial", ioException);
            throw new OperationException(TEMPLATE_READ_ERROR_MESSAGE, ioException);
        }
        return jasperPrint;
    }

    private void mergeContextIntoReport(PartialReport partialReport, String studentId)
            throws OperationException {
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

        partialReport.setTotalHours(
            reportContextDAO.getTotalReportedHoursByStudentId(studentId));
        partialReport.setDateReport(LocalDate.now().format(DATE_FORMATTER));
    }

    private void applyPlannedWeeklyAdvance(PartialReport partialReport, String studentId)
            throws OperationException {
        Activity activity = reportContextDAO.getActivityByName(
            studentId, partialReport.getActivityName());

        if (activity == null) {
            throw new OperationException(ACTIVITY_NOT_FOUND_MESSAGE, null);
        }

        int plannedAdvance = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);
        partialReport.setPlannedAdvanceWeek(plannedAdvance);
    }

    private Map<String, Object> buildReportParameters(PartialReport report) {
        Map<String, Object> parameters = new HashMap<>();

        addContextParameters(parameters, report);
        addActivityNameParameters(parameters, report.getActivityNames());
        addAdvanceMatrixParameters(parameters, report);

        return parameters;
    }

    private void addContextParameters(Map<String, Object> parameters, PartialReport report) {
        parameters.put("studentName", report.getStudentName());
        parameters.put("professorName", report.getProfessorName());
        parameters.put("nrcSubject", report.getNrcSubject());
        parameters.put("schoolPeriod", report.getSchoolPeriod());
        parameters.put("projectName", report.getProjectName());
        parameters.put("projectObjective", report.getProjectObjective());
        parameters.put("projectMethodology", report.getProjectMethodology());
        parameters.put("affiliatedOrganization", report.getAffiliatedOrganization());
        parameters.put("projectSupervisor", report.getProjectSupervisor());
        parameters.put("reportPeriod", report.getReportPeriod());
        parameters.put("reportNumber", String.valueOf(report.getReportNumber()));
        parameters.put("reportDate", report.getDateReport());
        parameters.put("result", report.getResult());
        parameters.put("observations", report.getObservations());
    }
    //FIXME verificar operacion tenearias
    private void addActivityNameParameters(Map<String, Object> parameters,
            String[] activityNames) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            String activityName = activityNames[activityIndex] == null
                ? "" : activityNames[activityIndex];
            String parameterKey = activityIndex == 0
                ? ACTIVITY_NAME_PREFIX
                : ACTIVITY_NAME_PREFIX + (activityIndex + 1);
            parameters.put(parameterKey, activityName);
        }
    }

    private void addAdvanceMatrixParameters(Map<String, Object> parameters,
            PartialReport report) {
        int[][] plannedAdvances = report.getPlannedAdvances();
        int[][] realAdvances = report.getRealAdvances();

        for (int weekIndex = 0; weekIndex < PartialReport.MAX_WEEKS; weekIndex++) {
            int weekNumber = weekIndex + FIRST_ACTIVITY_INDEX;
            putPlannedRow(parameters, weekNumber, plannedAdvances[weekIndex]);
            putRealRow(parameters, weekNumber, realAdvances[weekIndex]);
        }
    }

    private void putPlannedRow(Map<String, Object> parameters,
            int weekNumber, int[] plannedRow) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            int activityNumber = activityIndex + FIRST_ACTIVITY_INDEX;
            String parameterKey = buildCellKey(
                PLANNED_ADVANCE_PREFIX, weekNumber, activityNumber);
            parameters.put(parameterKey, plannedRow[activityIndex]);
        }
    }

    private void putRealRow(Map<String, Object> parameters,
            int weekNumber, int[] realRow) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            int activityNumber = activityIndex + FIRST_ACTIVITY_INDEX;
            String parameterKey = buildCellKey(
                REAL_ADVANCE_PREFIX, weekNumber, activityNumber);
            parameters.put(parameterKey, realRow[activityIndex]);
        }
    }

    private String buildCellKey(String prefix, int weekNumber, int activityNumber) {
        return prefix + weekNumber + ACTIVITY_KEY_FRAGMENT + activityNumber;
    }
}