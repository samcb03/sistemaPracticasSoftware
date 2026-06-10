package uv.lis.logic.common;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final int FIRST_INDEX = 0;
    private static final int INDEX_OFFSET = 1;

    private final ReportContextDAO reportContextDAO;
    private final SimpleJasperReportsContext jasperReportsContext;

    public PartialReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
        this.jasperReportsContext = buildReportsContext();
    }

    private SimpleJasperReportsContext buildReportsContext() {
        SimpleJasperReportsContext reportsContext 
            = new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
        List<RepositoryService> repositoryServices = new ArrayList<>();
        repositoryServices.add(new ClasspathImageRepositoryCommon());
        reportsContext.setExtensions(RepositoryService.class, repositoryServices);
        return reportsContext;
    }

    public JasperPrint generatePartialReport(PartialReport partialReport) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoReport(partialReport, currentStudent.getIdStudent());
        fillAdvanceMatrix(partialReport, currentStudent.getIdStudent());
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
            jasperPrint = JasperFillManager.getInstance(jasperReportsContext)
                .fill(templateStream, parameters, new JREmptyDataSource());
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

        partialReport.setTotalHours(reportContextDAO.getTotalReportedHoursByStudentId(studentId));
        partialReport.setDateReport(LocalDate.now().format(DATE_FORMATTER));
    }

    private void fillAdvanceMatrix(PartialReport partialReport, String studentId) throws OperationException {
        List<Activity> selectedActivities = new ArrayList<>();
        String[] activityNames = partialReport.getActivityNames();
        boolean useManualAdvances = partialReport.isManualAdvances();

        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            String activityName = activityNames[activityIndex];

            if (activityName != null && !activityName.isBlank()) {
                Activity activity = resolveActivity(studentId, activityName);

                if (!useManualAdvances) {
                    fillActivityColumn(partialReport, activity, activityIndex);
                }
                selectedActivities.add(activity);
            }
        }

        partialReport.setReportPeriod(buildReportPeriod(selectedActivities));
    }

    private Activity resolveActivity(String studentId, String activityName) throws OperationException {
        Activity activity = reportContextDAO.getActivityByName(studentId, activityName);

        if (activity == null) {
            throw new OperationException(ACTIVITY_NOT_FOUND_MESSAGE, null);
        }
        return activity;
    }

    private void fillActivityColumn(PartialReport partialReport, Activity activity, int activityIndex) {
        int activityWeeks = WorkProgressCalculator.calculateActivityWeeks(activity);
        int totalWeeks = Math.min(activityWeeks, PartialReport.MAX_WEEKS);
        int plannedWeeklyAdvance = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);
        int writtenRealAdvance = partialReport.getRealWeeklyAdvances()[activityIndex];
        int realWeeklyAdvance = WorkProgressCalculator.calculateWeeklyRealAdvance(writtenRealAdvance, activity);

        int[][] plannedAdvances = partialReport.getPlannedAdvances();
        int[][] realAdvances = partialReport.getRealAdvances();

        for (int weekIndex = 0; weekIndex < totalWeeks; weekIndex++) {
            plannedAdvances[weekIndex][activityIndex] = plannedWeeklyAdvance;
            realAdvances[weekIndex][activityIndex] = realWeeklyAdvance;
        }
    }

    private String buildReportPeriod(List<Activity> activities) {
        String reportPeriod = "";
        LocalDate earliestStart = findEarliestStart(activities);
        LocalDate latestEnd = findLatestEnd(activities);

        if (earliestStart != null && latestEnd != null) {
            reportPeriod = earliestStart.format(DATE_FORMATTER) + " - " + latestEnd.format(DATE_FORMATTER);
        }
        return reportPeriod;
    }

    private LocalDate findEarliestStart(List<Activity> activities) {
        LocalDate earliestStart = null;

        for (Activity activity : activities) {
            LocalDate startDate = activity.getStartDate();

            if (startDate != null && (earliestStart == null || startDate.isBefore(earliestStart))) {
                earliestStart = startDate;
            }
        }
        return earliestStart;
    }

    private LocalDate findLatestEnd(List<Activity> activities) {
        LocalDate latestEnd = null;

        for (Activity activity : activities) {
            LocalDate endDate = activity.getEndDate();

            if (endDate != null && (latestEnd == null || endDate.isAfter(latestEnd))) {
                latestEnd = endDate;
            }
        }
        return latestEnd;
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

    private void addActivityNameParameters(Map<String, Object> parameters, String[] activityNames) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            String activityName = activityNames[activityIndex] == null ? "" : activityNames[activityIndex];
            String parameterKey = activityIndex == FIRST_INDEX ? ACTIVITY_NAME_PREFIX 
                : ACTIVITY_NAME_PREFIX + (activityIndex + INDEX_OFFSET);
            parameters.put(parameterKey, activityName);
        }
    }

    private void addAdvanceMatrixParameters(Map<String, Object> parameters, PartialReport report) {
        int[][] plannedAdvances = report.getPlannedAdvances();
        int[][] realAdvances = report.getRealAdvances();

        for (int weekIndex = 0; weekIndex < PartialReport.MAX_WEEKS; weekIndex++) {
            int weekNumber = weekIndex + INDEX_OFFSET;
            putPlannedRow(parameters, weekNumber, plannedAdvances[weekIndex]);
            putRealRow(parameters, weekNumber, realAdvances[weekIndex]);
        }
    }

    private void putPlannedRow(Map<String, Object> parameters, int weekNumber, int[] plannedRow) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            int activityNumber = activityIndex + INDEX_OFFSET;
            String parameterKey = buildCellKey(
                PLANNED_ADVANCE_PREFIX, weekNumber, activityNumber);
            parameters.put(parameterKey, String.valueOf(plannedRow[activityIndex]));
        }
    }

    private void putRealRow(Map<String, Object> parameters, int weekNumber, int[] realRow) {
        for (int activityIndex = 0; activityIndex < PartialReport.MAX_ACTIVITIES; activityIndex++) {
            int activityNumber = activityIndex + INDEX_OFFSET;
            String parameterKey = buildCellKey(
                REAL_ADVANCE_PREFIX, weekNumber, activityNumber);
            parameters.put(parameterKey, String.valueOf(realRow[activityIndex]));
        }
    }

    private String buildCellKey(String prefix, int weekNumber, int activityNumber) {
        String cell = prefix + weekNumber + ACTIVITY_KEY_FRAGMENT + activityNumber;
        return cell;
    }
}