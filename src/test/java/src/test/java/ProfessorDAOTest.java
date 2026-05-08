package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


@ExtendWith(MockitoExtension.class)
class ProfessorDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProfessorDAO professorDAO;

    @BeforeEach
    void setUp() throws Exception {
        professorDAO = new ProfessorDAO();
        Field field = ProfessorDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(professorDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Professor buildProfessor(String number, String name, String lastName, boolean isCoordinator) {
        Professor professor = new Professor();
        professor.setPersonnelNumber(number);
        professor.setFirstName(name);
        professor.setLastName(lastName);
        professor.setIsCoordinator(isCoordinator);
        return professor;
    }

    @Test
    void getAllActiveProfessorsMap_withResults_returnsPopulatedMap() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn("Juan", "Ana");
        when(resultSet.getString("apellidos")).thenReturn("Pérez", "García");
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001", "UV-002");

        assertEquals(2, professorDAO.getAllActiveProfessorsMap().size());
    }

    @Test
    void getAllActiveProfessorsMap_emptyResultSet_returnsEmptyMap() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(professorDAO.getAllActiveProfessorsMap().isEmpty());
    }

    @Test
    void getAllActiveProfessorsMap_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            professorDAO.getAllActiveProfessorsMap());
    }

    @Test
    void getProfessorPersonnelNumberByName_found_returnsPersonnelNumber() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001");

        assertEquals("UV-001", professorDAO.getProfessorPersonnelNumberByName("Juan", 
            "Pérez"));
    }

    @Test
    void getProfessorPersonnelNumberByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () ->
            professorDAO.getProfessorPersonnelNumberByName("No", "Existe"));
    }

    @Test
    void getProfessorPersonnelNumberByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            professorDAO.getProfessorPersonnelNumberByName("Juan", "Pérez"));
    }

    @Test
    void registerProfessor_professor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);
        professor.setId(10);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.registerProfessor(professor));
    }

    @Test
    void registerProfessor_coordinator_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-002", "Carlos", "López", true);
        professor.setId(11);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.registerProfessor(professor));
    }

    @Test
    void registerProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-003", "Luis", "Martínez", false);
        professor.setId(12);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(professor));
    }

    @Test
    void registerProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Error de BD"));

        assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(buildProfessor("UV-001", "Ana", "García", 
                false)));
    }

    @Test
    void modifyProfessor_professor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García Actualizada", 
            false);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.modifyProfessor(professor));
    }

    @Test
    void modifyProfessor_coordinator_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-002", "Carlos", "López", true);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.modifyProfessor(professor));
    }

    @Test
    void modifyProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "Sin", "Registro", false);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(professor));
    }

    @Test
    void modifyProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(buildProfessor("UV-001", "Ana", "García", 
                false)));
    }

    @Test
    void inactivateProfessor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", 
        false);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.inactivateProfessor(professor));
    }

    @Test
    void inactivateProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "No", "Existe", false);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(professor));
    }

    @Test
    void inactivateProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(buildProfessor("UV-001", "Ana", "García", 
                false)));
    }
}