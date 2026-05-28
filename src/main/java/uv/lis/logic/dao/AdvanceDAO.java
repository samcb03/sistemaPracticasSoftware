package uv.lis.logic.dao;


import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IAdvanceDAO;
import uv.lis.logic.dto.Advance;
import uv.lis.logic.exceptions.OperationException;

public class AdvanceDAO implements IAdvanceDAO{
    private static final Logger LOGGER = Logger.getLogger(AutoevaluationDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public AdvanceDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public boolean registerAdvance(Advance advance) throws OperationException{
        String advanceQuery = "INSERT INTO Avance (idProyecto, idReporte, semana, horasAcumuladas) VALUES (?, ?, ?, ?)";
        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {
            
            preparedStatement.setInt(1, advance.getProjectId());
            preparedStatement.setInt(2, advance.getReportId());
            preparedStatement.setInt(3, advance.getWeekNumber());
            preparedStatement.setInt(4, advance.getAccumulatedHours());
            
            return preparedStatement.executeUpdate() > NO_ROWS_AFFECTED;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar autoevaluación", e);
            throw new OperationException("Error al registrar la autoevaluación", e);
        }
    }

    public ArrayList<Advance> getAdvancesByProject(int projectId) throws OperationException{
        ArrayList<Advance> advances = new ArrayList<>();
        String advanceQuery = "SELECT * FROM Avance WHERE idProyecto = ?";
        
        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {
            
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Advance advance = new Advance();
                advance.setProjectId(resultSet.getInt("idProyecto"));
                advance.setReportId(resultSet.getInt("idReporte"));
                advance.setWeekNumber(resultSet.getInt("semana"));
                advance.setAccumulatedHours(resultSet.getInt("horasAcumuladas"));
                advances.add(advance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,"Error al obtener avance", e);
        }
        return advances;
    }

    public int getAccumulatedHoursByProject(int projectId) throws OperationException {
        int accumulatedHours = 0;
        String advanceQuery = "SELECT horasAcumuladas FROM Avance WHERE idProyecto = ?"
                            + " ORDER BY idReporte DESC LIMIT 1";

        try(Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)){
            preparedStatement.setInt(1, projectId);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    accumulatedHours = resultSet.getInt("horasAcumuladas");
                }
            }
        }catch(SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener horas acumuladas", e);
            throw new OperationException("Error al obtener las horas acumuladas", e);
        }
        return accumulatedHours;
    }
}

