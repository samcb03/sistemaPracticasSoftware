package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

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
    private static final Logger LOGGER = Logger.getLogger(ProjectSupervisorDAO.class.getName());
    private MySQLConnectionManager connectionManager;
    private static final int INACTIVE_STATUS = 0;

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
    public ArrayList<String> getProjectsBySupervisorName(String supervisorName) throws OperationException {
        ArrayList<String> projectList = new ArrayList<>();
        String projectQuery = "SELECT p.idProyecto, p.nombre, p.descripcion "
                            + "FROM Proyecto p "
                            + "JOIN ResponsableProyecto rp ON p.idResponsableProyecto = rp.idResponsableProyecto "
                            + "WHERE rp.nombre = ? "
                            + "ORDER BY p.nombre ASC";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, supervisorName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String entry = "ID: " + resultSet.getInt("idProyecto")
                        + " — " + resultSet.getString("nombre")
                        + " (" + resultSet.getString("descripcion") + ")";
                    projectList.add(entry);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos del supervisor", e);
            throw new OperationException("No se pudo obtener la lista de proyectos.", e);
        }
        return projectList;
    }

    @Override
    public boolean registerProjectSupervisor(ProjectSupervisor projectSupervisor) throws OperationException {
        boolean isRegistered = false;

        String supervisorQuery = "INSERT INTO ResponsableProyecto(nombre, cargo," 
                               + "correo, estado, idOrganizacionVinculada) VALUES(?,?,?,?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getPosition());
            preparedStatement.setString(3, projectSupervisor.getEmail());
            preparedStatement.setBoolean(4, projectSupervisor.getIsActive());
            preparedStatement.setInt(5,projectSupervisor.getOrganizationInt());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Supervisor del proyecto registrado exitosamente");
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar al supervisor del proyecto");
                throw new OperationException("No se pudo registrar al supervisor del proyecto", null);
            }

        } catch (SQLException e) {
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
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {
            
            preparedStatement.setString(1, projectSupervisor.getName());
            preparedStatement.setString(2, projectSupervisor.getPosition());
            preparedStatement.setString(3, projectSupervisor.getEmail());
            preparedStatement.setInt(4, projectSupervisor.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
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
    public boolean inactivateProjectSupervisor(String projectSupervisorName) throws OperationException {
        boolean isInactive = false;
        String supervisorQuery = "UPDATE responsableProyecto SET estado = '0' WHERE idResponsableProyecto = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1,projectSupervisorName);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
                LOGGER.log(Level.INFO, "Supervisor dado de baja exitosamente");
            } else {
                LOGGER.log(Level.WARNING, "No se pudo dar de baja al supervisor del proyecto con ID {0}", 
                    projectSupervisorName);
                throw new OperationException("No se pudo dar de baja al supervisor del proyecto con ID: " 
                    + projectSupervisorName, null);
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
        String supervisorQuery = "SELECT nombre FROM ResponsableProyecto WHERE nombre LIKE ? LIMIT 10";

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
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al obtener el ID del responsable de proyecto", e);
        }

        return supervisorId;
    }

    @Override
    public ArrayList<String> getSupervisorsByOrganizationId(int organizationId) throws OperationException {
        ArrayList<String> supervisorNames = new ArrayList<>();
        String supervisorQuery = "SELECT nombre FROM ResponsableProyecto WHERE idOrganizacionVinculada = ? " 
                               + "AND estado = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setInt(1, organizationId);
            preparedStatement.setBoolean(2, true);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    supervisorNames.add(resultSet.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos al filtrar responsables", e);
            throw new OperationException("Error al filtrar los responsables por organización", e);
        }

        return supervisorNames;
    }

    @Override
    public boolean isSupervisorInactive(String supervisorName) throws OperationException {
        boolean isInactive = false;
        String supervisorQuery = "SELECT estado FROM ResponsableProyecto WHERE nombre = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

            preparedStatement.setString(1, supervisorName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isInactive = resultSet.getInt("estado") == INACTIVE_STATUS;
                } else {
                    throw new OperationException("No se encontró al profesor con número: " 
                        + supervisorName, null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar el estado del profesor", e);
            throw new OperationException("No se pudo verificar al profesor. Intente más tarde", e);
        }

        return isInactive;
    }

    @Override
    public boolean hasProjectsActives(String supervisorName) throws OperationException {
        boolean hasProjectActives = false;
        String projectQuery = "SELECT 1 FROM Proyecto p "
                            + "JOIN ResponsableProyecto rp ON p.idResponsableProyecto = rp.idResponsableProyecto "
                            + "WHERE rp.nombre = ? "
                            + "AND p.estado = ? "
                            + "LIMIT 1;";
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, supervisorName);
            preparedStatement.setBoolean(2, true);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hasProjectActives = resultSet.next();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar Proyectos asignados", e);
            throw new OperationException("No se pudo verificar los proyectos activos .Intente más tarde", e);
        }

        return hasProjectActives;
    }

    @Override
    public Optional<ProjectSupervisor> getProjectSupervisorByName(String supervisorName) throws OperationException {
        Optional<ProjectSupervisor> validateSupervisor = Optional.empty();
        String supervisorQuery = "SELECT rp.idResponsableProyecto, rp.nombre, rp.cargo, rp.correo, ov.nombreOV "
                               + "FROM ResponsableProyecto rp "
                               + "LEFT JOIN OrganizacionVinculada ov ON rp.idOrganizacionVinculada = " 
                               + "ov.idOrganizacionVinculada "
                               + "WHERE rp.nombre = ?";

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
                    supervisor.setAffiliatedOrganizationName(resultSet.getString("nombreOV"));
                    
                    validateSupervisor = Optional.of(supervisor);
                } else {
                    LOGGER.log(Level.INFO, "No se encontró un supervisor con el nombre: {0}", supervisorName);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar al supervisor en la base de datos", e);
            throw new OperationException("Error al buscar al supervisor", e);
        }

        return validateSupervisor;
    }
}