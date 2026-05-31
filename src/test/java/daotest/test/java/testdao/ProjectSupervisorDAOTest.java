package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class ProjectSupervisorDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProjectSupervisorDAO projectSupervisorDAO;

    @BeforeEach
    void setUp() throws Exception {
        projectSupervisorDAO = new ProjectSupervisorDAO();
        Field field = ProjectSupervisorDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(projectSupervisorDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void getAllSupervisorNames_supervisorsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("nombre")).thenReturn("Juan Lopez");

        ArrayList<String> result = projectSupervisorDAO.getAllSupervisorNames();

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllSupervisorNames_noSupervisors_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ArrayList<String> result = projectSupervisorDAO.getAllSupervisorNames();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSupervisorNames_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getAllSupervisorNames());
    }

    @Test
    void getProjectSupervisorById_supervisorFound_returnsNonEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idResponsableProyecto")).thenReturn(1);
        when(resultSet.getString("nombre")).thenReturn("Juan Lopez");
        when(resultSet.getString("cargo")).thenReturn("Gerente");
        when(resultSet.getString("correo")).thenReturn("juan@empresa.com");

        Optional<ProjectSupervisor> result = projectSupervisorDAO.getProjectSupervisorById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void getProjectSupervisorById_supervisorNotFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getProjectSupervisorById(99));
    }

    @Test
    void getProjectSupervisorById_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.getProjectSupervisorById(1));
    }

    @Test
    void registerProjectSupervisor_validSupervisor_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = projectSupervisorDAO.registerProjectSupervisor(buildSupervisor());

        assertTrue(result);
    }

    @Test
    void registerProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.registerProjectSupervisor(buildSupervisor()));
    }

    @Test
    void registerProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.registerProjectSupervisor(buildSupervisor()));
    }

    @Test
    void modifyProjectSupervisor_validSupervisor_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor());

        assertTrue(result);
    }

    @Test
    void modifyProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor()));
    }

    @Test
    void modifyProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.modifyProjectSupervisor(buildSupervisor()));
    }

    @Test
    void inactivateProjectSupervisor_supervisorExists_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = projectSupervisorDAO.inactivateProjectSupervisor("Juan Lopez");

        assertTrue(result);
    }

    @Test
    void inactivateProjectSupervisor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.inactivateProjectSupervisor("Juan Lopez"));
    }

    @Test
    void inactivateProjectSupervisor_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.inactivateProjectSupervisor("Juan Lopez"));
    }

    @Test
    void isSupervisorInactive_supervisorIsInactive_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estado")).thenReturn(0);

        boolean result = projectSupervisorDAO.isSupervisorInactive("Juan Lopez");

        assertTrue(result);
    }

    @Test
    void isSupervisorInactive_supervisorIsActive_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estado")).thenReturn(1);

        boolean result = projectSupervisorDAO.isSupervisorInactive("Juan Lopez");

        assertFalse(result);
    }

    @Test
    void isSupervisorInactive_supervisorNotFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> projectSupervisorDAO.isSupervisorInactive("NoExiste"));
    }

    @Test
    void getSupervisorIdByName_supervisorFound_returnsId() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idResponsableProyecto")).thenReturn(5);

        int result = projectSupervisorDAO.getSupervisorIdByName("Juan Lopez");

        assertNotEquals(-1, result);
    }

    @Test
    void getSupervisorIdByName_supervisorNotFound_returnsMinusOne() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int result = projectSupervisorDAO.getSupervisorIdByName("NoExiste");

        assertTrue(result == -1);
    }

    @Test
    void hasProjectsActives_hasActiveProjects_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        boolean result = projectSupervisorDAO.hasProjectsActives("Juan Lopez");

        assertTrue(result);
    }

    @Test
    void hasProjectsActives_noActiveProjects_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        boolean result = projectSupervisorDAO.hasProjectsActives("Juan Lopez");

        assertFalse(result);
    }

    private ProjectSupervisor buildSupervisor() {
        ProjectSupervisor supervisor = new ProjectSupervisor();
        supervisor.setId(1);
        supervisor.setName("Juan Lopez");
        supervisor.setPosition("Gerente");
        supervisor.setEmail("juan@empresa.com");
        supervisor.setIsActive(true);
        supervisor.setOrganizationInt(1);
        return supervisor;
    }
}