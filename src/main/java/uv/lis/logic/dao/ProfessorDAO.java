package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


public class ProfessorDAO extends UserDAO implements IProfessorDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private MySQLConnectionManager connectionManager;
    
    @Override
    public Professor getProfessorByPersonalNumber(String personnelNumber) throws OperationException {
        Professor professor = null;
        String professorQuery = "SELECT p.numeroPersonal, p.rol, u.nombre, u.apellidos "
            + "FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario WHERE p.numeroPersonal = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, personnelNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    professor = new Professor();
                    professor.setPersonnelNumber(resultSet.getString("numeroPersonal")); 
                    professor.setFirstName(resultSet.getString("nombre")); 
                    professor.setLastName(resultSet.getString("apellidos"));
                    professor.setIsCoordinator(false); 

                    LOGGER.log(Level.INFO, "Busqueda de Profesor con numero de personal obtenido con exito", 
                        professor.getPersonnelNumber());
                } else {
                    LOGGER.log(Level.INFO, "No se encontro un profesor con el numero de personal {0}.", personnelNumber);
                    throw new OperationException("No se encontró un profesor con el numero de personal: " + personnelNumber, null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al obtener el profesor", null);
        }

        return professor;
    }

    @Override
    public boolean registerProfessor(Professor professor) throws OperationException {
        boolean isRegistered = false;
        
        String proffesorQuery = "INSERT INTO Profesor (idUsuario, numeroPersonal, rol, estado) VALUES (?, ?, ?,?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(proffesorQuery)) {
            
            preparedStatement.setInt(1, professor.getId());      
            preparedStatement.setString(2, professor.getPersonnelNumber());     

            if (professor.getIsCoordinator()) { 
                preparedStatement.setString(3, "Coordinador");
            } else {
                preparedStatement.setString(3, "Maestro");
            }

            preparedStatement.setString(4,"1");

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de profesor con ID {0} exitosa.", professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar al profesor con numero de personal {0}.", professor.getPersonnelNumber());
                throw new OperationException("No se pudo registrar al profesor con numero de personal: " + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al registrar el profesor", null);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifyProfessor(Professor professor) throws OperationException {
        boolean isModified = false;

        String proffesorQuery = "UPDATE Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario SET p.rol = ?," 
            + "u.nombre = ?, u.apellidos = ? WHERE p.numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStament = databaseConnection.prepareStatement(proffesorQuery)) {

            if (professor.getIsCoordinator()) { 
                preparedStament.setString(1, "Coordinador");
            } else {
                preparedStament.setString(1, "Maestro");
            }

            preparedStament.setString(2, professor.getFirstName());
            preparedStament.setString(3, professor.getLastName());
            
            preparedStament.setString(4, professor.getPersonnelNumber()); 

            if (preparedStament.executeUpdate() > NO_ROWS_AFFECTED) {
                isModified = true;
                LOGGER.log(Level.INFO, "Modificacion de profesor con numero de personal {0} exitosa.", professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar al profesor con numero de personal {0}.", professor.getPersonnelNumber());
                throw new OperationException("No se pudo modificar al profesor con numero de personal: " + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al modificar el profesor", null);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProfessor(Professor professor) throws OperationException {
        boolean isInactived = false;

        String proffesorQuery = "UPDATE Profesor SET estado = 0 WHERE numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(proffesorQuery)) {
            
            preparedStatement.setString(1, professor.getPersonnelNumber());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactived = true;
                LOGGER.log(Level.INFO, "Inactivacion de profesor con numero de personal exitosa.", 
                    professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo inactivar al profesor con numero de personal {0}.", professor.getPersonnelNumber());
                throw new OperationException("No se pudo inactivar al profesor con numero de personal: " + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al inactivar el profesor", null);

        }

        return isInactived;
    }
}
