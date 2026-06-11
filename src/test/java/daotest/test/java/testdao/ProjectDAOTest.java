package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

class ProjectDAOTest {

    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS = 0;
    private static final int PROJECT_ID = 1;
    private static final int SECOND_PROJECT_ID = 2;
    private static final int GENERATED_ID = 7;
    private static final int CAPACITY = 5;
    private static final int ORGANIZATION_ID = 3;
    private static final int SUPERVISOR_ID = 4;

    private static final String PROJECT_NAME = "Sistema de Practicas";
    private static final String SECOND_NAME = "Portal Web";
    private static final String PROJECT_DESCRIPTION = "Descripcion del proyecto";
    private static final String SECOND_DESCRIPTION = "Desc B";
    private static final String METHODOLOGY = "Scrum";
    private static final String SECOND_METHODOLOGY = "Kanban";
    private static final String OBJECTIVE = "Objetivo del proyecto";
    private static final String SECOND_OBJECTIVE = "Obj B";
    private static final String ORGANIZATION_NAME = "UV";
    private static final String SUPERVISOR_NAME = "Luis Martinez";
    private static final String STUDENT_ID = "S23013127";
    private static final String CONNECTION_ERROR = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_PROJECT_ID = "idProyecto";
    private static final String COLUMN_CAPACITY = "cupo";
    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_METHODOLOGY = "metodologiaProyecto";
    private static final String COLUMN_OBJECTIVE = "objetivo";
    private static final String COLUMN_DESCRIPTION = "descripcion";
    private static final String COLUMN_ORGANIZATION_NAME = "nombreOrganizacion";
    private static final String COLUMN_STATUS = "estado";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProjectDAO projectDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        projectDAO = new ProjectDAO();
        Field field = ProjectDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(projectDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private Project builderProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        project.setDescription(PROJECT_DESCRIPTION);
        project.setCapacity(CAPACITY);
        project.setMethodology(METHODOLOGY);
        project.setObjective(OBJECTIVE);
        project.setActive(true);
        project.setIdAffiliatedOrganization(ORGANIZATION_ID);
        project.setIdSupervisor(SUPERVISOR_ID);
        return project;
    }

