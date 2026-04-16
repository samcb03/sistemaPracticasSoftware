package uv.lis.logic.services;

import java.util.logging.Level;

import uv.lis.logic.dao.AutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;

import java.util.logging.Logger;
public class AutoevaluationService {
    private static final Logger LOGGER = Logger.getLogger(AutoevaluationService.class.getName());
    private AutoevaluationDAO autoevaluationDAO;

    public AutoevaluationService() {
        this.autoevaluationDAO = new AutoevaluationDAO();
    }

    public boolean registerAutoevaluation(Autoevaluation autoevaluation) throws OperationException {
        try {
            if (!isValidRange(autoevaluation)) {
                LOGGER.log(Level.WARNING, "Los valores de la autoevaluación deben estar entre 1 y 5 para el alumno {0}", 
                    autoevaluation.getIdStudent());
                throw new OperationException("Los valores de la autoevaluación deben estar entre 1 y 5.", null);
            } 
            if (autoevaluationDAO.existsByStudent(autoevaluation.getIdStudent())) {
                LOGGER.log(Level.WARNING, "El alumno {0} ya ha registrado una autoevaluación", 
                    autoevaluation.getIdStudent());
                throw new OperationException("El alumno ya ha registrado una autoevaluación: " + autoevaluation.getIdStudent(), null);
            }

            return autoevaluationDAO.registerAutoevaluation(autoevaluation);
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos al registrar autoevaluación para el alumno {0}",e);
            throw new OperationException("Error de base de datos al registrar la autoevaluación", null);
        }
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

