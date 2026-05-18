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
import uv.lis.logic.contracts.ISchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;

public class SchoolPeriodDAO implements ISchoolPeriodDAO{
    private static final int NO_ROWS_AFFECTED = 0; 
    private static final Logger LOGGER = Logger.getLogger(SchoolPeriodDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public SchoolPeriodDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    public SchoolPeriodDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public ArrayList<String> getAllSchoolPeriodsNames() throws OperationException {
        ArrayList<String> periodsNames = new ArrayList<>();
        String schoolPeriodQuery = "SELECT nombre FROM PeriodoEscolar";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                periodsNames.add(resultSet.getString("nombre"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los periodos escolares", e);
            throw new OperationException("No se pudieron obtener los periodos. Intentelo mas tarde", e);
        }
        return periodsNames;
    }

    @Override
    public Optional<String> getSchoolPeriodIdByName(String periodName) throws OperationException {
        Optional<String> validatePeriodId = Optional.empty();
        String schoolPeriodQuery = "SELECT idPeriodoEscolar FROM PeriodoEscolar WHERE nombre = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)) {

            preparedStatement.setString(1, periodName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String periodId = resultSet.getString("idPeriodoEscolar");
                    validatePeriodId = Optional.of(periodId);
                    LOGGER.log(Level.INFO, "ID de periodo escolar obtenido con exito");
                } else {
                    LOGGER.log(Level.INFO, "No se encontro el periodo escolar");
                    throw new OperationException("No se encontró el periodo escolar: " + periodName, null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener el ID del periodo escolar", e);
        }

        return validatePeriodId;
    }

    @Override
    public boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException {
        boolean isRegistered = false;
        String schoolPeriodQuery = "INSERT INTO PeriodoEscolar(idPeriodoEscolar, FechaInicio, FechaFin) " 
                                    + "VALUES(?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)){

            preparedStatement.setInt(1, schoolPeriod.getId());
            preparedStatement.setDate(2, schoolPeriod.getStartDate());
            preparedStatement.setDate(3, schoolPeriod.getEndDate());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED){
                isRegistered = true;
            } else {
                throw new OperationException("No se pudo registrar el periodo escolar. Intentelo mas tarde", 
                    null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al registrar el periodo escolar", e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException {
        boolean isModified = false;
        String schoolPeriodQuery = "UPDATE PeriodoEscolar " 
                                 + "SET FechaInicio = ? , FechaFin = ? WHERE idPeriodoEscolar = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)){

            preparedStatement.setDate(1, schoolPeriod.getStartDate());
            preparedStatement.setDate(2, schoolPeriod.getEndDate());
            preparedStatement.setInt(3, schoolPeriod.getId());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
            } else {
                throw new OperationException("No se pudo modificar el periodo escolar. Intentelo mas tarde",
                    null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al modificar el periodo escolar", e);
        }
        return isModified;
    }

    @Override
    public boolean existsSchoolPeriod(int idPeriod) throws OperationException {
        String periodQuery = "SELECT 1 FROM PeriodoEscolar WHERE idPeriodoEscolar = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(periodQuery)) {

            preparedStatement.setInt(1, idPeriod);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validando periodo escolar", e);
            throw new OperationException("Error al validar el periodo escolar", e);
        }
    }
}
