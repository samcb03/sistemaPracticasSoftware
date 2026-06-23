package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_VALUE;

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

public class AdvanceDAO implements IAdvanceDAO {

    private static final Logger LOGGER = Logger.getLogger(AdvanceDAO.class.getName());

    private MySQLConnectionManager connectionManager;
    private static final int COUNT_COLUMN = 1;
    private static final int ZERO_COUNT = 0;

    public AdvanceDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public boolean registerAdvance(Advance advance) throws OperationException {
        boolean isRegistered = false;
        String advanceQuery = "INSERT INTO Avance (idProyecto, idReporte, semana, "
                            + "horasAcumuladas) VALUES (?, ?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {

            preparedStatement.setInt(1, advance.getProjectId());
            preparedStatement.setInt(2, advance.getReportId());
            preparedStatement.setInt(3, advance.getWeekNumber());
            preparedStatement.setInt(4, advance.getAccumulatedHours());

            isRegistered = preparedStatement.executeUpdate() > NO_VALUE;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar avance", e);
            throw new OperationException("Error al registrar el avance", e);
        }
        return isRegistered;
    }

    @Override
    public ArrayList<Advance> getAdvancesByProject(int projectId) throws OperationException {
        ArrayList<Advance> advances = new ArrayList<>();
        String advanceQuery = "SELECT * FROM Avance WHERE idProyecto = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {

            preparedStatement.setInt(1, projectId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    advances.add(mapAdvance(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener avance", e);
            throw new OperationException("Error al obtener los avances", e);
        }
        return advances;
    }

    @Override
    public boolean existsAdvanceForReport(int reportId) throws OperationException {
        boolean advanceExists = false;
        String advanceQuery = "SELECT COUNT(*) FROM Avance WHERE idReporte = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {

            preparedStatement.setInt(1, reportId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                advanceExists = resultSet.next() && resultSet.getInt(COUNT_COLUMN) > ZERO_COUNT;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar avance existente", e);
            throw new OperationException("Error al verificar avance existente", e);
        }
        return advanceExists;
    }

    @Override
    public int getAccumulatedHoursByProject(int projectId) throws OperationException {
        int accumulatedHours = 0;
        String advanceQuery = "SELECT horasAcumuladas FROM Avance WHERE idProyecto = ?"
                            + " ORDER BY idReporte DESC LIMIT 1";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(advanceQuery)) {

            preparedStatement.setInt(1, projectId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    accumulatedHours = resultSet.getInt("horasAcumuladas");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener horas acumuladas", e);
            throw new OperationException("Error al obtener las horas acumuladas", e);
        }
        return accumulatedHours;
    }

    private Advance mapAdvance(ResultSet resultSet) throws SQLException {
        Advance advance = new Advance();
        advance.setProjectId(resultSet.getInt("idProyecto"));
        advance.setReportId(resultSet.getInt("idReporte"));
        advance.setWeekNumber(resultSet.getInt("semana"));
        advance.setAccumulatedHours(resultSet.getInt("horasAcumuladas"));

        return advance;
    }
}