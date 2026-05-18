package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IActivityDAO;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;

public class ActivityDAO implements IActivityDAO {
        private static final int NO_ROWS_AFFECTED = 0;
        private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());
        private MySQLConnectionManager connectionManager;

    public ActivityDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public List<Activity> getAllActivities() throws OperationException {
        List<Activity> activities = new ArrayList<>();
        String activityQuery = "SELECT * FROM Actividad;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int idActivity = resultSet.getInt("idActividad");
                String activityName = resultSet.getString("nombreActividad");
                String activityDescription = resultSet.getString("descripcionActividad");
                Date startDate = resultSet.getDate("FechaInicio");
                Date endDate = resultSet.getDate("FechaFin");
                
                activities.add(new Activity(idActivity, activityName, activityDescription, startDate, endDate));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al obtener las actividades", e);
        }
        return activities;
    }

    @Override
    public Optional<Activity> getActivityById(int idActivity) throws OperationException {
        Optional<Activity> activityOptional = Optional.empty();
        String activityQuery = "SELECT * FROM Actividad WHERE idActividad = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            preparedStatement.setInt(1, idActivity);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("idActividad");
                String activityName = resultSet.getString("Nombre actividad");
                String activityDescription = resultSet.getString("Descripcion");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date endDate = resultSet.getDate("Fecha de fin");
                
                activityOptional = Optional.of(new Activity(id, activityName, activityDescription, startDate, endDate));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener las actividades", e);
        }
        return activityOptional;
    }

    @Override
    public boolean registerActivity(Activity activity) throws OperationException {
        boolean isRegistered = false;
        String activityQuery = "INSERT INTO Actividad(idActividad, nombreActividad, descripcionActividad, FechaInicio, " 
            + "FechaFinal) VALUES(?, ?, ?, ?, ?);";

        if (activity == null) {
            isRegistered = false;
            throw new OperationException("No se pudo registrar la actividad", null);
        }

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            
            preparedStatement.setInt(1, activity.getId());
            preparedStatement.setString(2, activity.getName());
            preparedStatement.setString(3, activity.getDescription());
            preparedStatement.setDate(4, activity.getStartDate());
            preparedStatement.setDate(5, activity.getEndDate());
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la actividad con ID {0}.", activity.getId());
                throw new OperationException("No se pudo registrar la actividad con ID: " + activity.getId(),
                    null);     
            }   
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al registrar la actividad", e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyActivity(Activity activity) throws OperationException {
        boolean isModified = false;
        String activityQuery = "UPDATE Actividad SET nombreActividad = ?, descripcionActividad = ?, fechaInicio = ?, " 
                             + "fechaFin = ? WHERE idActividad = ?;";

        if (activity == null) {
            isModified = false;
            throw new OperationException("No se pudo modificar la actividad", null);
        }
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            
            preparedStatement.setString(1, activity.getName());
            preparedStatement.setString(2, activity.getDescription());
            preparedStatement.setDate(3, activity.getStartDate());
            preparedStatement.setDate(4, activity.getEndDate());
            preparedStatement.setInt(5, activity.getId());
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar la actividad con ID {0}.", activity.getId());
                throw new OperationException("No se pudo modificar la actividad con ID: " + activity.getId(), 
                    null);     
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al modificar la actividad", e);
        }
        return isModified;
    }
}