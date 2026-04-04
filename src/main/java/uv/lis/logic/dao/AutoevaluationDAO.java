package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IAutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;

public class AutoevaluationDAO implements IAutoevaluationDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(AutoevaluationDAO.class.getName());

    @Override
    public boolean registerAutoevaluation(Autoevaluation autoevaluation) {
    boolean isRegistered = false;
    
    String query = "INSERT INTO Autoevaluacion (matricula, participacionProductiva, " +
        "conocimientoAplicado, confianzaEnActividades, interesEnActividades, " 
        + "apoyoOrganizacional, conocimientoDeReglas, orientacionSupervisor, " 
        + "seguimientoEfectivo, alineacionCarrera, importanciaPracticas, " 
        + "puntuacionFinal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    try (Connection databaseConnection = MySQLConnectionManager.getConnection();
         PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
        
        preparedStatement.setString(1, autoevaluation.getIdStudent());
        preparedStatement.setInt(2, autoevaluation.getProductiveParticipation());
        preparedStatement.setInt(3, autoevaluation.getAppliedKnowledge());
        preparedStatement.setInt(4, autoevaluation.getConfidenceInActivities());
        preparedStatement.setInt(5, autoevaluation.getActivitiesInterest());
        preparedStatement.setInt(6, autoevaluation.getOrganizationSupport());
        preparedStatement.setInt(7, autoevaluation.getRulesAwareness());
        preparedStatement.setInt(8, autoevaluation.getSupervisorGuidance());
        preparedStatement.setInt(9, autoevaluation.getEffectiveMonitoring());
        preparedStatement.setInt(10, autoevaluation.getCareerAlignment());
        preparedStatement.setInt(11, autoevaluation.getInternshipImportance());
        preparedStatement.setDouble(12, autoevaluation.getFinalScore());

        if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
            isRegistered = true;
            LOGGER.log(Level.INFO, "Autoevaluación registrada para el alumno {0}", 
                autoevaluation.getIdStudent());
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error al registrar autoevaluación", e);
    }

    return isRegistered;
    }
}
