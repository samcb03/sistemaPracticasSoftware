package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISubjectDAO;
import uv.lis.logic.dto.Subject;

public class SubjectDAO implements ISubjectDAO{

    @Override
    public List<Subject> getSubjectbyId(int FoundNrc) {
        List<Subject> subjects = new ArrayList<>();
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            
            String subjectQuery = "SELECT * FROM ExperienciaEducativa WHERE NRC = ?;";
            
            PreparedStatement preparedStatement = connection.prepareStatement(subjectQuery);
            preparedStatement.setInt(1, FoundNrc);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                String nrc = resultSet.getString("NRC");
                String subjectName = resultSet.getString("Nombre experiencia educativa");
                int idSchoolPeriod = resultSet.getInt("IdPeriodoEscolar");
                
                subjects.add(new Subject(nrc, subjectName, idSchoolPeriod));
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error en la BD: " + e.getMessage());
        }
        return subjects;
    }

    @Override
    public boolean registerSubject(Subject subject) {
        if (subject == null){
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String subjectQuery = "INSERT INTO ExperienciaEducativa(NRC, nombreExperiencia, carrera, idPeriodoEscolar)" +
            "VALUES(?,?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(subjectQuery);
            preparedStatement.setString(1, subject.getNrc());
            preparedStatement.setString(2, subject.getSubjectName());
            preparedStatement.setString(3, subject.getCAREER());
            preparedStatement.setInt(4, subject.getIdSchoolPeriod());
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean modifySubject(Subject subject) {
        if (subject == null){
            return false;
        }
        try {
            Connection connection = MySQLConnectionManager.getConnection();
            String subjectQuery = "UPDATE ExperienciaEducativa" +
            "SET nombreExperiencia = ? ,carrera = ? ,idPeriodoEscolar = ?)" +
            "WHERE NRC = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(subjectQuery);
            preparedStatement.setString(1, subject.getSubjectName());
            preparedStatement.setString(2, subject.getCAREER());
            preparedStatement.setInt(3, subject.getIdSchoolPeriod());
            preparedStatement.setString(4, subject.getNrc());
            preparedStatement.executeUpdate();
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    
}
