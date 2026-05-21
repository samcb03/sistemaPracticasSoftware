package uv.lis.logic.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import uv.lis.logic.dao.ReportContextDAO;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class MonthlyReportCommon {
    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/MonthlyReport.jrxml";
    private final ReportContextDAO reportContextDAO;

    public MonthlyReportCommon() {
        this.reportContextDAO = new ReportContextDAO();
    }

    public JasperPrint generateMonthlyReport(MonthlyReport monthlyReport) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            throw new OperationException("No hay un estudiante en sesión", null);
        }

        mergeContextIntoMonthlyReport(monthlyReport, currentStudent.getIdStudent());

        InputStream reportStream = getClass().getResourceAsStream(TEMPLATE_PATH);
        if (reportStream == null) {
            throw new OperationException("No se encontró la plantilla del reporte: " + TEMPLATE_PATH, null);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        Map<String, Object> parameters = buildReportParameters(monthlyReport);
        return JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
    }

    private void mergeContextIntoMonthlyReport(MonthlyReport report, String studentId) throws OperationException {
        MonthlyReport context = reportContextDAO.getMonthlyReportData(studentId, 0);
        report.setStudentName(context.getStudentName());
        report.setCoordinadorName(context.getCoordinadorName()); 
        report.setMonth(context.getMonth());
        report.setReportNumber(context.getReportNumber());
        String totalHoras = reportContextDAO.getHoursAccumulate(studentId);
        report.setAccumulateHour(Integer.parseInt(totalHoras));
        report.setBlock(context.getBlock());
        report.setSection(context.getSection());
        report.setPeriod(context.getPeriod());
        report.setProfessorName(context.getProfessorName());
    }

    private Map<String, Object> buildReportParameters(MonthlyReport report) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("Mes", report.getMonth());
        parameters.put("HorasReportadas", String.valueOf(report.getReportedHours()));
        parameters.put("NombreAlumno", report.getStudentName());
        parameters.put("NombreResponsable", report.getCoordinadorName()); 
        parameters.put("numeroReporte", String.valueOf(report.getReportNumber()));
        parameters.put("HorasAcumuladas", String.valueOf(report.getAccumulateHour()));
        parameters.put("Bloque", report.getNrcSubject()); 
        parameters.put("Seccion", report.getSection()); 
        parameters.put("PeriodoPrincipal", report.getPeriod()); 
        parameters.put("Academico", report.getProfessorName());

        for (int i = 1; i <= 7; i++) {
            parameters.put("Periodo" + i, report.getPeriod(i));
            parameters.put("Actividad" + i, report.getActivity(i));
            parameters.put("Observacion" + i, report.getObservation(i));
        }

        return parameters;
    }
}