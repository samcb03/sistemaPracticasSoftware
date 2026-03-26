package cuarto.construccion.logic.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cuarto.construccion.dataaccess.MySQLConnectionManager;
import cuarto.construccion.logic.contracts.IActivityDAO;
import cuarto.construccion.logic.dto.Activity;

public class ActivityDAO implements IActivityDAO {

    @Override
    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<>();
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String activityQuery = "SELECT * FROM Actividad;";
            PreparedStatement preparedStatement = connection.prepareStatement(activityQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int idActivity = resultSet.getInt("idActividad");
                String activityName = resultSet.getString("Nombre actividad");
                String activityDescription = resultSet.getString("Descripcion");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date endDate = resultSet.getDate("Fecha de fin");
                
                activities.add(new Activity(idActivity, activityName, activityDescription, startDate, endDate));
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesById(int idActivity) {
        List<Activity> activities = new ArrayList<>();
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String activityQuery = "SELECT * FROM Actividad WHERE idActividad = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(activityQuery);
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
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return activities;
    }

    @Override
    public boolean registerActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String activityQuery = "INSERT INTO Actividad(idActividad, descripcionActividad, FechaInicio, FechaFinal)" + 
            "VALUES(?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(activityQuery);
            
            preparedStatement.setInt(1, activity.getIdActivity());
            preparedStatement.setString(2, activity.getActivityName());
            preparedStatement.setString(3, activity.getDescription());
            preparedStatement.setDate(4, activity.getStartDate());
            preparedStatement.setDate(5, activity.getEndDate());
            
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean modifyActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String activityQuery = "UPDATE Actividad SET descripcionActividad = ?, FechaInicio = ?, FechaFin = ? WHERE idActividad = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(activityQuery);
            
            preparedStatement.setString(1, activity.getActivityName());
            preparedStatement.setString(2, activity.getDescription());
            preparedStatement.setDate(3, activity.getStartDate());
            preparedStatement.setDate(4, activity.getEndDate());
            preparedStatement.setInt(5, activity.getIdActivity());
            
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}