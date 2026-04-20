package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISubjectDAO;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;


public class SubjectDAO implements ISubjectDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public SubjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public boolean registerSubject(Subject subject) throws OperationException{
        boolean isRegistered = false;
        
        String subjectQuery = "INSERT INTO ExperienciaEducativa (NRC, nombreExperiencia, carrera, idPeriodoEscolar) " 
            + "VALUES (?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {

            preparedStatement.setInt(1, subject.getNrc());
            preparedStatement.setString(2, subject.getSUBJECT_NAME());
            preparedStatement.setString(3, subject.getCAREER());
            preparedStatement.setString(4, subject.getSchoolPeriod());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Experiencia Educativa registrada con éxito. NRC: {0}", subject.getNrc());
            } else {
                throw new OperationException("No se pudo registrar la Experiencia Educativa." 
                    + "Intentelo mas tarde", null);
            } 

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la Experiencia Educativa con NRC: " + subject.getNrc(), e);
            throw new OperationException("No se pudo registrar la Experiencia Educativa. Intentelo mas tarde", 
                e);
        }
        
        return isRegistered;
    }

   @Override
    public ArrayList<Subject> getAllSubjects() throws OperationException {
        ArrayList<Subject> subjects = new ArrayList<>();
        String subjectQuery = "SELECT NRC, idPeriodoEscolar FROM ExperienciaEducativa";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Subject subject = new Subject();
                subject.setNrc(resultSet.getInt("NRC"));
                subject.setSchoolPeriod(String.valueOf(resultSet.getInt("idPeriodoEscolar")));
                subjects.add(subject);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo obtener las Experiencias Educativas. Intentelo mas tarde", 
                e);
        }
        return subjects;
    }
}
