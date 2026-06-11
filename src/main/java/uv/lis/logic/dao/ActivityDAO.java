package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
                LocalDate startDate = resultSet.getObject("FechaInicio", LocalDate.class);
                LocalDate endDate = resultSet.getObject("FechaFin", LocalDate.class);
                int idProject = resultSet.getInt("idProyecto"); 
                
                activities.add(new Activity(idActivity, activityName, activityDescription, startDate, endDate, 
                    idProject));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
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
            
            if (resultSet.next()) { 
                int id = resultSet.getInt("idActividad");
                
                String activityName = resultSet.getString("nombreActividad"); 
                String activityDescription = resultSet.getString("descripcionActividad"); 
                
                LocalDate startDate = resultSet.getObject("FechaInicio", LocalDate.class); 
                LocalDate endDate = resultSet.getObject("FechaFin", LocalDate.class); 
                
                int idProject = resultSet.getInt("idProyecto");
                
                activityOptional = Optional.of(new Activity(id, activityName, activityDescription, startDate, endDate, 
                    idProject));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener la actividad", e);
        }
        return activityOptional;
    }

    @Override
    public boolean registerActivity(Activity activity) throws OperationException {
        boolean isRegistered = false;
    
        String activityQuery = "INSERT INTO Actividad(nombreActividad, descripcionActividad, FechaInicio, " 
                             + "FechaFin, idProyecto,horasReportadas) " 
                             + "VALUES(?, ?, ?, ?, ?, ?);";

        if (activity == null) {
            throw new OperationException("No se pudo registrar la actividad porque es nula", null);
        }

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery,
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, activity.getName());
            preparedStatement.setString(2, activity.getDescription());
            preparedStatement.setObject(3, activity.getStartDate());
            preparedStatement.setObject(4, activity.getEndDate());
            preparedStatement.setInt(5, activity.getProjectId());
            preparedStatement.setInt(6,activity.getHoursReported());
            
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                try(ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        activity.setId(generatedId);

                    }
                }
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de actividad exitosa");    
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la actividad.");
                throw new OperationException("No se pudo registrar la actividad.", null); 
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al registrar la actividad", e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyActivity(Activity activity) throws OperationException {
        boolean isModified = false;
        
        String activityQuery = "UPDATE Actividad SET nombreActividad = ?, descripcionActividad = ?, FechaInicio = ?, " 
                             + "FechaFin = ?, idProyecto = ?, horasReportadas = ? WHERE idActividad = ?;";

        if (activity == null) {
            throw new OperationException("No se pudo modificar la actividad porque es nula", null);
        }
        
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityQuery)) {
            
            preparedStatement.setString(1, activity.getName());
            preparedStatement.setString(2, activity.getDescription());
            preparedStatement.setObject(3, activity.getStartDate());
            preparedStatement.setObject(4, activity.getEndDate());
            preparedStatement.setInt(5, activity.getProjectId());
            preparedStatement.setInt(6, activity.getHoursReported());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar la actividad con ID {0}.", activity.getId());
                throw new OperationException("No se pudo modificar la actividad con ID: " + activity.getId(), 
                    null);    
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al modificar la actividad", e);
        }
        return isModified;
    }

    @Override
    public List<Activity> getActivitiesByStudentId(String studentId) throws OperationException {
        List<Activity> activities = new ArrayList<>();
        String activityContextQuery = "SELECT a.idActividad, a.nombreActividad, "
                                    + "a.descripcionActividad, a.FechaInicio, a.FechaFin, "
                                    + "p.idProyecto "
                                    + "FROM Actividad a "
                                    + "INNER JOIN Proyecto p ON a.idProyecto = p.idProyecto "
                                    + "INNER JOIN Solicita_Proyecto sp ON p.idProyecto = sp.idProyecto "
                                    + "WHERE sp.matricula = ? AND sp.estatus = ? "
                                    + "ORDER BY a.idActividad";
 
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(activityContextQuery)) {
 
            preparedStatement.setString(1, studentId);
            preparedStatement.setInt(2, STATUS_ASSIGNED);
 
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(mapActivity(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al acceder a la base de datos", e);
            throw new OperationException("Error al obtener las actividades", e);
        }
        return activities;
    }
 
    private Activity mapActivity(ResultSet resultSet) throws SQLException {
        int idActivity = resultSet.getInt("idActividad");
        String activityName = resultSet.getString("nombreActividad");
        String activityDescription = resultSet.getString("descripcionActividad");
        LocalDate startDate = resultSet.getObject("FechaInicio", LocalDate.class);
        LocalDate endDate = resultSet.getObject("FechaFin", LocalDate.class);
        int idProject = resultSet.getInt("idProyecto");
        Activity activity = new Activity(idActivity, activityName, activityDescription,startDate, endDate, idProject);
            
        return activity;
    }
}