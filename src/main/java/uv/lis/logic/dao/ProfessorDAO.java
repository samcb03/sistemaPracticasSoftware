package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
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

    public ProfessorDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }
    
    public ProfessorDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public LinkedHashMap<String, String> getAllActiveProfessorsMap() throws OperationException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String query = "SELECT p.numeroPersonal, u.nombre, u.apellidos "  
            + "FROM Profesor p " 
            + "INNER JOIN Usuario u ON p.idUsuario = u.idUsuario " 
            + "WHERE p.estado = 1";

        try (Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String fullName = resultSet.getString("nombre") + " " + resultSet.getString("apellidos");
                String personnelNumber = resultSet.getString("numeroPersonal");
                map.put(fullName, personnelNumber);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener profesores", e);
            throw new OperationException("Error al obtener profesores", e);
        }

        return map;
    }
    
    @Override
    public String getProfessorPersonnelNumberByName(String firstName, String lastName) throws OperationException {
        String personnelNumber = null;
        String professorQuery = "SELECT p.numeroPersonal "
            + "FROM Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario "
            + "WHERE u.nombre = ? AND u.apellidos = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    personnelNumber = resultSet.getString("numeroPersonal");
                } else {
                    LOGGER.log(Level.INFO, "No se encontro profesor");
                    throw new OperationException("No se encontró un profesor con el nombre: " +
                        firstName + " " + lastName, null);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos", e);
            throw new OperationException("Error al obtener el numero de personal del profesor", e);
        }

        return personnelNumber;
    }

    @Override
    public boolean registerProfessor(Professor professor) throws OperationException {
        boolean isRegistered = false;
        
        String professorQuery = "INSERT INTO Profesor (idUsuario, numeroPersonal, idRol, estado) VALUES (?, ?, ?, ?);";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {
            
            preparedStatement.setInt(1, professor.getId());      
            preparedStatement.setString(2, professor.getPersonnelNumber());     

            if (professor.getIsCoordinator()) { 
                preparedStatement.setInt(3, 3);
            } else {
                preparedStatement.setInt(3, 2);
            }

            preparedStatement.setString(4,"1");

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isRegistered = true;
                LOGGER.log(Level.INFO, "Registro de profesor con ID {0} exitosa.", professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo registrar al profesor con numero de personal {0}.", 
                    professor.getPersonnelNumber());
                throw new OperationException("No se pudo registrar al profesor con numero de personal: " 
                    + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al registrar el profesor", e);
        }
        
        return isRegistered;
    }

    @Override
    public boolean modifyProfessor(Professor professor) throws OperationException {
        boolean isModified = false;

        String professorQuery = "UPDATE Profesor p INNER JOIN Usuario u ON p.idUsuario = u.idUsuario" 
            + "SET p.rol = ?, u.nombre = ?, u.apellidos = ? WHERE p.numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStament = databaseConnection.prepareStatement(professorQuery)) {

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
                LOGGER.log(Level.INFO, "Modificacion de profesor con numero de personal {0} exitosa.", 
                    professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo modificar al profesor con numero de personal {0}.", 
                    professor.getPersonnelNumber());
                throw new OperationException("No se pudo modificar al profesor con numero de personal: " 
                    + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al modificar el profesor", e);
        }

        return isModified;
    }

    @Override
    public boolean inactivateProfessor(Professor professor) throws OperationException {
        boolean isInactived = false;

        String professorQuery = "UPDATE Profesor SET estado = 0 WHERE numeroPersonal = ?;";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(professorQuery)) {
            
            preparedStatement.setString(1, professor.getPersonnelNumber());

            if (preparedStatement.executeUpdate() > NO_ROWS_AFFECTED) {
                isInactived = true;
                LOGGER.log(Level.INFO, "Inactivacion de profesor con numero de personal exitosa.", 
                    professor.getPersonnelNumber());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo inactivar al profesor con numero de personal {0}.", 
                    professor.getPersonnelNumber());
                throw new OperationException("No se pudo inactivar al profesor con numero de personal: " 
                    + professor.getPersonnelNumber(), null);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de conexion con la base de datos",e);
            throw new OperationException("Error al inactivar el profesor", e);
        }

        return isInactived;
    }
}
