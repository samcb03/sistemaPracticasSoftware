package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IActivityDAO;
import uv.lis.logic.dto.Activity;


public class ActivityDAO implements IActivityDAO {
        private static final int NO_ROWS_AFFECTED = 0;
        private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());

    @Override
    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<>();
        String activityQuery = "SELECT * FROM Actividad;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int idActivity = resultSet.getInt("idActividad");
                String activityName = resultSet.getString("Nombre actividad");
                String activityDescription = resultSet.getString("Descripcion");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date endDate = resultSet.getDate("Fecha de fin");
                
                activities.add(new Activity(idActivity, activityName, activityDescription, startDate, endDate));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesById(int idActivity) {
        List<Activity> activities = new ArrayList<>();
        String activityQuery = "SELECT * FROM Actividad WHERE idActividad = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            preparedStatement.setInt(1, idActivity);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("idActividad");
                String activityName = resultSet.getString("Nombre actividad");
                String activityDescription = resultSet.getString("Descripcion");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date endDate = resultSet.getDate("Fecha de fin");
                
                activities.add(new Activity(id, activityName, activityDescription, startDate, endDate));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return activities;
    }

    @Override
    public boolean registerActivity(Activity activity) {
        boolean isRegistered = false;
        String activityQuery = "INSERT INTO Actividad(idActividad, descripcionActividad, FechaInicio, FechaFinal)" 
            + "VALUES(?, ?, ?, ?, ?);";

        if (activity == null) {
            isRegistered = false;
        }

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            
            preparedStatement.setInt(1, activity.getId());
            preparedStatement.setString(2, activity.getName());
            preparedStatement.setString(3, activity.getDescription());
            preparedStatement.setDate(4, activity.getStartDate());
            preparedStatement.setDate(5, activity.getEndDate());
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyActivity(Activity activity) {
        boolean isModified = false;
        String activityQuery = "UPDATE Actividad SET descripcionActividad = ?, FechaInicio = ?, FechaFin = ?" 
            + "WHERE idActividad = ?;";

        if (activity == null) {
            isModified = false;
        }
        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            
            preparedStatement.setString(1, activity.getName());
            preparedStatement.setString(2, activity.getDescription());
            preparedStatement.setDate(3, activity.getStartDate());
            preparedStatement.setDate(4, activity.getEndDate());
            preparedStatement.setInt(5, activity.getId());
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isModified;
    }
}