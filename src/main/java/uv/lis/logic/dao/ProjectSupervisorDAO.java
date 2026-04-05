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

    @Override
    public ProjectSupervisor getProjectSupervisorById(int idProjectSupervisor) {
        ProjectSupervisor projectSupervisor = null;

        String projectSupervisorQuery = "SELECT * FROM responsableProyecto WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectSupervisorQuery)){

            preparedStatement.setInt(1, idProjectSupervisor);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    projectSupervisor = new ProjectSupervisor();
                    projectSupervisor.setId(resultSet.getInt("idResponsableProyecto"));
                    projectSupervisor.setName(resultSet.getString("nombre"));
                    projectSupervisor.setEmail(resultSet.getString("correo"));
                    projectSupervisor.setIdAffiliatedOrganization(resultSet.getInt("idOrganizacionVinculada"));
                    
                    LOGGER.log(Level.INFO, "Busqueda de responsable de proyecto con ID {0} exitosa.", 
                        projectSupervisor.getId());
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión a la base de datos al buscar por ID", e);
        }

        return projectSupervisor;
    }

    @Override
    public boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) {
        boolean isRegistered = false;

        String projectSupervisorQuery = "INSERT INTO responsableProyecto(nombre, correo, idOrganizacionVinculada) " 
            + "VALUES(?,?,?);";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectSupervisorQuery)) {

            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getEmail());
            preparedStatement.setInt(3, projectSupervisor.getIdAffiliatedOrganization());

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
            + "nombre = ?, correo = ?, idOrganizacionVinculada = ? WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectSupervisorQuery)){
            
            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getEmail());
            preparedStatement.setInt(3, projectSupervisor.getIdAffiliatedOrganization());
            
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

        String query = "UPDATE responsableProyecto SET estado = '1' WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
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