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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

class ProjectSupervisorDAOTest {

    private static final int NO_ROWS_AFFECTED = 0;
    private static final int ROWS_AFFECTED = 1;
    private static final int SUPERVISOR_ID = 2;
    private static final int ORGANIZATION_ID = 1;
    private static final int GENERATED_ID = 5;
    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 0;
    private static final int NOT_FOUND_ID = -1;

    private static final String SUPERVISOR_NAME = "Juan Lopez";
    private static final String SUPERVISOR_POSITION = "Gerente";
    private static final String SUPERVISOR_EMAIL = "juan@empresa.com";
    private static final String NONEXISTENT_SUPERVISOR = "NoExiste";
    private static final String CONNECTION_ERROR = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_SUPERVISOR_ID = "idResponsableProyecto";
    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_POSITION = "cargo";
    private static final String COLUMN_EMAIL = "correo";
    private static final String COLUMN_STATUS = "estado";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProjectSupervisorDAO projectSupervisorDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        projectSupervisorDAO = new ProjectSupervisorDAO();
        Field field = ProjectSupervisorDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(projectSupervisorDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockResultSetSingleSupervisor() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt(COLUMN_SUPERVISOR_ID)).thenReturn(SUPERVISOR_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(SUPERVISOR_NAME);
        when(resultSet.getString(COLUMN_POSITION)).thenReturn(SUPERVISOR_POSITION);
        when(resultSet.getString(COLUMN_EMAIL)).thenReturn(SUPERVISOR_EMAIL);
    }

    private ProjectSupervisor buildSupervisor() {
        ProjectSupervisor supervisor = new ProjectSupervisor();
        supervisor.setId(SUPERVISOR_ID);
        supervisor.setName(SUPERVISOR_NAME);
        supervisor.setPosition(SUPERVISOR_POSITION);
        supervisor.setEmail(SUPERVISOR_EMAIL);
        supervisor.setIsActive(true);
        supervisor.setOrganizationInt(ORGANIZATION_ID);
        return supervisor;
    }

    private ProjectSupervisor buildExpectedMappedSupervisor() {
        ProjectSupervisor supervisor = new ProjectSupervisor();
        supervisor.setId(SUPERVISOR_ID);
        supervisor.setName(SUPERVISOR_NAME);
        supervisor.setPosition(SUPERVISOR_POSITION);
        supervisor.setEmail(SUPERVISOR_EMAIL);
        return supervisor;
    }

    @Test
    void getAllSupervisorNames_supervisorsExist_returnsExpectedList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(SUPERVISOR_NAME);

        ArrayList<String> result = projectSupervisorDAO.getAllSupervisorNames();

        assertEquals(List.of(SUPERVISOR_NAME), result);
    }

    @Test
    void getAllSupervisorNames_noSupervisors_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        ArrayList<String> result = projectSupervisorDAO.getAllSupervisorNames();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSupervisorNames_databaseError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getAllSupervisorNames());
    }

    @Test
    void getProjectSupervisorById_supervisorFound_returnsExpectedSupervisor() throws Exception {
        mockQueryExecution();
        mockResultSetSingleSupervisor();
        Optional<ProjectSupervisor> expected = Optional.of(buildExpectedMappedSupervisor());

        Optional<ProjectSupervisor> result = projectSupervisorDAO.getProjectSupervisorById(SUPERVISOR_ID);

        assertEquals(expected, result);
    }

    @Test
    void getProjectSupervisorById_supervisorNotFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getProjectSupervisorById(SUPERVISOR_ID));
    }

    @Test
    void getProjectSupervisorById_databaseError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getProjectSupervisorById(SUPERVISOR_ID));
    }

    @Test
    void registerProjectSupervisor_validSupervisor_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(projectSupervisorDAO.registerProjectSupervisor(buildSupervisor()));
    }

    @Test
    void registerProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.registerProjectSupervisor(buildSupervisor()));
    }

    @Test
    void registerProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.registerProjectSupervisor(buildSupervisor()));
    }

    @Test
    void modifyProjectSupervisor_validSupervisor_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor()));
    }

    @Test
    void modifyProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor()));
    }

    @Test
    void modifyProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor()));
    }

    @Test
    void inactivateProjectSupervisor_supervisorExists_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(projectSupervisorDAO.inactivateProjectSupervisor(SUPERVISOR_NAME));
    }

    @Test
    void inactivateProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.inactivateProjectSupervisor(SUPERVISOR_NAME));
    }

    @Test
    void inactivateProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.inactivateProjectSupervisor(SUPERVISOR_NAME));
    }

    @Test
    void isSupervisorInactive_supervisorIsInactive_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(INACTIVE_STATUS);

        assertTrue(projectSupervisorDAO.isSupervisorInactive(SUPERVISOR_NAME));
    }

    @Test
    void isSupervisorInactive_supervisorIsActive_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(ACTIVE_STATUS);

        assertFalse(projectSupervisorDAO.isSupervisorInactive(SUPERVISOR_NAME));
    }

    @Test
    void isSupervisorInactive_supervisorNotFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.isSupervisorInactive(NONEXISTENT_SUPERVISOR));
    }

    @Test
    void getSupervisorIdByName_supervisorFound_returnsExpectedId() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_SUPERVISOR_ID)).thenReturn(GENERATED_ID);

        int result = projectSupervisorDAO.getSupervisorIdByName(SUPERVISOR_NAME);

        assertEquals(GENERATED_ID, result);
    }

    @Test
    void getSupervisorIdByName_supervisorNotFound_returnsNotFoundId() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        int result = projectSupervisorDAO.getSupervisorIdByName(NONEXISTENT_SUPERVISOR);

        assertEquals(NOT_FOUND_ID, result);
    }

    @Test
    void hasProjectsActives_hasActiveProjects_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        boolean result = projectSupervisorDAO.hasProjectsActives(SUPERVISOR_NAME);

        assertTrue(result);
    }

    @Test
    void hasProjectsActives_noActiveProjects_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        boolean result = projectSupervisorDAO.hasProjectsActives(SUPERVISOR_NAME);

        assertFalse(result);
    }
}