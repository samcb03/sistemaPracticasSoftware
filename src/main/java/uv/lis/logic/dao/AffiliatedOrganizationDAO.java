package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IAffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;

public class AffiliatedOrganizationDAO implements IAffiliatedOrganizationDAO{
    private static final Logger logger = Logger.getLogger(AffiliatedOrganizationDAO.class.getName());

    @Override
    public AffiliatedOrganization getAffiliatedOrganizationById(int idAfilliatedOrganization) {
        AffiliatedOrganization affiliatedOrganization = null;

        String affiliatedOrganizationQuery = "SELECT * FROM organizacionVinculada " +
        "WHERE idOrganizacionVinculada = ?"; 

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)) {

            preparedStatement.setInt(1,idAfilliatedOrganization);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    affiliatedOrganization = new AffiliatedOrganization();

                    affiliatedOrganization.setId(resultSet.getInt("idOrganizacionVinculada"));
                    affiliatedOrganization.setName(resultSet.getString("nombre"));
                    affiliatedOrganization.setCity(resultSet.getString("ciudad"));
                    affiliatedOrganization.setState(resultSet.getString("estado"));
                    affiliatedOrganization.setEmail(resultSet.getString("correo"));
                    affiliatedOrganization.setPhoneNumber(resultSet.getString("telefono"));
                    affiliatedOrganization.setNumberOfDirectUsers(resultSet.getInt("numUsuariosDirectos"));
                    affiliatedOrganization.setNumberOfIndirectUsers(resultSet.getInt("numUsuariosIndirectos"));

                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return affiliatedOrganization;   
    }

    @Override
    public boolean registerAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) {
        boolean isRegistered = false;
        String affiliatedOrganizationQuery = "INSERT INTO organizacionVinculada(nombreOv," +
                                             "ciudad,estado,sector,correo,telefono,poblacionAtentida," +
                                             "numUsuariosIndirectos,numUsuariosDirectos)" +
                                             "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();

            PreparedStatement preparedStatement = databaseConnection.prepareStatement(affiliatedOrganizationQuery)){

            preparedStatement.setInt(1, affiliatedOrganization.getId());
            preparedStatement.setString(2, affiliatedOrganization.getName());
            preparedStatement.setString(3, affiliatedOrganization.getCity());
            preparedStatement.setString(4, affiliatedOrganization.getState());
            preparedStatement.setString(5, affiliatedOrganization.getEmail());
            preparedStatement.setString(6, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(7,affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(8,affiliatedOrganization.getNumberOfDirectUsers());

            if (preparedStatement.executeUpdate() > 0) {
                isRegistered = true;
                databaseConnection.close();
            }
            databaseConnection.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifyAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) {
        boolean isModified = false;

        String query = "UPDATE organizacionVinculada SET nombreOv = ?, ciudad = ?, estado = ?, correo = ?, " +
                       "telefono = ?, numUsuariosIndirectos = ?, numUsuariosDirectos = ? " +
                       "WHERE idOrganizacion = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setString(1, affiliatedOrganization.getName());
            preparedStatement.setString(2, affiliatedOrganization.getCity());
            preparedStatement.setString(3, affiliatedOrganization.getState());
            preparedStatement.setString(4, affiliatedOrganization.getEmail());
            preparedStatement.setString(5, affiliatedOrganization.getPhoneNumber());
            preparedStatement.setInt(6, affiliatedOrganization.getNumberOfIndirectUsers());
            preparedStatement.setInt(7, affiliatedOrganization.getNumberOfDirectUsers());
            preparedStatement.setInt(8, affiliatedOrganization.getId());

            if (preparedStatement.executeUpdate() > 0) {
                isModified = true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isModified;
    }

    @Override
    public boolean inactiveAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) {
        boolean isInactive = false;

        String query = "UPDATE organizacionVinculada SET estado = 'Inactivo' WHERE idOrganizacion = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setInt(1, affiliatedOrganization.getId());

            if (preparedStatement.executeUpdate() > 0) {
                isInactive = true;
            }

        } catch (SQLException e) {
               logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }

        return isInactive;
    }

}
