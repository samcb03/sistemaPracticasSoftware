package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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
import java.util.LinkedHashMap;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


@ExtendWith(MockitoExtension.class)
class ProfessorDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;

    @Mock
    private Connection databaseConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ProfessorDAO professorDAO;

    @BeforeEach
    void setUp() throws Exception {
        professorDAO = new ProfessorDAO();
        Field field = ProfessorDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(professorDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void getAllActiveProfessorsMap_withResults_returnsPopulatedMap() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn("Juan", "Ana");
        when(resultSet.getString("apellidos")).thenReturn("Pérez", "García");
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001", "UV-002");

        LinkedHashMap<String, String> result = professorDAO.getAllActiveProfessorsMap();

        assertEquals(2, result.size());
        assertEquals("UV-001", result.get("Juan Pérez"));
        assertEquals("UV-002", result.get("Ana García"));
    }

    @Test
    void getAllActiveProfessorsMap_emptyResultSet_returnsEmptyMap() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        LinkedHashMap<String, String> result = professorDAO.getAllActiveProfessorsMap();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllActiveProfessorsMap_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.getAllActiveProfessorsMap()
        );
        assertTrue(exception.getMessage().contains("Error al obtener profesores"));
    }

    @Test
    void getProfessorPersonnelNumberByName_found_returnsPersonnelNumber() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001");

        String result = professorDAO.getProfessorPersonnelNumberByName("Juan", "Pérez");

        assertEquals("UV-001", result);
    }

    @Test
    void getProfessorPersonnelNumberByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.getProfessorPersonnelNumberByName("No", "Existe")
        );
        assertTrue(exception.getMessage().contains("No se encontró un profesor con el nombre: No Existe"));
    }

    @Test
    void getProfessorPersonnelNumberByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.getProfessorPersonnelNumberByName("Juan", "Pérez")
        );
        assertTrue(exception.getMessage().contains("Error al obtener el numero de personal del profesor"));
    }

    @Test
    void registerProfessor_professor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);
        professor.setId(10);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.registerProfessor(professor));
        verify(preparedStatement).setInt(3, 2);
    }

    @Test
    void registerProfessor_coordinator_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-002", "Carlos", "López", true);
        professor.setId(11);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.registerProfessor(professor));
        verify(preparedStatement).setInt(3, 3);
    }

    @Test
    void registerProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-003", "Luis", "Martínez", false);
        professor.setId(12);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(professor)
        );
        assertTrue(exception.getMessage().contains("UV-003"));
    }

    @Test
    void registerProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Error de BD"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(buildProfessor("UV-001", "Ana", "García", false))
        );
        assertTrue(exception.getMessage().contains("Error al registrar el profesor"));
    }

    @Test
    void modifyProfessor_professor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García Actualizada", false);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.modifyProfessor(professor));
        verify(preparedStatement).setString(1, "Maestro");
        verify(preparedStatement).setString(2, "Ana");
        verify(preparedStatement).setString(3, "García Actualizada");
        verify(preparedStatement).setString(4, "UV-001");
    }

    @Test
    void modifyProfessor_coordinator_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-002", "Carlos", "López", true);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.modifyProfessor(professor));
        verify(preparedStatement).setString(1, "Coordinador");
    }

    @Test
    void modifyProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "Sin", "Registro", false);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(professor)
        );
        assertTrue(exception.getMessage().contains("UV-999"));
    }

    @Test
    void modifyProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(buildProfessor("UV-001", "Ana", "García", false))
        );
        assertTrue(exception.getMessage().contains("Error al modificar el profesor"));
    }

    @Test
    void inactivateProfessor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(professorDAO.inactivateProfessor(professor));
        verify(preparedStatement).setString(1, "UV-001");
    }

    @Test
    void inactivateProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "No", "Existe", false);

        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(professor)
        );
        assertTrue(exception.getMessage().contains("UV-999"));
    }

    @Test
    void inactivateProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(buildProfessor("UV-001", "Ana", "García", false))
        );
        assertTrue(exception.getMessage().contains("Error al inactivar el profesor"));
    }

    private Professor buildProfessor(String number, String name, String lastName, boolean isCoordinator) {
        Professor professor = new Professor();
        professor.setPersonnelNumber(number);
        professor.setFirstName(name);
        professor.setLastName(lastName);
        professor.setIsCoordinator(isCoordinator);
        return professor;
    }
}