    private Project builderFirstFullProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        project.setMethodology(METHODOLOGY);
        project.setCapacity(CAPACITY);
        project.setObjective(OBJECTIVE);
        project.setDescription(PROJECT_DESCRIPTION);
        project.setAffiliatedOrganizationName(ORGANIZATION_NAME);
        project.setActive(true);
        return project;
    }

    private Project builderSecondFullProject() {
        Project project = new Project();
        project.setId(SECOND_PROJECT_ID);
        project.setName(SECOND_NAME);
        project.setMethodology(SECOND_METHODOLOGY);
        project.setCapacity(CAPACITY);
        project.setObjective(SECOND_OBJECTIVE);
        project.setDescription(SECOND_DESCRIPTION);
        project.setAffiliatedOrganizationName(ORGANIZATION_NAME);
        project.setActive(true);
        return project;
    }

    private Project builderFirstProjectWithCapacity() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        project.setCapacity(CAPACITY);
        project.setAffiliatedOrganizationName(ORGANIZATION_NAME);
        return project;
    }

    private Project builderSecondProjectWithCapacity() {
        Project project = new Project();
        project.setId(SECOND_PROJECT_ID);
        project.setName(SECOND_NAME);
        project.setCapacity(CAPACITY);
        project.setAffiliatedOrganizationName(ORGANIZATION_NAME);
        return project;
    }

    @Test
    void getAllProjects_withResults_returnsExpectedProjectList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_PROJECT_ID)).thenReturn(PROJECT_ID, SECOND_PROJECT_ID);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY, CAPACITY);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME, SECOND_NAME);
        when(resultSet.getString(COLUMN_METHODOLOGY)).thenReturn(METHODOLOGY, SECOND_METHODOLOGY);
        when(resultSet.getString(COLUMN_OBJECTIVE)).thenReturn(OBJECTIVE, SECOND_OBJECTIVE);
        when(resultSet.getString(COLUMN_DESCRIPTION)).thenReturn(PROJECT_DESCRIPTION, SECOND_DESCRIPTION);
        when(resultSet.getString(COLUMN_ORGANIZATION_NAME)).thenReturn(ORGANIZATION_NAME, ORGANIZATION_NAME);
        when(resultSet.getBoolean(COLUMN_STATUS)).thenReturn(true, true);

        assertEquals(List.of(builderFirstFullProject(), builderSecondFullProject()),
            projectDAO.getAllProjects());
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
    void getProjectByName_succesfull_returnsProject() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_PROJECT_ID)).thenReturn(PROJECT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME);

        Project expected = new Project();
        expected.setId(PROJECT_ID);
        expected.setName(PROJECT_NAME);

        assertEquals(Optional.of(expected), projectDAO.getProjectByName(PROJECT_NAME));
    }

    @Test
    void getProjectByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, 
            () -> projectDAO.getProjectByName(PROJECT_NAME));
    }

    @Test
    void getProjectByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, 
            () -> projectDAO.getProjectByName(PROJECT_NAME));
    }

    @Test
    void registerProject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(GENERATED_ID);

        assertTrue(projectDAO.registerProject(builderProject()));
    }

    @Test
    void registerProject_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
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
    void getAllProjectNames_withResults_returnsExpectedNameList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME, SECOND_NAME);

        assertEquals(List.of(PROJECT_NAME, SECOND_NAME), projectDAO.getAllProjectNames());
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

        assertThrows(OperationException.class, 
            () -> projectDAO.getAllProjectNames());
    }

    @Test
    void getProjectBySupervisorName_found_returnsProjectName() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME);

        Optional<String> result = projectDAO.getProjectBySupervisorName(SUPERVISOR_NAME);
        assertEquals(Optional.of(PROJECT_NAME), result);
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
    void getProjectNamesByOrganizationId_withResults_returnsExpectedNameList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME, SECOND_NAME);

        assertEquals(List.of(PROJECT_NAME, SECOND_NAME),
            projectDAO.getProjectNamesByOrganizationId(ORGANIZATION_ID));
    }

    @Test
    void getProjectNamesByOrganizationId_withResults_setsCorrectParameters() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        projectDAO.getProjectNamesByOrganizationId(ORGANIZATION_ID);
        verify(preparedStatement).setInt(1, ORGANIZATION_ID);
        verify(preparedStatement).setBoolean(2, true);
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
    void getProjectByStudentId_whenProjectExists_returnsProject() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_PROJECT_ID)).thenReturn(PROJECT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME);

        Project expectedProject = new Project();
        expectedProject.setId(PROJECT_ID);
        expectedProject.setName(PROJECT_NAME);

        Optional<Project> result = projectDAO.getProjectByStudentId(STUDENT_ID);
        assertEquals(Optional.of(expectedProject), result);
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

    @Test
    void getAllProjectsWithCapacity_withResults_returnsExpectedProjectList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_PROJECT_ID)).thenReturn(PROJECT_ID, SECOND_PROJECT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(PROJECT_NAME, SECOND_NAME);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY, CAPACITY);
        when(resultSet.getString(COLUMN_ORGANIZATION_NAME)).thenReturn(ORGANIZATION_NAME, ORGANIZATION_NAME);

        assertEquals(List.of(builderFirstProjectWithCapacity(), builderSecondProjectWithCapacity()),
            projectDAO.getAllProjectsWithCapacity());
    }

    @Test
    void getAllProjectsWithCapacity_withResults_setsCorrectParameters() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        projectDAO.getAllProjectsWithCapacity();
        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).setInt(2, STATUS_ASSIGNED);
    }

    @Test
    void getAllProjectsWithCapacity_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(projectDAO.getAllProjectsWithCapacity().isEmpty());
    }

    @Test
    void getAllProjectsWithCapacity_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> projectDAO.getAllProjectsWithCapacity());
    }
}