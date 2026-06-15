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
import uv.lis.logic.contracts.IAffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

public class AffiliatedOrganizationDAO implements IAffiliatedOrganizationDAO {

    private static final Logger LOGGER = Logger.getLogger(AffiliatedOrganizationDAO.class.getName());
    private static final int INACTIVE_STATE = 0;
    private static final int ACTIVE_STATE = 1;

    private MySQLConnectionManager connectionManager;

    public AffiliatedOrganizationDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public AffiliatedOrganizationDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<AffiliatedOrganization> getOrganizationById(int idAffiliatedOrganization) 
        throws OperationException {
        Optional<AffiliatedOrganization> validateOrganization = Optional.empty();
        String affiliatedOrganizationQuery = "SELECT * FROM OrganizacionVinculada "
                                           + "WHERE idOrganizacionVinculada = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setInt(1, idAffiliatedOrganization);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    validateOrganization = Optional.of(mapAffiliatedOrganization(resultSet));
                } else {
                    LOGGER.log(Level.INFO, "No se encontró una organización vinculada con el id {0}.",
                        idAffiliatedOrganization);
                    throw new OperationException("No se encontró una organización vinculada con el id: "
                        + idAffiliatedOrganization, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener la organización vinculada", e);
        }
        return validateOrganization;
    }

