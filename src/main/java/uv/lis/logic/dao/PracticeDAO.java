package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_VALUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IPracticeDAO;
import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

public class PracticeDAO implements IPracticeDAO {
    private static final Logger LOGGER = Logger.getLogger(PracticeDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public PracticeDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public PracticeDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Practice getPracticeByStudent(String idStudent) throws OperationException {
        Practice Practice = new Practice();

        String practiceQuery = "SELECT idPractica, calificacion, idStudent "
                             + "FROM Practica "
                             + "WHERE idStudent = ? "
                             + "LIMIT 1;";

        try (Connection connection = connectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(practiceQuery)) {

            preparedStatement.setString(1, idStudent);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Practice.setIdPractice(resultSet.getInt("idPractica"));
                    Practice.setCalification(resultSet.getInt("calificacion"));
                    Practice.setidStudent(resultSet.getString("idStudent"));
                } else {
                    throw new OperationException(
                        "No se encontró práctica para el alumno indicado", null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la práctica del alumno", e);
            throw new OperationException("Error al recuperar la práctica del alumno", e);
        }

        return Practice;
    }

    @Override
    public boolean registerPractice(Practice Practice) throws OperationException {
        boolean isRegistered = false;

        String practiceQuery = "INSERT INTO Practica (calificacion, idStudent) "
                             + "VALUES (?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(practiceQuery)) {

            preparedStatement.setInt(1, Practice.getCalification());
            preparedStatement.setString(2, Practice.getidStudent());

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Práctica registrada para el alumno {0}",
                    Practice.getidStudent());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la práctica para el alumno {0}",
                    Practice.getidStudent());
                throw new OperationException("No se pudo registrar la práctica", null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la práctica", e);
            throw new OperationException("Error al registrar la práctica", e);
        }

        return isRegistered;
    }

    @Override
    public boolean existsByStudent(String idStudent) throws OperationException {
        boolean exists = false;
        String practiceQuery = "SELECT 1 FROM Practice WHERE matricula = ? LIMIT 1;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(practiceQuery)) {

            preparedStatement.setString(1, idStudent);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                exists = resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la existencia de la práctica", e);
            throw new OperationException("Error al verificar la existencia de la práctica", e);
        }

        return exists;
    }
}