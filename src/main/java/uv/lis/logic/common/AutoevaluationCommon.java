package uv.lis.logic.common;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class AutoevaluationCommon {

    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/Autoevaluation.jasper";
    private final AutoevaluationDAO autoevaluationDAO = new AutoevaluationDAO();

    public JasperPrint generateAutoevaluation(Autoevaluation autoevaluation) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException("No hay estudiante en sesión.", null);
        }

        mergeContextIntoEvaluation(autoevaluation, currentStudent.getIdStudent());

        if (!isValidRange(autoevaluation)) {
            throw new OperationException("Las respuestas deben estar entre 1 y 5.", null);
        }

        if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
            throw new OperationException("El alumno ya ha registrado una autoevaluación.", null);
        }

        autoevaluationDAO.registerAutoevaluation(autoevaluation);
        InputStream autoevaluationStream = getClass().getResourceAsStream(TEMPLATE_PATH);
        
        JasperReport jasperReport = (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(autoevaluationStream);
        Map<String, Object> parameters = buildParameters(autoevaluation);
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, 
            new net.sf.jasperreports.engine.JREmptyDataSource());

        return jasperPrint;
    }

    private void mergeContextIntoEvaluation(Autoevaluation autoevaluation, String studentId) throws OperationException {

        Autoevaluation context = autoevaluationDAO.getAutoevaluationData(studentId);
        autoevaluation.setStudentName(context.getStudentName());
        autoevaluation.setIdStudent(context.getIdStudent());
        autoevaluation.setOrganizationName(context.getOrganizationName());
        autoevaluation.setProjectSupervisorName(context.getProjectSupervisorName()); 
        autoevaluation.setProjectName(context.getProjectName());
    }

    private Map<String, Object> buildParameters(Autoevaluation autoevaluation) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("nombreAlumno",        autoevaluation.getStudentName());
        parameters.put("matricula",           autoevaluation.getIdStudent());
        parameters.put("organizacion",        autoevaluation.getOrganizationName());
        parameters.put("responsableProyecto", autoevaluation.getProjectSupervisorName());
        parameters.put("nombreProyecto",      autoevaluation.getProjectName());
        parameters.put("puntuacion",
            String.format("%.1f%%", autoevaluation.getFinalScore()));

        int[] scores = {
            autoevaluation.getProductiveParticipation(),
            autoevaluation.getAppliedKnowledge(),
            autoevaluation.getConfidenceInActivities(),
            autoevaluation.getActivitiesInterest(),
            autoevaluation.getOrganizationSupport(),
            autoevaluation.getRulesAwareness(),
            autoevaluation.getSupervisorGuidance(),
            autoevaluation.getEffectiveMonitoring(),
            autoevaluation.getCareerAlignment(),
            autoevaluation.getInternshipImportance()
        };

        for (int i = 0; i < scores.length; i++) {
            int questionNum   = i + 1;
            int selectedScore = scores[i];
            for (int col = 1; col <= 5; col++) {
                parameters.put("p" + col + "_" + questionNum,
                    col == selectedScore ? "X" : "");
            }
        }

        return parameters;
    }

    private boolean isValidRange(Autoevaluation a) {
        boolean isValid= false;
        int[] scores = {
            a.getProductiveParticipation(), a.getAppliedKnowledge(),
            a.getConfidenceInActivities(),  a.getActivitiesInterest(),
            a.getOrganizationSupport(),     a.getRulesAwareness(),
            a.getSupervisorGuidance(),      a.getEffectiveMonitoring(),
            a.getCareerAlignment(),         a.getInternshipImportance()
        };
        for (int s : scores) {
            if (s < 1 || s > 5) {
                return isValid;
            }
            isValid = true;
        }
        return isValid;
    }
}