    @Override
    public boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException {
        boolean isRegistered = false;
        String affiliatedOrganizationQuery = "INSERT INTO OrganizacionVinculada (nombreOV, "
                                           + "ciudad, estado, calle, numeroDomicilio, codigoPostal, "
                                           + "sector, correo, telefono, numUsuariosIndirectos, "
                                           + "numUsuariosDirectos, estadoEnBD) "
                                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(
            affiliatedOrganizationQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, affiliatedOrganization.getName());
            preparedStatement.setString(2, affiliatedOrganization.getCity());
            preparedStatement.setString(3, affiliatedOrganization.getState());
            preparedStatement.setString(4, affiliatedOrganization.getStreet());
            preparedStatement.setString(5, affiliatedOrganization.getStreetNumber());
            preparedStatement.setString(6, affiliatedOrganization.getPostalCode());
            preparedStatement.setString(7, affiliatedOrganization.getSector());
            preparedStatement.setString(8, affiliatedOrganization.getEmail());
            preparedStatement.setString(9, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(10, affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(11, affiliatedOrganization.getNumberOfDirectUsers());
            preparedStatement.setInt(12, ACTIVE_STATE);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        affiliatedOrganization.setId(generatedKeys.getInt(1));
                    }
                }
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de organizacion vinculada con ID {0} exitosa.", 
                    affiliatedOrganization.getId());
            } else {
                LOGGER.log(Level.WARNING,
                    "No se pudo registrar la organización vinculada con nombre {0}.", affiliatedOrganization.getName());
                throw new OperationException(
                    "No se pudo registrar la organización vinculada con nombre: " + affiliatedOrganization.getName(), 
                        null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo registrar la Organizacion. Intentelo mas tarde", e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException {
        boolean isModified = false;
        String affiliatedOrganizationQuery = "UPDATE OrganizacionVinculada "
                                           + "SET nombreOV = ?, ciudad = ?, estado = ?, calle = ?, "
                                           + "numeroDomicilio = ?, codigoPostal = ?, sector = ?, "
                                           + "correo = ?, telefono = ?, numUsuariosIndirectos = ?, "
                                           + "numUsuariosDirectos = ? "
                                           + "WHERE idOrganizacionVinculada = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, affiliatedOrganization.getName());
            preparedStatement.setString(2, affiliatedOrganization.getCity());
            preparedStatement.setString(3, affiliatedOrganization.getState());
            preparedStatement.setString(4, affiliatedOrganization.getStreet());
            preparedStatement.setString(5, affiliatedOrganization.getStreetNumber());
            preparedStatement.setString(6, affiliatedOrganization.getPostalCode());
            preparedStatement.setString(7, affiliatedOrganization.getSector());
            preparedStatement.setString(8, affiliatedOrganization.getEmail());
            preparedStatement.setString(9, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(10, affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(11, affiliatedOrganization.getNumberOfDirectUsers());
            preparedStatement.setInt(12, affiliatedOrganization.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            } else {
                LOGGER.log(Level.WARNING,
                    "No se pudo modificar la organización vinculada con ID {0}.", affiliatedOrganization.getId());
                throw new OperationException(
                    "No se pudo modificar la organización vinculada con ID: " + affiliatedOrganization.getId(), 
                        null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al modificar la organización vinculada", e);
        }
        return isModified;
    }

    @Override
    public boolean inactivateOrganization(String organizationName) throws OperationException {
        boolean isInactive = false;
        String affiliatedOrganizationQuery = "UPDATE OrganizacionVinculada "
                                           + "SET estadoEnBD = '0' "
                                           + "WHERE nombreOV = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, organizationName);

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
            } else {
                LOGGER.log(Level.WARNING,
                    "No se pudo inactivar la organización vinculada con nombre {0}.", organizationName);
                throw new OperationException(
                    "No se pudo inactivar la organización vinculada con nombre: " + organizationName, null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al inactivar la organización vinculada", e);
        }
        return isInactive;
    }

    @Override
    public ArrayList<String> getAllOrganizationNames() throws OperationException {
        ArrayList<String> organizationNames = new ArrayList<>();
        String affiliatedOrganizationQuery = "SELECT nombreOV FROM OrganizacionVinculada";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    organizationNames.add(resultSet.getString("nombreOV"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al conseguir la organización vinculada", e);
        }
        return organizationNames;
    }

    @Override
    public int getOrganizationIdByName(String organizationName) throws OperationException {
        int organizationId = 0;
        String affiliatedOrganizationQuery = "SELECT idOrganizacionVinculada "
                                           + "FROM OrganizacionVinculada WHERE nombreOV = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizationId = resultSet.getInt("idOrganizacionVinculada");
                } else {
                    throw new OperationException("No se encontró la organización: " + organizationName, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión", e);
            throw new OperationException("Error al obtener el ID de la organización", e);
        }
        return organizationId;
    }

    @Override
    public Optional<String> getOrganizationBySupervisorName(String supervisorName) throws OperationException {
        Optional<String> organization = Optional.empty();
        String affiliatedOrganizationQuery = "SELECT ov.nombreOV "
                                           + "FROM OrganizacionVinculada ov "
                                           + "INNER JOIN ResponsableProyecto rp "
                                           + "ON ov.idOrganizacionVinculada = "
                                           + "rp.idOrganizacionVinculada "
                                           + "WHERE rp.nombre = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, supervisorName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organization = Optional.of(resultSet.getString("nombreOV"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión a la base de datos", e);
            throw new OperationException("Error al cargar el nombre de la organización", e);
        }
        return organization;
    }

    @Override
    public boolean isOrganizationInactive(String organizationName) throws OperationException {
        boolean isInactive = false;
        String affiliatedOrganizationQuery = "SELECT estadoEnBD FROM OrganizacionVinculada "
                                           + "WHERE nombreOV = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isInactive = resultSet.getInt("estadoEnBD") == INACTIVE_STATE;
                } else {
                    throw new OperationException("No se encontró la organización con nombre: "
                        + organizationName, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar el estado de la organización", e);
            throw new OperationException("No se pudo verificar la organización. Intente más tarde", e);
        }
        return isInactive;
    }

    @Override
    public ArrayList<String> searchActiveOrganizationsByNamePrefix(String prefix) throws OperationException {
        ArrayList<String> organizationNames = new ArrayList<>();
        String affiliatedOrganizationQuery = "SELECT nombreOV FROM OrganizacionVinculada "
                                           + "WHERE nombreOV LIKE ? AND estadoEnBD = 1 "
                                           + "LIMIT 10";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, prefix + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    organizationNames.add(resultSet.getString("nombreOV"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar organizaciones por nombre", e);
            throw new OperationException("Error al buscar organizaciones. Inténtelo más tarde.", e);
        }
        return organizationNames;
    }

    @Override
    public Optional<AffiliatedOrganization> getOrganizationByName(String organizationName) throws OperationException {
        Optional<AffiliatedOrganization> validateOrganization = Optional.empty();
        String affiliatedOrganizationQuery = "SELECT * FROM OrganizacionVinculada "
                                           + "WHERE nombreOV = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    validateOrganization = Optional.of(mapAffiliatedOrganization(resultSet));
                } else {
                    LOGGER.log(Level.INFO, "No se encontró una organización con el nombre: {0}", organizationName);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error buscando a la Organización", e);
            throw new OperationException("Error al buscar a la Organizacion en la base de datos", e);
        }
        return validateOrganization;
    }

    @Override
    public ArrayList<String> getProjectsByOrganization(String organizationName) throws OperationException {
        ArrayList<String> projectList = new ArrayList<>();
        String projectQuery = "SELECT p.idProyecto, p.nombre, p.descripcion "
                            + "FROM Proyecto p "
                            + "JOIN OrganizacionVinculada ov "
                            + "ON p.idOrganizacionVinculada = ov.idOrganizacionVinculada "
                            + "WHERE ov.nombreOV = ? "
                            + "ORDER BY p.nombre ASC";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String entry = "ID: " + resultSet.getInt("idProyecto")
                                 + " — " + resultSet.getString("nombre")
                                 + " (" + resultSet.getString("descripcion") + ")";
                    projectList.add(entry);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos de la organización", e);
            throw new OperationException("No se pudo obtener la lista de proyectos de la organización.", e);
        }
        return projectList;
    }

    @Override
    public boolean hasActiveProjects(String organizationName) throws OperationException {
        boolean hasProjectsActives = false;
        String projectQuery = "SELECT 1 FROM Proyecto p "
                            + "JOIN OrganizacionVinculada ov "
                            + "ON p.idOrganizacionVinculada = ov.idOrganizacionVinculada "
                            + "WHERE ov.nombreOV = ? LIMIT 1";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hasProjectsActives = resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar Proyectos asignados", e);
            throw new OperationException("No se pudo verificar los proyectos activos. Intente más tarde", e);
        }
        return hasProjectsActives;
    }

    @Override
    public ArrayList<Project> getCompleteProjectsByOrganization(String organizationName) throws OperationException {
        ArrayList<Project> projectList = new ArrayList<>();
        String projectQuery = "SELECT p.idProyecto, p.nombre, p.descripcion, p.objetivo, "
                            + "p.cupo, p.metodologiaProyecto, p.estado, ov.nombreOV "
                            + "FROM Proyecto p "
                            + "JOIN OrganizacionVinculada ov "
                            + "ON p.idOrganizacionVinculada = ov.idOrganizacionVinculada "
                            + "WHERE ov.nombreOV = ? "
                            + "ORDER BY p.nombre ASC";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(projectQuery)) {

            preparedStatement.setString(1, organizationName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectList.add(mapProject(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos de la organización", e);
            throw new OperationException( "No se pudo obtener la lista de proyectos de la organización.", e);
        }
        return projectList;
    }

    private AffiliatedOrganization mapAffiliatedOrganization(ResultSet resultSet) throws SQLException {
        AffiliatedOrganization affiliatedOrganization = new AffiliatedOrganization();
        affiliatedOrganization.setId(resultSet.getInt("idOrganizacionVinculada"));
        affiliatedOrganization.setName(resultSet.getString("nombreOV"));
        affiliatedOrganization.setCity(resultSet.getString("ciudad"));
        affiliatedOrganization.setStreet(resultSet.getString("calle"));
        affiliatedOrganization.setStreetNumber(resultSet.getString("numeroDomicilio"));
        affiliatedOrganization.setPostalCode(resultSet.getString("codigoPostal"));
        affiliatedOrganization.setState(resultSet.getString("estado"));
        affiliatedOrganization.setSector(resultSet.getString("sector"));
        affiliatedOrganization.setEmail(resultSet.getString("correo"));
        affiliatedOrganization.setPhoneNumber(resultSet.getString("telefono"));
        affiliatedOrganization.setNumberOfDirectUsers(resultSet.getInt("numUsuariosDirectos"));
        affiliatedOrganization.setNumberOfIndirectUsers(resultSet.getInt("numUsuariosIndirectos"));

        return affiliatedOrganization;
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        Project project = new Project();
        project.setId(resultSet.getInt("idProyecto"));
        project.setName(resultSet.getString("nombre"));
        project.setDescription(resultSet.getString("descripcion"));
        project.setObjective(resultSet.getString("objetivo"));
        project.setCapacity(resultSet.getInt("cupo"));
        project.setMethodology(resultSet.getString("metodologiaProyecto"));
        project.setActive(resultSet.getBoolean("estado"));
        project.setAffiliatedOrganizationName(resultSet.getString("nombreOV"));

        return project;
    }
}