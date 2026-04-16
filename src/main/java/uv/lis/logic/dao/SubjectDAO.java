package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISubjectDAO;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;


public class SubjectDAO implements ISubjectDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger logger = Logger.getLogger(SubjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public SubjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public Subject getSubjectById(int foundNrc) throws OperationException{ 
        String subjectQuery = "SELECT * FROM ExperienciaEducativa WHERE NRC = ?;";

        Subject subject = new Subject();
         
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(subjectQuery)) {
            
            
            preparedStatement.setInt(1, foundNrc);
   
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                
                if (resultSet.next()) {
                    int nrc = resultSet.getInt("NRC");
                    String subjectName = resultSet.getString("nombreExperiencia");
                    int idSchoolPeriod = resultSet.getInt("idPeriodoEscolar"); 
                
                    subject.setNrc(nrc);
                    subject.setSubjectName(subjectName);
                    subject.setIdSchoolPeriod(idSchoolPeriod);
                } else {
                    throw new OperationException("No se encontró la Experiencia Educativa con NRC: " + foundNrc, null);
                }
            }
            
            logger.log(Level.INFO, "Busqueda de Experiencia Educativa con NRC {0} realizada.", foundNrc);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar la Experiencia Educativa con NRC: " + foundNrc, e);
            throw new OperationException("No se pudo buscar la Experiencia Educativa. Intentelo mas tarde", null); 

        }
        
        return subject;
    }

    @Override
    public boolean registerSubject(Subject subject) throws OperationException{
        boolean isRegistered = false;
        
        String subjectQuery = "INSERT INTO ExperienciaEducativa (NRC, nombreExperiencia, carrera, idPeriodoEscolar) " 
            + "VALUES (?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {

            preparedStatement.setInt(1, subject.getNrc());
            preparedStatement.setString(2, subject.getSubjectName());
            preparedStatement.setString(3, subject.getCAREER());
            preparedStatement.setInt(4, subject.getIdSchoolPeriod());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                logger.log(Level.INFO, "Experiencia Educativa registrada con éxito. NRC: {0}", subject.getNrc());
            } else {
                throw new OperationException("No se pudo registrar la Experiencia Educativa. Intentelo mas tarde", null);
            } 

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al registrar la Experiencia Educativa con NRC: " + subject.getNrc(), e);
            throw new OperationException("No se pudo registrar la Experiencia Educativa. Intentelo mas tarde", null);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifySubject(Subject subject) throws OperationException{
        boolean isModified = false;
        
        String subjectQuery = "UPDATE ExperienciaEducativa SET nombreExperiencia = ?, carrera = ?, idPeriodoEscolar = ? " 
            + "WHERE NRC = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {

            preparedStatement.setString(1, subject.getSubjectName());
            preparedStatement.setString(2, subject.getCAREER());
            preparedStatement.setInt(3, subject.getIdSchoolPeriod());
            preparedStatement.setInt(4, subject.getNrc()); 
                
            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                logger.log(Level.INFO, "Experiencia Educativa modificada con éxito. NRC: {0}", subject.getNrc());
            } else {
                throw new OperationException("No se pudo modificar la Experiencia Educativa. Intentelo mas tarde", null);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al modificar la Experiencia Educativa con NRC: " 
                + (subject != null ? subject.getNrc() : "null"), e);
            throw new OperationException("No se pudo modificar la Experiencia Educativa con NRC: " + subject.getNrc() +". Intentelo mas tarde", null);
        }
        
        return isModified;
    }
}
