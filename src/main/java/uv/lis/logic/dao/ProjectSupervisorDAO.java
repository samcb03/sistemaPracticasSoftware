package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

public class ProjectSupervisorDAO implements IProjectSupervisorDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(ProjectSupervisorDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public ProjectSupervisorDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public ArrayList<String> getAllSupervisorNames() throws OperationException{
        String supervisorQuery = "SELECT nombre FROM ResponsableProyecto";
        ArrayList<String> supervisorNames = new ArrayList<>();

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        supervisorNames.add(resultSet.getString("nombre"));
                    } 
            }
        } catch (SQLException e) {
               LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
                throw new OperationException("Error al conseguir los responsables", e);
        }

        return supervisorNames;
    }

    @Override
    public Optional<ProjectSupervisor> getProjectSupervisorById(int id) throws OperationException{
        Optional<ProjectSupervisor> validateSupervisor = Optional.empty();

        String supervisorQuery = "SELECT * FROM ResponsableProyecto WHERE idResponsableProyecto = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ProjectSupervisor supervisor = new ProjectSupervisor();
                supervisor.setId(resultSet.getInt("idResponsableProyecto"));
                supervisor.setName(resultSet.getString("nombre"));
                supervisor.setPosition(resultSet.getString("cargo"));
                supervisor.setEmail(resultSet.getString("correo"));
                validateSupervisor = Optional.of(supervisor);
            } else {
                LOGGER.log(Level.INFO, "No se encontró un supervisor con el id {0}.", id);
                throw new OperationException("No se encontró un supervisor con el id: " + id, null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscando al supervisor", e);
            throw new OperationException("Error al buscar al supervisor", e);
        }

        return validateSupervisor;
    }
    @Override
    public Optional<ProjectSupervisor> getProjectSupervisorByName(String supervisorName) throws OperationException {

        Optional<ProjectSupervisor> validateSupervisor = Optional.empty();

        String supervisorQuery = "SELECT * FROM ResponsableProyecto WHERE nombre = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, supervisorName);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    ProjectSupervisor supervisor = new ProjectSupervisor();
                    supervisor.setId(resultSet.getInt("idResponsableProyecto"));
                    supervisor.setName(resultSet.getString("nombre"));
                    supervisor.setPosition(resultSet.getString("cargo"));
                    supervisor.setEmail(resultSet.getString("correo"));
                    
                    validateSupervisor = Optional.of(supervisor);
                } else {
                    LOGGER.log(Level.INFO, "No se encontró un supervisor con el nombre: {0}", supervisorName);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscando al supervisor", e);
            throw new OperationException("Error al buscar al supervisor en la base de datos", e);
        }

        return validateSupervisor;
    }

    @Override
    public boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException {
        boolean isRegistered = false;

        String supervisorQuery = "INSERT INTO responsableProyecto(nombre, cargo," 
                               + "correo, estado) VALUES(?,?,?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getPosition());
            preparedStatement.setString(3, projectSupervisor.getEmail());
            preparedStatement.setString(4, "0");

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Supervisor del proyecto registrado exitosamente");
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar al supervisor del proyecto");
                throw new OperationException("No se pudo registrar al supervisor del proyecto", null);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Error de conexión con la base de datos al registrar", e);
            throw new OperationException("Error al registrar al supervisor del proyecto", e);
        }

        return isRegistered;
    }

    @Override
    public boolean modifyProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException {
        boolean isModified = false;

        String supervisorQuery = "UPDATE responsableProyecto SET " 
                               + "nombre = ?, cargo = ?, correo = ? WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)){
            
            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getPosition());
            preparedStatement.setString(3, projectSupervisor.getEmail());
            preparedStatement.setInt(4, projectSupervisor.getId());

            if(preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Supervisor modificado exitosamente");
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar al supervisor del proyecto con ID {0}", 
                    projectSupervisor.getId());
                throw new OperationException("No se pudo modificar al supervisor del proyecto con ID: " 
                    + projectSupervisor.getId(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al modificar al supervisor", e);
            throw new OperationException("Error al modificar al supervisor del proyecto", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException {
        boolean isInactive = false;

        String supervisorQuery = "UPDATE responsableProyecto SET estado = '0' WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setInt(1, projectSupervisor.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Supervisor dado de baja exitosamente");
            } else {
                LOGGER.log(Level.WARNING, "No se pudo dar de baja al supervisor del proyecto con ID {0}", 
                    projectSupervisor.getId());
                throw new OperationException("No se pudo dar de baja al supervisor del proyecto con ID: " 
                    + projectSupervisor.getId(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al dar de baja al supervisor", e);
            throw new OperationException("Error al dar de baja al supervisor del proyecto", e);
        }

        return isInactive;
    }

    @Override
    public ArrayList<String> searchProjectSupervisorName(String supervisorName) throws OperationException {
        ArrayList<String> projectSupervisorNames = new ArrayList<>();
        String supervisorQuery = "SELECT nombre FROM ResponsableProyecto WHERE nombre LIKE ? ";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, supervisorName + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectSupervisorNames.add(resultSet.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar a los encargados", e);
            throw new OperationException("No se pudieron obtener los nombres de los encargados", e);
        }
        return projectSupervisorNames;
    }

    @Override
    public int getSupervisorIdByName(String supervisorName) throws OperationException {
        int supervisorId = -1;
        String supervisorQuery = "SELECT idResponsableProyecto FROM ResponsableProyecto WHERE nombre = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, supervisorName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supervisorId = resultSet.getInt("idResponsableProyecto");
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error al obtener el ID del responsable de proyecto", e);
        }

        return supervisorId;
    }
}