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
import uv.lis.logic.contracts.IAffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public class AffiliatedOrganizationDAO implements IAffiliatedOrganizationDAO{
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(AffiliatedOrganizationDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public AffiliatedOrganizationDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public AffiliatedOrganizationDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public Optional<AffiliatedOrganization> getOrganizationById(int idAfilliatedOrganization) throws OperationException {
        Optional<AffiliatedOrganization> validateOrganization = Optional.empty();
        String affiliatedOrganizationQuery = "SELECT * FROM organizacionVinculada " 
            + "WHERE idOrganizacionVinculada = ?"; 

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setInt(1,idAfilliatedOrganization);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    AffiliatedOrganization affiliatedOrganization = new AffiliatedOrganization();
                    affiliatedOrganization.setId(resultSet.getInt("idOrganizacionVinculada"));
                    affiliatedOrganization.setName(resultSet.getString("nombreOV"));
                    affiliatedOrganization.setCity(resultSet.getString("ciudad"));
                    affiliatedOrganization.setState(resultSet.getString("estado"));
                    affiliatedOrganization.setSector(resultSet.getString("sector"));
                    affiliatedOrganization.setEmail(resultSet.getString("correo"));
                    affiliatedOrganization.setPhoneNumber(resultSet.getString("telefono"));
                    affiliatedOrganization.setNumberOfDirectUsers(resultSet.getInt("numUsuariosDirectos"));
                    affiliatedOrganization.setNumberOfIndirectUsers(resultSet.getInt("numUsuariosIndirectos"));
                    validateOrganization = Optional.of(affiliatedOrganization);

                } else {
                    LOGGER.log(Level.INFO, "No se encontró una organización vinculada con el id {0}.", 
                        idAfilliatedOrganization);
                    throw new OperationException("No se encontró una organización vinculada con el id: " 
                        + idAfilliatedOrganization, null);
                }   
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al obtener la organización vinculada", e);
        }

        return validateOrganization;   
    }

    @Override
    public boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException {
        boolean isRegistered = false;
        String affiliatedOrganizationQuery = "INSERT INTO organizacionVinculada(nombreOV," 
                                           + "ciudad, estado, calle, numeroDomicilio, codigoPostal, "
                                           + "sector, correo, telefono, numUsuariosIndirectos,numUsuariosDirectos)" 
                                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, affiliatedOrganization.getName());
            preparedStatement.setString(2, affiliatedOrganization.getCity());
            preparedStatement.setString(3, affiliatedOrganization.getState());
            preparedStatement.setString(4, affiliatedOrganization.getStreet());
            preparedStatement.setString(5, affiliatedOrganization.getStreetNumber());
            preparedStatement.setInt(6, affiliatedOrganization.getPostalCode());
            preparedStatement.setString(7, affiliatedOrganization.getSector());
            preparedStatement.setString(8, affiliatedOrganization.getEmail());
            preparedStatement.setString(9, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(10, affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(11, affiliatedOrganization.getNumberOfDirectUsers());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        affiliatedOrganization.setId(generatedId);
                    }
                }
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de organizacion vinculada con ID {0} exitosa.", 
                    affiliatedOrganization.getId());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la organización vinculada con nombre {0}.", 
                    affiliatedOrganization.getName());
                throw new OperationException("No se pudo registrar la organización vinculada con nombre: " 
                    + affiliatedOrganization.getName(), null);
            }   
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("No se pudo registrar la Organizacion. Intentelo mas tarde", e);

        }
        return isRegistered;
    }

    @Override
    public boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException {
        boolean isModified = false;
        String affiliatedOrganizationQuery = "UPDATE organizacionVinculada " 
                                           + "SET nombreOv = ?, ciudad = ?, "
                                           + "estado = ?, calle = ?, numeroDomicilio = ?, codigoPostal = ?, sector = ?," 
                                           + "correo = ?, telefono = ?, numUsuariosIndirectos = ?, numUsuariosDirectos = ? " 
                                           + "WHERE idOrganizacionVinculada = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setString(1, affiliatedOrganization.getName());
            preparedStatement.setString(2, affiliatedOrganization.getCity());
            preparedStatement.setString(3, affiliatedOrganization.getState());
            preparedStatement.setString(4, affiliatedOrganization.getStreet());
            preparedStatement.setString(5, affiliatedOrganization.getStreetNumber());
            preparedStatement.setInt(6, affiliatedOrganization.getPostalCode());
            preparedStatement.setString(7, affiliatedOrganization.getSector());
            preparedStatement.setString(8, affiliatedOrganization.getEmail());
            preparedStatement.setString(9, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(10, affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(11, affiliatedOrganization.getNumberOfDirectUsers());
            preparedStatement.setInt(12, affiliatedOrganization.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar la organización vinculada con ID {0}.", 
                    affiliatedOrganization.getId());
                throw new OperationException("No se pudo modificar la organización vinculada con ID: " 
                    + affiliatedOrganization.getId(), null);     
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al modificar la organización vinculada", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException {
        boolean isInactive = false;
        String affiliatedOrganizationQuery = "UPDATE organizacionVinculada " 
                                           + "SET estadoEnBD = '0'" 
                                           + "WHERE idOrganizacionVinculada = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setInt(1, affiliatedOrganization.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactive = true;
            } else {
                LOGGER.log(Level.WARNING, "No se pudo inactivar la organización vinculada con ID {0}.", 
                    affiliatedOrganization.getId());
                throw new OperationException("No se pudo inactivar la organización vinculada con ID: " 
                    + affiliatedOrganization.getId(), null);     
            }

        } catch (SQLException e) {
               LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
                throw new OperationException("Error al inactivar la organización vinculada", e);
        }

        return isInactive;
    }


    @Override
    public ArrayList<String> getAllOrganizationNames() throws OperationException{
        String affiliatedOrganizationQuery = "SELECT nombreOV FROM OrganizacionVinculada";
        ArrayList<String> organizationNames = new ArrayList<>();

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        organizationNames.add(resultSet.getString("nombreOV"));
                    } 
            }
        } catch (SQLException e) {
               LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
                throw new OperationException("Error al conseguir la organización vinculada", e);
        }

        return organizationNames;
    }

    @Override
    public int getOrganizationIdByName(String name) throws OperationException {
        String affiliatedOrganizationQuery = "SELECT idOrganizacionVinculada FROM OrganizacionVinculada" 
                                           + "WHERE nombreOV = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int organizationId = resultSet.getInt("idOrganizacionVinculada");
                    return organizationId;
                } else {
                    throw new OperationException("No se encontró la organización: " + name, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión", e);
            throw new OperationException("Error al obtener el ID de la organización", e);
        }
    }

    @Override
    public Optional<String> getOrganizationBySupervisorName(String nombreSupervisor) throws OperationException {
        Optional<String> organization = Optional.empty();
        String supervisorQuery = "SELECT nombreOV FROM OrganizacionVinculada ov" 
            + " JOIN Organizacion_Tiene_Responsable ot ON ov.idOrganizacionVinculada = ot.idOrganizacionVinculada" 
            + " JOIN ResponsableProyecto rp ON ot.idResponsableProyecto = rp.idResponsableProyecto"
            + " WHERE rp.nombre = ?";

            try(Connection databaseConnection = connectionManager.getConnection();
                    PreparedStatement preparedStatement = databaseConnection.prepareStatement(supervisorQuery)) {

                        preparedStatement.setString(1,nombreSupervisor);

                        try(ResultSet resultSet = preparedStatement.executeQuery()) {
                            if(resultSet.next()) {
                               String organizationName = resultSet.getString("nombreOV");
                               organization = Optional.of(organizationName);
                            }
                        }
                    } catch(SQLException e) {
                        LOGGER.log(Level.SEVERE,"Error de conexión a la base de datos", e);
                        throw new OperationException("Error al cargar el nombre de la organización", e);
                    }
            return organization;
        }

}
