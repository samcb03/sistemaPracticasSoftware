package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;

public class SchoolPeriodDAO implements ISchoolPeriodDAO{
        private static final Logger logger = Logger.getLogger(SchoolPeriodDAO.class.getName());
    

    @Override
    public List<SchoolPeriod> getSchoolPeriodbyId(int foundIdSchoolPeriod) {
        List<SchoolPeriod> schoolPeriods = new ArrayList<>();
        String schoolPeriodQuery = "SELECT * FROM PeriodoEscolar WHERE idPeriodoEscolar = ?;";

        try (Connection databaseConnection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(schoolPeriodQuery)) {

            preparedStatement.setInt(1, foundIdSchoolPeriod);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int idSchoolPeriod = resultSet.getInt("idPeriodoEscolar");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date enDate = resultSet.getDate("Fecha de fin");
                schoolPeriods.add(new SchoolPeriod(idSchoolPeriod, startDate, enDate));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return schoolPeriods;
    }

    @Override
    public boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) {
        boolean isRegistered = false;
        String schoolPeriodQuery = "INSERT INTO PeriodoEscolar(idPeriodoEscolar,FechaInicio, FechaFin) VALUES(?,?);";

        try (Connection connection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(schoolPeriodQuery)){

            preparedStatement.setInt(1, schoolPeriod.getId());
            preparedStatement.setDate(2, schoolPeriod.getStartDate());
            preparedStatement.setDate(3, schoolPeriod.getEndDate());

            if (preparedStatement.executeUpdate() > 0){
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
        String schoolPeriodQuery = "UPDATE PeriodoEscolar" +
                                   "SET FechaInicio = ? , FechaFin = ?" + 
                                   "WHERE idPeriodoEscolar = ?;";

        try (Connection connection = MySQLConnectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(schoolPeriodQuery)){

            preparedStatement.setDate(1, schoolPeriod.getStartDate());
            preparedStatement.setDate(2, schoolPeriod.getEndDate());
            preparedStatement.setInt(3, schoolPeriod.getId());

            if (preparedStatement.executeUpdate() > 0) {
                isModified = true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexion con la base de datos",e);
        }
        return isModified;
    }
}
