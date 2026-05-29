package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class ProjectDAOTest {

    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS = 0;
    private static final int PROJECT_ID = 1;
    private static final int GENERATED_ID = 7;
    private static final int CAPACITY = 5;
    private static final int ORGANIZATION_ID = 3;
    private static final int SUPERVISOR_ID = 2;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final String PROJECT_NAME = "Sistema de Practicas";
    private static final String SECOND_NAME = "Portal Web";
    private static final String SUPERVISOR_NAME = "Luis Martinez";
    private static final String STUDENT_ID = "S23013127";
    private static final String CONNECTION_ERROR = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProjectDAO projectDAO;

    @BeforeEach
    void setUp() throws Exception {
        projectDAO = new ProjectDAO();
        Field field = ProjectDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(projectDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Project builderProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        project.setDescription("Descripcion del proyecto");
        project.setCapacity(CAPACITY);
        project.setMethodology("Scrum");
        project.setObjective("Objetivo del proyecto");
        project.setActive(true);
        project.setIdAffiliatedOrganization(ORGANIZATION_ID);
        project.setIdSupervisor(SUPERVISOR_ID);
        return project;
    }

    @Test
    void getAllProjects_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID, EXPECTED_LIST_SIZE);
        when(resultSet.getInt("cupo")).thenReturn(CAPACITY, CAPACITY);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME, SECOND_NAME);
        when(resultSet.getString("metodologiaProyecto")).thenReturn("Scrum", "Kanban");
        when(resultSet.getString("objetivo")).thenReturn("Obj A", "Obj B");
        when(resultSet.getString("descripcion")).thenReturn("Desc A", "Desc B");
        when(resultSet.getString("nombreOrganizacion")).thenReturn("UV", "UV");
        when(resultSet.getBoolean("estado")).thenReturn(true, true);

        assertEquals(EXPECTED_LIST_SIZE, projectDAO.getAllProjects().size());
    }

    @Test
    void getAllProjects_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(projectDAO.getAllProjects().isEmpty());
    }

    @Test
    void getAllProjects_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> projectDAO.getAllProjects());
    }

    @Test
    void getProjectByName_found_returnsPresent() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME);

        assertTrue(projectDAO.getProjectByName(PROJECT_NAME).isPresent());
    }

    @Test
    void getProjectByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () -> projectDAO.getProjectByName(PROJECT_NAME));
    }

    @Test
    void getProjectByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> projectDAO.getProjectByName(PROJECT_NAME));
    }

    @Test
    void registerProject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(GENERATED_ID);

        assertTrue(projectDAO.registerProject(builderProject()));
    }

    @Test
    void registerProject_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> projectDAO.registerProject(builderProject()));
    }

    @Test
    void registerProject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.registerProject(builderProject()));
    }

    @Test
    void modifyProject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(projectDAO.modifyProject(builderProject()));
    }

    @Test
    void modifyProject_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> projectDAO.modifyProject(builderProject()));
    }

    @Test
    void modifyProject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.modifyProject(builderProject()));
    }

    @Test
    void inactivateProject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(projectDAO.inactivateProject(builderProject()));
    }

    @Test
    void inactivateProject_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> projectDAO.inactivateProject(builderProject()));
    }

    @Test
    void inactivateProject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.inactivateProject(builderProject()));
    }

    @Test
    void getAllProjectNames_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME, SECOND_NAME);

        assertEquals(EXPECTED_LIST_SIZE, projectDAO.getAllProjectNames().size());
    }

    @Test
    void getAllProjectNames_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(projectDAO.getAllProjectNames().isEmpty());
    }

    @Test
    void getAllProjectNames_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> projectDAO.getAllProjectNames());
    }

    @Test
    void getProjectBySupervisorName_found_returnsPresent() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME);

        assertTrue(projectDAO.getProjectBySupervisorName(SUPERVISOR_NAME).isPresent());
    }

    @Test
    void getProjectBySupervisorName_notFound_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(projectDAO.getProjectBySupervisorName(SUPERVISOR_NAME).isPresent());
    }

    @Test
    void getProjectBySupervisorName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.getProjectBySupervisorName(SUPERVISOR_NAME));
    }

    @Test
    void getProjectNamesByOrganizationId_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME, SECOND_NAME);

        assertEquals(EXPECTED_LIST_SIZE,
            projectDAO.getProjectNamesByOrganizationId(ORGANIZATION_ID).size());
    }

    @Test
    void getProjectNamesByOrganizationId_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(projectDAO.getProjectNamesByOrganizationId(ORGANIZATION_ID).isEmpty());
    }

    @Test
    void getProjectNamesByOrganizationId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.getProjectNamesByOrganizationId(ORGANIZATION_ID));
    }

    @Test
    void getProjectByStudentId_found_returnsPresent() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME);

        assertTrue(projectDAO.getProjectByStudentId(STUDENT_ID).isPresent());
    }

    @Test
    void getProjectByStudentId_notFound_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(projectDAO.getProjectByStudentId(STUDENT_ID).isPresent());
    }

    @Test
    void getProjectByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectDAO.getProjectByStudentId(STUDENT_ID));
    }
}