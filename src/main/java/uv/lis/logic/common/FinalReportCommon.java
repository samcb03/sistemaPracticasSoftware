package uv.lis.logic.common;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.Report;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class FinalReportCommon {

    private static final Logger LOGGER = Logger.getLogger(FinalReportCommon.class.getName());
    private static final String REPORT_TEMPLATE_PATH = "/uv/lis/GUI/view/templates/finalReport.jrxml";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReportContextDAO reportContextDAO;

    public FinalReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
    }

    public JasperPrint generateFinalReport(Report finalReport) 
            throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            throw new OperationException("No hay un estudiante en sesión", null);
        }

        mergeContextIntoReport(finalReport, currentStudent.getIdStudent());

        JasperPrint jasperPrint;
        try (InputStream templateStream = 
                getClass().getResourceAsStream(REPORT_TEMPLATE_PATH)) {

            if (templateStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", 
                    REPORT_TEMPLATE_PATH);
                throw new OperationException(
                    "No se encontró la plantilla del reporte.", null);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            Map<String, Object> parameters = buildReportParameters(finalReport);
            jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, new JREmptyDataSource());

        } catch (java.io.IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al leer la plantilla del reporte", ioException);
            throw new OperationException(
                "Error al cargar la plantilla del reporte.", ioException);
        }

        return jasperPrint;
    }

    private void mergeContextIntoReport(Report report, String studentId) 
            throws OperationException {
        Report context = reportContextDAO.getReportContextByStudentId(studentId);

        report.setStudentName(context.getStudentName());
        report.setProfessorName(context.getProfessorName());
        report.setNrcSubject(context.getNrcSubject());
        report.setSchoolPeriod(context.getSchoolPeriod());
        report.setProjectName(context.getProjectName());
        report.setProjectObjective(context.getProjectObjective());
        report.setProjectMethodology(context.getProjectMethodology());
        report.setAffiliatedOrganization(context.getAffiliatedOrganization());

        report.setTotalHours(reportContextDAO.getTotalReportedHoursByStudentId(studentId));
        report.setDateReport(LocalDate.now().format(DATE_FORMATTER));
    }

    private Map<String, Object> buildReportParameters(Report report) {
        Map<String, Object> parameters = new HashMap<>();

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

        parameters.put("activityName1", report.getActivityName1());
        parameters.put("advancePercentageActivity1", report.getAdvancePercentageActivity1());
        parameters.put("observationsActivity1", report.getObservationsActivity1());
        parameters.put("activityName2", report.getActivityName2());
        parameters.put("advancePercentageActivity2", report.getAdvancePercentageActivity2());
        parameters.put("observationsActivity2", report.getObservationsActivity2());

        parameters.put("result1", report.getResult1());
        parameters.put("resultAdvance1", report.getResultAdvance1());
        parameters.put("resultObservations1", report.getResultObservations1());
        parameters.put("result2", report.getResult2());
        parameters.put("resultAdvance2", report.getResultAdvance2());
        parameters.put("resultObservations2", report.getResultObservations2());

        parameters.put("generalObservations", report.getGeneralObservations());

        return parameters;
    }
}