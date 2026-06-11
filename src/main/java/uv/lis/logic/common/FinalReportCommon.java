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
import uv.lis.logic.dto.ActivityProgress;
import uv.lis.logic.dto.DeliverableResult;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FinalReportCommon {

    private static final Logger LOGGER = Logger.getLogger(FinalReportCommon.class.getName());
    private static final String REPORT_TEMPLATE_PATH = "/uv/lis/GUI/view/templates/finalReport.jasper";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte.";

    private final ReportContextDAO reportContextDAO;
    private final SimpleJasperReportsContext jasperReportsContext;

    public FinalReportCommon() {
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

    public JasperPrint generateFinalReport(FinalReport finalReport) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoReport(finalReport, currentStudent.getIdStudent());
        return fillReportTemplate(finalReport);
    }

    public JasperPrint fillReportTemplate(FinalReport finalReport) throws JRException, OperationException {
        JasperPrint jasperPrint;

        try (InputStream templateStream = getClass().getResourceAsStream(REPORT_TEMPLATE_PATH)) {

            if (templateStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", REPORT_TEMPLATE_PATH);
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            Map<String, Object> parameters = buildReportParameters(finalReport);
            jasperPrint = JasperFillManager.getInstance(jasperReportsContext)
                .fill(templateStream, parameters, new JREmptyDataSource());
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al leer la plantilla del reporte", ioException);
            throw new OperationException(TEMPLATE_READ_ERROR_MESSAGE, ioException);
        }
        return jasperPrint;
    }

    private void mergeContextIntoReport(FinalReport finalReport, String studentId) throws OperationException {
        FinalReport context = reportContextDAO.getFinalReportContextByStudentId(studentId);

        finalReport.setStudentName(context.getStudentName());
        finalReport.setProfessorName(context.getProfessorName());
        finalReport.setNrcSubject(context.getNrcSubject());
        finalReport.setSchoolPeriod(context.getSchoolPeriod());
        finalReport.setProjectName(context.getProjectName());
        finalReport.setProjectObjective(context.getProjectObjective());
        finalReport.setProjectMethodology(context.getProjectMethodology());
        finalReport.setAffiliatedOrganization(context.getAffiliatedOrganization());

        finalReport.setTotalHours(reportContextDAO.getTotalReportedHoursByStudentId(studentId));
        finalReport.setDateReport(LocalDate.now().format(DATE_FORMATTER));
    }

    private Map<String, Object> buildReportParameters(FinalReport finalReport) {
        Map<String, Object> parameters = new HashMap<>();

        addContextParameters(parameters, finalReport);
        addActivityParameters(parameters, finalReport);
        addDeliverableParameters(parameters, finalReport);
        parameters.put("generalObservations", finalReport.getGeneralObservations());

        return parameters;
    }

    private void addContextParameters(Map<String, Object> parameters, FinalReport report) {
        parameters.put("nrcSubject", report.getNrcSubject());
        parameters.put("professorName", report.getProfessorName());
        parameters.put("schoolPeriod", report.getSchoolPeriod());
        parameters.put("studentName", report.getStudentName());
        parameters.put("affiliatedOrganization", report.getAffiliatedOrganization());
        parameters.put("projectName", report.getProjectName());
        parameters.put("totalHours", report.getTotalHours());
        parameters.put("dateReport", report.getDateReport());
        parameters.put("projectMethodology", report.getProjectMethodology());
        parameters.put("projectObjective", report.getProjectObjective());
    }

    private void addActivityParameters(Map<String, Object> parameters, FinalReport report) {
        ActivityProgress firstActivity = report.getFirstActivity();
        ActivityProgress secondActivity = report.getSecondActivity();

        parameters.put("activityName1", firstActivity.getName());
        parameters.put("advancePercentageActivity1", firstActivity.getAdvancePercentage());
        parameters.put("observationsActivity1", firstActivity.getObservations());

        parameters.put("activityName2", secondActivity.getName());
        parameters.put("advancePercentageActivity2", secondActivity.getAdvancePercentage());
        parameters.put("observationsActivity2", secondActivity.getObservations());
    }

    private void addDeliverableParameters(Map<String, Object> parameters, FinalReport report) {
        DeliverableResult firstDeliverable = report.getFirstDeliverable();
        DeliverableResult secondDeliverable = report.getSecondDeliverable();

        parameters.put("result1", firstDeliverable.getResult());
        parameters.put("resultAdvance1", firstDeliverable.getAdvancePercentage());
        parameters.put("resultObservations1", firstDeliverable.getObservations());

        parameters.put("result2", secondDeliverable.getResult());
        parameters.put("resultAdvance2", secondDeliverable.getAdvancePercentage());
        parameters.put("resultObservations2", secondDeliverable.getObservations());
    }
}