package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISubjectDAO;
import uv.lis.logic.dto.Subject;

public class SubjectDAO implements ISubjectDAO {

    private static final Logger logger = Logger.getLogger(SubjectDAO.class.getName());

    @Override
    public List<Subject> getSubjectbyId(int foundNrc) { 
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM ExperienciaEducativa WHERE NRC = ?;";

        try (Connection connection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                
            preparedStatement.setInt(1, foundNrc);
   
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                
                if (resultSet.next()) {
                    String nrc = resultSet.getString("NRC");
                    String subjectName = resultSet.getString("nombreExperiencia");
                    int idSchoolPeriod = resultSet.getInt("idPeriodoEscolar"); 
                    
                    subjects.add(new Subject(nrc, subjectName, idSchoolPeriod));
                }
            }
            
            logger.log(Level.INFO, "Busqueda de Experiencia Educativa con NRC {0} realizada.", foundNrc);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar la Experiencia Educativa con NRC: " + foundNrc, e);
        }
        
        return subjects;
    }

    @Override
    public boolean registerSubject(Subject subject) {
        boolean isRegistered = false;
        
        String query = "INSERT INTO ExperienciaEducativa (NRC, nombreExperiencia, carrera, idPeriodoEscolar) " +
                       "VALUES (?, ?, ?, ?);";

        try (Connection connection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, subject.getNrc());
            preparedStatement.setString(2, subject.getSubjectName());
            preparedStatement.setString(3, subject.getCAREER());
            preparedStatement.setInt(4, subject.getIdSchoolPeriod());

            if (preparedStatement.executeUpdate() > 0) {
                isRegistered = true;
                logger.log(Level.INFO, "Experiencia Educativa registrada con éxito. NRC: {0}", subject.getNrc());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al registrar la Experiencia Educativa con NRC: " + subject.getNrc(), e);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifySubject(Subject subject) {
        boolean isModified = false;
        
        String query = "UPDATE ExperienciaEducativa SET nombreExperiencia = ?, carrera = ?, idPeriodoEscolar = ? " +
                       "WHERE NRC = ?;";

        try (Connection connection = MySQLConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setString(1, subject.getSubjectName());
            preparedStatement.setString(2, subject.getCAREER());
            preparedStatement.setInt(3, subject.getIdSchoolPeriod());
            preparedStatement.setString(4, subject.getNrc()); 
            
            if (preparedStatement.executeUpdate() > 0) {
                isModified = true;
                logger.log(Level.INFO, "Experiencia Educativa modificada con éxito. NRC: {0}", subject.getNrc());
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al modificar la Experiencia Educativa con NRC: " + (subject != null ? subject.getNrc() : "null"), e);
        }
        
        return isModified;
    }
}