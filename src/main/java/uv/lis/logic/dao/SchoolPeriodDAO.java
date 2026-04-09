package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;


public class SchoolPeriodDAO implements ISchoolPeriodDAO{
    private static final int NO_ROWS_AFFECTED = 0; 
    private static final Logger logger = Logger.getLogger(SchoolPeriodDAO.class.getName());
    private MySQLConnectionManager connectionManager;
    
    public SchoolPeriodDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public SchoolPeriod getSchoolPeriodbyId(int foundIdSchoolPeriod) {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        String schoolPeriodQuery = "SELECT * FROM PeriodoEscolar WHERE idPeriodoEscolar = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)) {

            preparedStatement.setInt(1, foundIdSchoolPeriod);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int idSchoolPeriod = resultSet.getInt("idPeriodoEscolar");
                Date startDate = resultSet.getDate("FechaInicio");
                Date enDate = resultSet.getDate("FechaFin");
                
                schoolPeriod.setId(idSchoolPeriod);
                schoolPeriod.setStartDate(startDate);
                schoolPeriod.setEndDate(enDate);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return schoolPeriod;
    }

    @Override
    public boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) {
        boolean isRegistered = false;
        String schoolPeriodQuery = "INSERT INTO PeriodoEscolar(idPeriodoEscolar,FechaInicio, FechaFin) " 
            + "VALUES(?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)){

            preparedStatement.setInt(1, schoolPeriod.getId());
            preparedStatement.setDate(2, schoolPeriod.getStartDate());
            preparedStatement.setDate(3, schoolPeriod.getEndDate());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED){
                isRegistered = true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isRegistered;
    }

    @Override
    public boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) {
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
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isModified;
    }

    @Override
    public boolean existsSchoolPeriod(int idPeriod) {
        String periodQuery = "SELECT 1 FROM PeriodoEscolar WHERE idPeriodoEscolar = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(periodQuery)) {

            preparedStatement.setInt(1, idPeriod);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); 

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error validando periodo escolar", e);
        }

        return false;
    }
}
