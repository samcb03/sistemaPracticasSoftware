package uv.lis.logic.common;


import java.util.List;
import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;


public class AutoevaluationCommon {
    private final AutoevaluationDAO autoevaluationDAO = new AutoevaluationDAO();

    public boolean registerAutoevaluation(Autoevaluation autoevaluation) throws OperationException {
        if (!isValidRange(autoevaluation)) {
            throw new OperationException("Los valores de la autoevaluación deben estar entre 1 y 5.", 
                null);
        }
        if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
            throw new OperationException("El alumno ya ha registrado una autoevaluación.", null);
        }
        return autoevaluationDAO.registerAutoevaluation(autoevaluation);
    }

    private boolean isValidRange(Autoevaluation autoevaluation) {
        List<Integer> values = List.of(
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
        );
        return values.stream().allMatch(this::isInRange);
    }

    private boolean isInRange(int value) {
        boolean inRange = value >= 1 && value <= 5;
        return inRange;
    }
}

