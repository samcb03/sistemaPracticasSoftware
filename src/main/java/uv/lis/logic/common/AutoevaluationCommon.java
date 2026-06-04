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
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.SessionManager;

public class AutoevaluationCommon {
    private static final Logger LOGGER = Logger.getLogger(MonthlyReportCommon.class.getName());
    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/Autoevaluation.jasper";
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte.";

    private final AutoevaluationDAO autoevaluationDAO;
    private SimpleJasperReportsContext jasperReportsContext;

    public AutoevaluationCommon() {
        this.autoevaluationDAO = new AutoevaluationDAO();
        this.jasperReportsContext = buildAutoevaluationContext();

    }

    private SimpleJasperReportsContext buildAutoevaluationContext() {
        SimpleJasperReportsContext reportsContext
            = new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
        List<RepositoryService> repositoryServices = new ArrayList<>();
        repositoryServices.add(new ClasspathImageRepositoryCommon());
        reportsContext.setExtensions(RepositoryService.class, repositoryServices);
        return reportsContext;
    }

    public JasperPrint generateAutoevaluation(Autoevaluation autoevaluation) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoEvaluation(autoevaluation, currentStudent.getIdStudent());

        if (!isValidRange(autoevaluation)) {
            throw new OperationException("Las respuestas deben estar entre 1 y 5.", null);
        }

        if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
            throw new OperationException("El alumno ya ha registrado una autoevaluación.", null);
        }

        autoevaluationDAO.registerAutoevaluation(autoevaluation);
        return fillAutoevaluation(autoevaluation);
    }

    
    private JasperPrint fillAutoevaluation(Autoevaluation autoevaluation) throws JRException, OperationException {
        JasperPrint jasperPrint;

        try (InputStream autoevaluationStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {

            if (autoevaluationStream == null) {
                LOGGER.log(Level.SEVERE, "No se encontró la plantilla: {0}", TEMPLATE_PATH);
                throw new OperationException(TEMPLATE_NOT_FOUND_MESSAGE, null);
            }

            Map<String, Object> parameters = buildParameters(autoevaluation);
            jasperPrint = JasperFillManager.getInstance(jasperReportsContext)
                .fill(autoevaluationStream, parameters, new JREmptyDataSource());
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al leer la plantilla de la autoevaluación", ioException);
            throw new OperationException(TEMPLATE_READ_ERROR_MESSAGE, ioException);
        }
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

    private boolean isValidRange(Autoevaluation autoevaluation) {
        boolean isValid= false;
        int[] scores = {
            autoevaluation.getProductiveParticipation(), autoevaluation.getAppliedKnowledge(),
            autoevaluation.getConfidenceInActivities(),  autoevaluation.getActivitiesInterest(),
            autoevaluation.getOrganizationSupport(),     autoevaluation.getRulesAwareness(),
            autoevaluation.getSupervisorGuidance(),      autoevaluation.getEffectiveMonitoring(),
            autoevaluation.getCareerAlignment(),         autoevaluation.getInternshipImportance()
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