package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class RequestProjectDAOTest {  

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private RequestProjectDAO requestProjectDAO;
     @BeforeEach
    void setUp() throws Exception {
        requestProjectDAO = new RequestProjectDAO();
        Field field = RequestProjectDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(requestProjectDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void getActiveRequestCountByStudentId_studentHasRequests_returnsCount() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(2);

        int result = requestProjectDAO.getActiveRequestCountByStudentId("S23013127");

        assertEquals(2, result);
    }

    @Test
    void getActiveRequestCountByStudentId_studentHasNoRequests_returnsZero() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(0);

        int result = requestProjectDAO.getActiveRequestCountByStudentId("S23013127");

        assertEquals(0, result);
    }

    @Test
    void getActiveRequestCountByStudentId_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getActiveRequestCountByStudentId("S23013127"));
    }

    @Test
    void getAvailableProjects_projectsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(1);
        when(resultSet.getString("nombre")).thenReturn("Proyecto Test");
        when(resultSet.getString("descripcion")).thenReturn("Descripcion test");
        when(resultSet.getInt("cupo")).thenReturn(5);
        when(resultSet.getString("metodologiaProyecto")).thenReturn("Scrum");
        when(resultSet.getString("objetivo")).thenReturn("Objetivo test");

        List<Project> result = requestProjectDAO.getAvailableProjects();

        assertFalse(result.isEmpty());
    }

    @Test
    void getAvailableProjects_noProjects_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Project> result = requestProjectDAO.getAvailableProjects();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableProjects_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getAvailableProjects());
    }

    @Test
    void hasAlreadyRequested_studentAlreadyRequested_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(1);

        boolean result = requestProjectDAO.hasAlreadyRequested("S23013127", 1);

        assertTrue(result);
    }

    @Test
    void hasAlreadyRequested_studentHasNotRequested_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(0);

        boolean result = requestProjectDAO.hasAlreadyRequested("S23013127", 1);

        assertFalse(result);
    }

    @Test
    void hasAlreadyRequested_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.hasAlreadyRequested("S23013127", 1));
    }

    @Test
    void hasAvailableCapacity_projectHasCapacity_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("cupo")).thenReturn(5);
        when(resultSet.getInt("solicitudes")).thenReturn(2);

        boolean result = requestProjectDAO.hasAvailableCapacity(1);

        assertTrue(result);
    }

    @Test
    void hasAvailableCapacity_projectIsFull_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("cupo")).thenReturn(3);
        when(resultSet.getInt("solicitudes")).thenReturn(3);

        boolean result = requestProjectDAO.hasAvailableCapacity(1);

        assertFalse(result);
    }

    @Test
    void hasAvailableCapacity_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.hasAvailableCapacity(1));
    }

    @Test
    void requestProject_validRequest_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = requestProjectDAO.requestProject("S23013127", 1);

        assertTrue(result);
    }

    @Test
    void requestProject_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = requestProjectDAO.requestProject("S23013127", 1);

        assertFalse(result);
    }

    @Test
    void requestProject_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.requestProject("S23013127", 1));
    }

    @Test
    void validateProjectRequest_maxRequestsReached_returnsError() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(Integer.MAX_VALUE);

        Optional<String> result = requestProjectDAO.validateProjectRequest("S23013127", 1);

        assertTrue(result.isPresent());
    }

    @Test
    void validateProjectRequest_validRequest_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(0);
        when(resultSet.getInt("cupo")).thenReturn(5);
        when(resultSet.getInt("solicitudes")).thenReturn(1);

        Optional<String> result = requestProjectDAO.validateProjectRequest("S23013127", 1);

        assertFalse(result.isPresent());
    }

    @Test
    void getApplicantsByProjectId_applicantsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getString("nombre")).thenReturn("Ana");
        when(resultSet.getString("apellidos")).thenReturn("Gomez Ramirez");

        List<Student> result = requestProjectDAO.getApplicantsByProjectId(1);

        assertFalse(result.isEmpty());
    }

    @Test
    void getApplicantsByProjectId_noApplicants_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Student> result = requestProjectDAO.getApplicantsByProjectId(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getApplicantsByProjectId_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getApplicantsByProjectId(1));
    }

    @Test
    void getProjectAssignedToStudent_projectFound_returnsProjectName() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("nombre")).thenReturn("Proyecto Test");

        String result = requestProjectDAO.getProjectAssignedToStudent("S23013127");

        assertEquals("Proyecto Test", result);
    }

    @Test
    void getProjectAssignedToStudent_noProjectAssigned_returnsDefaultMessage() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String result = requestProjectDAO.getProjectAssignedToStudent("S23013127");

        assertEquals("Sin proyecto asignado", result);
    }

    @Test
    void getProjectAssignedToStudent_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getProjectAssignedToStudent("S23013127"));
    }

    @Test
    void getAssignedStudentsByProjectId_studentsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getString("nombre")).thenReturn("Ana");
        when(resultSet.getString("apellidos")).thenReturn("Gomez Ramirez");

        ArrayList<Student> result = requestProjectDAO.getAssignedStudentsByProjectId(1);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAssignedStudentsByProjectId_noStudents_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ArrayList<Student> result = requestProjectDAO.getAssignedStudentsByProjectId(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAssignedStudentsByProjectId_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getAssignedStudentsByProjectId(1));
    }

    @Test
    void unassignStudentFromProject_validStudent_executesWithoutException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        requestProjectDAO.unassignStudentFromProject("S23013127");

        assertTrue(true);
    }

    @Test
    void unassignStudentFromProject_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> requestProjectDAO.unassignStudentFromProject("S23013127"));
    }
}
