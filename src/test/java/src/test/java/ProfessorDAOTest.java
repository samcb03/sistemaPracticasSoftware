package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


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
        MockitoAnnotations.openMocks(this);
        professorDAO = new ProfessorDAO();
        Field field = ProfessorDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(professorDAO, connectionManager);
    }

    @Test
    void getProfessorByPersonalNumber_successful_returnsProfessor() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001");
        when(resultSet.getString("nombre")).thenReturn("Juan");
        when(resultSet.getString("apellidos")).thenReturn("Pérez");

        assertEquals(buildProfessor("UV-001", "Juan", "Pérez", false), 
            professorDAO.getProfessorByPersonalNumber("UV-001"));
    }

    @Test
    void getProfessorByPersonalNumber_notFound_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.getProfessorByPersonalNumber("UV-999")
        );
        assertTrue(exception.getMessage().contains("UV-999"));
    }

    @Test
    void getProfessorByPersonalNumber_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo de conexión"));

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.getProfessorByPersonalNumber("UV-001")
        );
        assertTrue(exceeption.getMessage().contains("Error al obtener el profesor"));
    }

    @Test
    void registerProfessor_teacherSuccessful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);
        professor.setId(10);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = professorDAO.registerProfessor(professor);

        assertTrue(result);
        verify(preparedStatement).setString(3, "Maestro");
    }

    @Test
    void registerProfessor_coordinatorSuccessful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-002", "Carlos", "López", true);
        professor.setId(11);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = professorDAO.registerProfessor(professor);

        assertTrue(result);
        verify(preparedStatement).setString(3, "Coordinador");
    }

    @Test
    void registerProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-003", "Luis", "Martínez", false);
        professor.setId(12);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(professor)
        );
        assertTrue(exceeption.getMessage().contains("UV-003"));
    }

    @Test
    void registerProfessor_sqlError_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);
        professor.setId(10);

        when(connectionManager.getConnection()).thenThrow(new SQLException("Error de BD"));

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.registerProfessor(professor)
        );
        assertTrue(exceeption.getMessage().contains("Error al registrar el profesor"));
    }

    @Test
    void modifyProfessor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García Actualizada",
            false);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = professorDAO.modifyProfessor(professor);

        assertTrue(result);
        verify(preparedStatement).setString(1, "Maestro");
        verify(preparedStatement).setString(2, "Ana");
        verify(preparedStatement).setString(3, "García Actualizada");
    }

    @Test
    void modifyProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "Sin", "Registro", false);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(professor)
        );
        assertTrue(exceeption.getMessage().contains("UV-999"));
    }

    @Test
    void modifyProfessor_sqlError_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);

        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.modifyProfessor(professor)
        );
        assertTrue(exceeption.getMessage().contains("Error al modificar el profesor"));
    }

    @Test
    void inactivateProfessor_successful_returnsTrue() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = professorDAO.inactivateProfessor(professor);

        assertTrue(result);
        verify(preparedStatement).setString(1, "UV-001");
    }

    @Test
    void inactivateProfessor_noRowsAffected_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-999", "No", "Existe", false);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        

        OperationException exceeption = assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(professor)
        );
        assertTrue(exceeption.getMessage().contains("UV-999"));
    }

    @Test
    void inactivateProfessor_sqlError_throwsOperationException() throws Exception {
        Professor professor = buildProfessor("UV-001", "Ana", "García", false);

        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            professorDAO.inactivateProfessor(professor)
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