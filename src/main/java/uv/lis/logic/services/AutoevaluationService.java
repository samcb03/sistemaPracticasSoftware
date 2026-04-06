package uv.lis.logic.services;

import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;

public class AutoevaluationService {
    private AutoevaluationDAO autoevaluationDAO;

    public AutoevaluationService() {
        this.autoevaluationDAO = new AutoevaluationDAO();
    }

    public boolean registerAutoevaluation(Autoevaluation autoevaluation) {

        if (!isValidRange(autoevaluation)) {
            System.out.println("Valores fuera de rango (1–5)");
            return false;
        }

        if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
            System.out.println("El alumno ya tiene una autoevaluación");
            return false;
        }

        autoevaluation.calculateFinalScore();

        return autoevaluationDAO.registerAutoevaluation(autoevaluation);
    }

    private boolean isValidRange(Autoevaluation autoevaluation) {
        return isInRange(autoevaluation.getProductiveParticipation()) 
            && isInRange(autoevaluation.getAppliedKnowledge()) 
            && isInRange(autoevaluation.getConfidenceInActivities())
            && isInRange(autoevaluation.getActivitiesInterest())
            && isInRange(autoevaluation.getOrganizationSupport())
            && isInRange(autoevaluation.getRulesAwareness())
            && isInRange(autoevaluation.getSupervisorGuidance())
            && isInRange(autoevaluation.getEffectiveMonitoring())
            && isInRange(autoevaluation.getCareerAlignment())
            && isInRange(autoevaluation.getInternshipImportance());
    }

    private boolean isInRange(int value) {
        return value >= 1 && value <= 5;
    }
}

