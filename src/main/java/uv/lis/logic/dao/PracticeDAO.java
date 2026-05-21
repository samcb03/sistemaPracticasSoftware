package uv.lis.logic.dao;
import java.sql.Connection;

import uv.lis.logic.contracts.IPracticeDAO;
import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import uv.lis.dataaccess.MySQLConnectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticeDAO implements IPracticeDAO {
    private final static int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public PracticeDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public PracticeDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean registerPractice(Practice practice) throws OperationException {
        boolean isRegistered = false;
        String practiceQuery = "INSERT INTO Actividad (idActividad, nombreActividad, descripcionActividad, FechaInicio, FechaFin, idReporte) "
                             + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(practiceQuery)) {

            preparedStatement.setInt(1, practice.getIdPractice());
            preparedStatement.setString(2, practice.getPracticeName());
            preparedStatement.setString(3, practice.getDescription());
            preparedStatement.setString(4, practice.getStartDate()); 
            preparedStatement.setString(5, practice.getFinalDate());
            preparedStatement.setInt(6, practice.getProjectId());

                if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED){
                    try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (resultSet.next()) {
                            int generatedId = resultSet.getInt(1);
                            practice.setIdPractice(generatedId);
                    }
                }
                isRegistered = true;
                LOGGER.log(Level.INFO, "Practica con ID {0} registrado con éxito.", practice.getIdPractice());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la actividad.");
                throw new OperationException("No se pudo registrar la actividad. Intentelo mas tarde", 
                    null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo registrar la práctica. Intentelo más tarde", e);

        }
        return isRegistered;
    }

}
