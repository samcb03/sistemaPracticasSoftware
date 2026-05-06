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
    public boolean registerSubject(Subject subject) throws OperationException {
        boolean isRegistered = false;

        String subjectQuery = "INSERT INTO ExperienciaEducativa (NRC, nombreExperiencia, carrera, idPeriodoEscolar) "
            + "VALUES (?, ?, ?, ?);";

        String professorSubjectQuery = "INSERT INTO Profesor_Imparte_Experiencia (NRC, numeroPersonal) "
            + "VALUES (?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection()) {

            databaseConnection.setAutoCommit(false);

            try (PreparedStatement subjectStatement = databaseConnection.prepareStatement(subjectQuery);
                    PreparedStatement professorSubjectStatement 
                    = databaseConnection.prepareStatement(professorSubjectQuery)) {

                subjectStatement.setInt(1, subject.getNrc());
                subjectStatement.setString(2, subject.getSUBJECT_NAME());
                subjectStatement.setString(3, subject.getCAREER());
                subjectStatement.setInt(4, subject.getSchoolPeriodId());

                if (subjectStatement.executeUpdate() == NO_ROWS_AFFECTED) {
                    throw new OperationException("No se pudo registrar la Experiencia Educativa.", null);
                }

                professorSubjectStatement.setInt(1, subject.getNrc());
                professorSubjectStatement.setString(2, subject.getProfessorPersonnelNumber());

                if (professorSubjectStatement.executeUpdate() == NO_ROWS_AFFECTED) {
                    throw new OperationException(
                        "No se pudo asignar el profesor a la Experiencia Educativa.", null);
                }

                databaseConnection.commit();
                isRegistered = true;
                LOGGER.log(Level.INFO, "Experiencia Educativa registrada con éxito.");

            } catch (OperationException | SQLException e) {
                databaseConnection.rollback();
                LOGGER.log(Level.SEVERE, "Error al registrar la Experiencia Educativa", e);
                throw new OperationException("No se pudo registrar la Experiencia Educativa",
                    e);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión al registrar la Experiencia Educativa", e);
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
                subject.setSchoolPeriodId(resultSet.getInt("idPeriodoEscolar"));
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
