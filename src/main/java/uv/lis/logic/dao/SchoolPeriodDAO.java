package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISchoolPeriodDAO;
import uv.lis.logic.dto.SchoolPeriod;

public class SchoolPeriodDAO implements ISchoolPeriodDAO{

    @Override
    public List<SchoolPeriod> getSchoolPeriodbyId(int foundIdSchoolPeriod) {
        List<SchoolPeriod> schoolPeriods = new ArrayList<>();
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            
            String schoolPeriodQuery = "SELECT * FROM PeriodoEscolar WHERE idPeriodoEscolar = ?;";
            
            PreparedStatement preparedStatement = connection.prepareStatement(schoolPeriodQuery);
            preparedStatement.setInt(1, foundIdSchoolPeriod);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                int idSchoolPeriod = resultSet.getInt("idPeriodoEscolar");
                Date startDate = resultSet.getDate("Fecha de inicio");
                Date enDate = resultSet.getDate("Fecha de fin");
                schoolPeriods.add(new SchoolPeriod(idSchoolPeriod, startDate, enDate));
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error en la BD: " + e.getMessage());
        }
        return schoolPeriods;
    }

    @Override
    public boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) {
        if (schoolPeriod == null){
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String schoolPeriodQuery = "INSERT INTO PeriodoEscolar(idPeriodoEscolar,FechaInicio, FechaFin) VALUES(?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(schoolPeriodQuery);
            preparedStatement.setInt(1, schoolPeriod.getId());
            preparedStatement.setDate(2, schoolPeriod.getStartDate());
            preparedStatement.setDate(3, schoolPeriod.getEndDate());
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) {
        if (schoolPeriod == null){
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String schoolPeriodQuery = "UPDATE PeriodoEscolar" +
            "SET FechaInicio = ? , FechaFin = ?" + 
            "WHERE idPeriodoEscolar = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(schoolPeriodQuery);
            preparedStatement.setDate(1, schoolPeriod.getStartDate());
            preparedStatement.setDate(2, schoolPeriod.getEndDate());
            preparedStatement.setInt(3, schoolPeriod.getId());
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
