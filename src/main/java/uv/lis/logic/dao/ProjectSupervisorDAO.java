package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;


public class ProjectSupervisorDAO implements IProjectSupervisorDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(ProjectSupervisorDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ProjectSupervisorDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public ProjectSupervisor getProjectSupervisorById(int id) {
        ProjectSupervisor supervisor = null;

        String query = "SELECT * FROM ResponsableProyecto WHERE idResponsableProyecto = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                supervisor = new ProjectSupervisor();
                supervisor.setId(resultSet.getInt("idResponsableProyecto"));
                supervisor.setName(resultSet.getString("nombre"));
                supervisor.setEmail(resultSet.getString("correo"));
                supervisor.setPosition(resultSet.getString("cargo"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscando al supervisor", e);
        }

        return supervisor;
    }

    @Override
    public boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) {
        boolean isRegistered = false;

        String projectSupervisorQuery = "INSERT INTO responsableProyecto(nombre, correo," 
            + "cargo, estado) VALUES(?,?,?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectSupervisorQuery)) {

            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getEmail());
            preparedStatement.setString(3, projectSupervisor.getPosition());
            preparedStatement.setString(4, "0");

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Supervisor del proyecto registrado exitosamente");
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos al registrar", e);
        }

        return isRegistered;
    }

    @Override
    public boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) {
        boolean isModified = false;

        String projectSupervisorQuery = "UPDATE responsableProyecto SET " 
            + "nombre = ?, correo = ?, cargo = ? WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectSupervisorQuery)){
            
            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getEmail());
            preparedStatement.setString(3, projectSupervisor.getPosition());
            preparedStatement.setInt(4, projectSupervisor.getId());

            if(preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Supervisor modificado exitosamente");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar al supervisor", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProjectSupervisor(ProjectSupervisor projectSupervisor) {
        boolean isInactive = false;

        String query = "UPDATE responsableProyecto SET estado = '0' WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setInt(1, projectSupervisor.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Supervisor dado de baja exitosamente");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al dar de baja al supervisor", e);
        }

        return isInactive;
    }
}