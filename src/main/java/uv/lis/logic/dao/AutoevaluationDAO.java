package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IAutoevaluationDAO;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.exceptions.OperationException;

public class AutoevaluationDAO implements IAutoevaluationDAO {
    private static final Logger LOGGER = Logger.getLogger(AutoevaluationDAO.class.getName());
    private MySQLConnectionManager connectionManager;

    public AutoevaluationDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public AutoevaluationDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Autoevaluation getAutoevaluationData(String studentId) throws OperationException {
        Autoevaluation autoevaluationData = new Autoevaluation();

        String autoevaluationQuery = "SELECT "
                                    + "u.nombre AS nombreAlumno, "         
                                    + "u.apellidos AS apellidosAlumno, "
                                    + "ov.nombreOV AS organizacion, "
                                    + "p.nombre AS proyecto, "
                                    + "rp.nombre AS responsable "
                                    + "FROM Solicita_Proyecto sp "
                                    + "JOIN Alumno a ON sp.matricula = a.matricula "
                                    + "JOIN Usuario u ON a.idUsuario = u.idUsuario "
                                    + "JOIN Proyecto p ON sp.idProyecto = p.idProyecto "
                                    + "JOIN OrganizacionVinculada ov ON p.idOrganizacionVinculada" 
                                    + " = ov.idOrganizacionVinculada "
                                    + "JOIN ResponsableProyecto rp ON p.idResponsableProyecto =" 
                                    + "rp.idResponsableProyecto"
                                    + " WHERE sp.matricula = ? "
                                    + "AND sp.estatus = 2;";

        try (Connection connection = connectionManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(autoevaluationQuery)) {

            preparedStatement.setString(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String studentFullName = resultSet.getString("nombreAlumno")
                        + " " + resultSet.getString("apellidosAlumno");

                    autoevaluationData.setStudentName(studentFullName);
                    autoevaluationData.setIdStudent(studentId);
                    autoevaluationData.setProjectName(resultSet.getString("proyecto"));
                    autoevaluationData.setOrganizationName(resultSet.getString("organizacion"));
                    autoevaluationData.setProjectSupervisorName(resultSet.getString("responsable"));
                } else {
                    throw new OperationException(
                        "No se encontró información del proyecto para la matrícula: " + studentId, null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al recuperar datos de autoevaluacion", e);
            throw new OperationException("Error al recuperar datos de autoevaluacion", e);
        }

        return autoevaluationData;
    }

    @Override
    public boolean registerAutoevaluation(Autoevaluation autoevaluation) throws OperationException {
    boolean isRegistered = false;

        String autoevaluationQuery = "INSERT INTO Autoevaluacion (matricula, participacionProductiva, "
                                    + "conocimientoAplicado, confianzaEnActividades, interesEnActividades, " 
                                    + "apoyoOrganizacional, conocimientoDeReglas, orientacionSupervisor, " 
                                    + "seguimientoEfectivo, alineacionCarrera, importanciaPracticas, " 
                                    + "puntuacionFinal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(autoevaluationQuery)) {
            
            preparedStatement.setString(1, autoevaluation.getIdStudent());
            preparedStatement.setInt(2, autoevaluation.getProductiveParticipation());
            preparedStatement.setInt(3, autoevaluation.getAppliedKnowledge());
            preparedStatement.setInt(4, autoevaluation.getConfidenceInActivities());
            preparedStatement.setInt(5, autoevaluation.getActivitiesInterest());
            preparedStatement.setInt(6, autoevaluation.getOrganizationSupport());
            preparedStatement.setInt(7, autoevaluation.getRulesAwareness());
            preparedStatement.setInt(8, autoevaluation.getSupervisorGuidance());
            preparedStatement.setInt(9, autoevaluation.getEffectiveMonitoring());
            preparedStatement.setInt(10, autoevaluation.getCareerAlignment());
            preparedStatement.setInt(11, autoevaluation.getInternshipImportance());
            preparedStatement.setDouble(12, autoevaluation.getFinalScore());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Autoevaluación registrada para el alumno {0}", 
                    autoevaluation.getIdStudent());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar la autoevaluación para el alumno {0}", 
                    autoevaluation.getIdStudent());
                throw new OperationException("No se pudo registrar la autoevaluación para el alumno: " 
                    + autoevaluation.getIdStudent(), null);
            }   
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar autoevaluación", e);
            throw new OperationException("Error al registrar la autoevaluación", e);
        }

        return isRegistered;
    }

    @Override
    public boolean existsByStudent(String idStudent) throws OperationException {
        boolean exists = false;
        String autoevaluationQuery = "SELECT 1 FROM Autoevaluacion WHERE matricula = ? LIMIT 1";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(autoevaluationQuery)) {

            preparedStatement.setString(1, idStudent);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                exists = resultSet.next();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verificando la existencia de la autoevaluación", e);
            throw new OperationException("Error al verificar la existencia de la autoevaluación", e);
        }
        return exists;
    }

}
