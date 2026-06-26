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

    private static final Logger LOGGER = Logger.getLogger(AutoevaluationCommon.class.getName());
    private static final String TEMPLATE_PATH = "/uv/lis/GUI/view/templates/Autoevaluation.jasper";
    private static final String NO_STUDENT_IN_SESSION_MESSAGE = "No hay un estudiante en sesión";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "No se encontró la plantilla del reporte.";
    private static final String TEMPLATE_READ_ERROR_MESSAGE = "Error al cargar la plantilla del reporte.";
    private static final String INVALID_RANGE_MESSAGE = "Las respuestas deben estar entre 1 y 5.";
    private static final String ALREADY_REGISTERED_MESSAGE = "El alumno ya ha registrado una autoevaluación.";
    private static final String NO_DEPARTMENT = "No aplica";
    private static final int MINIMUM_SCORE = 1;
    private static final int MAXIMUM_SCORE = 5;
    private static final String MARKED_OPTION = "X";
    private static final String UNMARKED_OPTION = "";

    private final AutoevaluationDAO autoevaluationDAO;

    private SimpleJasperReportsContext jasperReportsContext;

    public AutoevaluationCommon() {
        this.autoevaluationDAO = new AutoevaluationDAO();
        this.jasperReportsContext = buildAutoevaluationContext();
    }

    public JasperPrint generateAutoevaluation(Autoevaluation autoevaluation) throws JRException, OperationException {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();

        if (currentStudent == null) {
            throw new OperationException(NO_STUDENT_IN_SESSION_MESSAGE, null);
        }

        mergeContextIntoEvaluation(autoevaluation, currentStudent.getIdStudent());

        if (!isValidRange(autoevaluation)) {
            throw new OperationException(INVALID_RANGE_MESSAGE, null);
        }

        if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
            throw new OperationException(ALREADY_REGISTERED_MESSAGE, null);
        }

        autoevaluationDAO.registerAutoevaluation(autoevaluation);
        JasperPrint jasperPrint = fillAutoevaluation(autoevaluation);
        return jasperPrint;

    }

    public JasperPrint fillAutoevaluation(Autoevaluation autoevaluation) throws JRException, OperationException {
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

    private SimpleJasperReportsContext buildAutoevaluationContext() {
        SimpleJasperReportsContext reportsContext
            = new SimpleJasperReportsContext(DefaultJasperReportsContext.getInstance());
        List<RepositoryService> repositoryServices = new ArrayList<>();
        repositoryServices.add(new ClasspathImageRepositoryCommon());
        reportsContext.setExtensions(RepositoryService.class, repositoryServices);
        return reportsContext;
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
        parameters.put("nombreAlumno", autoevaluation.getStudentName());
        parameters.put("matricula", autoevaluation.getIdStudent());
        parameters.put("organizacion", autoevaluation.getOrganizationName());
        parameters.put("responsableProyecto", autoevaluation.getProjectSupervisorName());
        parameters.put("departamento", NO_DEPARTMENT);
        parameters.put("nombreProyecto", autoevaluation.getProjectName());
        parameters.put("puntuacion", String.format("%.0f", autoevaluation.getFinalScore()));
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

        for (int index = 0; index < scores.length; index++) {
            int questionNumber = index + MINIMUM_SCORE;
            int selectedScore = scores[index];
            for (int column = MINIMUM_SCORE; column <= MAXIMUM_SCORE; column++) {
                parameters.put("p" + column + "_" + questionNumber,
                    column == selectedScore ? MARKED_OPTION : UNMARKED_OPTION);
            }
        }

        return parameters;
    }

    private boolean isValidRange(Autoevaluation autoevaluation) {
        boolean isValid = true;
        int[] scores = {
            autoevaluation.getProductiveParticipation(), autoevaluation.getAppliedKnowledge(),
            autoevaluation.getConfidenceInActivities(), autoevaluation.getActivitiesInterest(),
            autoevaluation.getOrganizationSupport(), autoevaluation.getRulesAwareness(),
            autoevaluation.getSupervisorGuidance(), autoevaluation.getEffectiveMonitoring(),
            autoevaluation.getCareerAlignment(), autoevaluation.getInternshipImportance()
        };

        for (int score : scores) {
            if (score < MINIMUM_SCORE || score > MAXIMUM_SCORE) {
                isValid = false;
            }
        }

        return isValid;
    }
}