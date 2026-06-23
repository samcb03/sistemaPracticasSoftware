package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_VALUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.ISubjectDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public class SubjectDAO implements ISubjectDAO {
    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public SubjectDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public SubjectDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean registerSubject(Subject subject) throws OperationException {
        boolean isRegistered = false;

        String subjectQuery = "INSERT INTO ExperienciaEducativa (NRC, nombreExperiencia, carrera, idPeriodoEscolar," 
                            + "seccion) "
                            + "VALUES (?, ?, ?, ?, ?);";

        String professorSubjectQuery = "INSERT INTO Profesor_Imparte_Experiencia (NRC, numeroPersonal) "
                                     + "VALUES (?, ?) "
                                     + "ON DUPLICATE KEY UPDATE estaActiva = TRUE;";

        try (Connection databaseConnection = connectionManager.getConnection()) {

            databaseConnection.setAutoCommit(false);

            try (PreparedStatement subjectStatement = databaseConnection.prepareStatement(subjectQuery);
                PreparedStatement professorSubjectStatement = 
                databaseConnection.prepareStatement(professorSubjectQuery)) {

                subjectStatement.setInt(1, subject.getNrc());
                subjectStatement.setString(2, Subject.getSubjectName());
                subjectStatement.setString(3, Subject.getCareer());
                subjectStatement.setInt(4, subject.getSchoolPeriodId());
                subjectStatement.setString(5, subject.getSection());
                int subjectRows = subjectStatement.executeUpdate();

                professorSubjectStatement.setInt(1, subject.getNrc());
                professorSubjectStatement.setString(2, subject.getProfessorPersonnelNumber());
                int professorRows = professorSubjectStatement.executeUpdate();

                boolean bothInserted = subjectRows != NO_VALUE && professorRows != NO_VALUE;

                if (bothInserted) {
                    databaseConnection.commit();
                    isRegistered = true;
                    LOGGER.log(Level.INFO, "Experiencia Educativa registrada con éxito.");
                } else {
                    databaseConnection.rollback();
                    LOGGER.log(Level.WARNING, "No se afectaron filas al registrar la Experiencia Educativa.");
                }

            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexión al registrar la Experiencia Educativa", e);
            throw new OperationException("No se pudo registrar la Experiencia Educativa. Intentelo mas tarde", 
                e);
        }

        return isRegistered;
    }
    
    @Override
    public ArrayList<String> getAllSubjectsNrcName() throws OperationException {
        ArrayList<String> subjects = new ArrayList<>();
        String subjectQuery = "SELECT ee.NRC, ee.nombreExperiencia, pe.nombre as PeriodoEscolar "
                            + "FROM ExperienciaEducativa ee "
                            + "INNER JOIN PeriodoEscolar pe "
                            + "ON ee.idPeriodoEscolar = pe.idPeriodoEscolar "
                            + "WHERE pe.FechaFin IS NULL OR pe.FechaFin >= CURDATE()";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String formatted = resultSet.getInt("NRC") + " - "
                                 + resultSet.getString("nombreExperiencia") + " - "
                                 + resultSet.getString("PeriodoEscolar");
                subjects.add(formatted);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("No se pudo obtener las Experiencias Educativas", e);
        }
        return subjects;
    }

    @Override
    public boolean assignStudentToSubject(String studentId, int subjectNrc) throws OperationException {
        boolean isAssigned = false;
        String subjectQuery = "INSERT INTO alumno_esta_ee (matricula, NRC) VALUES (?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {
            
            preparedStatement.setString(1, studentId);
            preparedStatement.setInt(2, subjectNrc);

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                isAssigned = true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al asignar alumno a la experiencia educativa", e);
            throw new OperationException("No se pudo asignar el alumno a la experiencia educativa", e);
        }
        return isAssigned;
    }

    @Override
    public String getSubjectNrcByStudentID(String studentID) throws OperationException {
        String subjectNRC = "No tiene asignada una experiencia";
        String subjectQuery = "SELECT ee.NRC FROM ExperienciaEducativa ee "
                            + "JOIN alumno_esta_ee aee ON ee.NRC = aee.NRC "
                            + "WHERE aee.matricula = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {
                
            preparedStatement.setString(1, studentID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    subjectNRC = resultSet.getString("NRC");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al asignar alumno a la experiencia educativa", e);
            throw new OperationException("No se pudo asignar el alumno a la experiencia educativa", e);
        }
        return subjectNRC;
    }

    @Override
    public void unassignProfessorFromSubject(String personnelNumber) throws OperationException {
        String subjectQuery = "UPDATE Profesor_Imparte_Experiencia "
                            + "SET estaActiva = FALSE WHERE numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {
            
            preparedStatement.setString(1, personnelNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al desasignar profesor a experiencia educativa", e);
            throw new OperationException("Error al desasignar profesor a experiencia educativa", e);
        }
    }

    @Override
    public ArrayList<Subject> getSubjectsByProfessor(String personalNumber)throws OperationException {
        ArrayList<Subject> subjects = new ArrayList<>();
        String subjectQuery = "SELECT ee.nrc, ee.nombreExperiencia, pe.nombre, ee.idPeriodoEscolar "
                            + "FROM ExperienciaEducativa ee "
                            + "INNER JOIN Profesor_Imparte_Experiencia i ON ee.nrc = i.nrc "
                            + "INNER JOIN PeriodoEscolar pe ON pe.idPeriodoEscolar = ee.idPeriodoEscolar "
                            + "WHERE i.numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {

            preparedStatement.setString(1, personalNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Subject subject = new Subject();
                    subject.setNrc(resultSet.getInt("nrc"));
                    subject.setName(resultSet.getString("nombreExperiencia"));
                    subject.setSchoolPeriodName(resultSet.getString("nombre"));
                    subject.setSchoolPeriodId(resultSet.getInt("idPeriodoEscolar"));
                    subjects.add(subject);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al consultar experiencias educativas", sqlException);
            throw new OperationException("Error al cargar las experiencias educativas. "
                + "Intente más tarde", sqlException);
        }
        return subjects;
    }

    @Override
    public ArrayList<Student> getEnrolledStudentsBySubject(int nrc) throws OperationException {
        ArrayList<Student> enrolledStudents = new ArrayList<>();
        String subjectQuery = "SELECT a.matricula, u.nombre, u.apellidos "
                            + "FROM Alumno a "
                            + "INNER JOIN Usuario u ON a.idUsuario = u.idUsuario "
                            + "INNER JOIN alumno_esta_ee aee ON a.matricula = aee.matricula "
                            + "WHERE aee.NRC = ? "
                            + "ORDER BY u.apellidos, u.nombre";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(subjectQuery)) {

            preparedStatement.setInt(1, nrc);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdStudent(resultSet.getString("matricula"));
                    student.setFirstName(resultSet.getString("nombre"));
                    student.setLastName(resultSet.getString("apellidos"));
                    enrolledStudents.add(student);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener los alumnos inscritos", sqlException);
            throw new OperationException("No se pudo obtener la lista de alumnos. Intente más tarde",
                sqlException);
        }

        return enrolledStudents;
    }

    @Override
    public boolean isSectionTakenInPeriod(int periodId, String section) throws OperationException {
        boolean isTaken = false;
        String sectionQuery = "SELECT COUNT(*) FROM ExperienciaEducativa "
                            + "WHERE idPeriodoEscolar = ? AND seccion = ?";
 
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(sectionQuery)) {
 
            preparedStatement.setInt(1, periodId);
            preparedStatement.setString(2, section);
 
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isTaken = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la sección en el periodo escolar", e);
            throw new OperationException("No se pudo verificar la disponibilidad de la sección", e);
        }
 
        return isTaken;
    }
}