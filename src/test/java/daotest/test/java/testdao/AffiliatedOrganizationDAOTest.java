package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Project;
import uv.lis.logic.exceptions.OperationException;

class AffiliatedOrganizationDAOTest {

    private static final int ORGANIZATION_ID = 1;
    private static final int DIRECT_USERS = 10;
    private static final int INDIRECT_USERS = 50;
    private static final int ROWS_AFFECTED = 1;
    private static final int GENERATED_KEY_COLUMN = 1;
    private static final int GENERATED_ID = 1;
    private static final int INACTIVE_STATE_VALUE = 0;
    private static final int ACTIVE_STATE_VALUE = 1;
    private static final int PROJECT_ID = 100;
    private static final int PROJECT_CAPACITY = 5;

    private static final String ORGANIZATION_NAME = "Tech Corp";
    private static final String SECOND_ORGANIZATION_NAME = "Tech Solutions";
    private static final String ORGANIZATION_PREFIX = "Tech";
    private static final String ORGANIZATION_CITY = "Xalapa";
    private static final String ORGANIZATION_STATE = "Veracruz";
    private static final String ORGANIZATION_SECTOR = "Tecnología";
    private static final String ORGANIZATION_EMAIL = "tech@corp.com";
    private static final String ORGANIZATION_PHONE = "2281234567";
    private static final String SUPERVISOR_NAME = "Juan Pérez";
    private static final String PROJECT_NAME = "Proyecto X";
    private static final String PROJECT_DESCRIPTION = "Descripción del proyecto";
    private static final String PROJECT_OBJECTIVE = "Objetivo general";
    private static final String PROJECT_METHODOLOGY = "Scrum";
    private static final String DATABASE_ERROR_MESSAGE = "DB error";

    private static final boolean PROJECT_ACTIVE = true;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private AffiliatedOrganizationDAO affiliatedOrganizationDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(connectionManager.getConnection()).thenReturn(connection);
        affiliatedOrganizationDAO = new AffiliatedOrganizationDAO(connectionManager);
    }

    private void mockQueryExecution() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockGeneratedKeyUpdate(int rowsAffected) throws Exception {
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockSimpleUpdate(int rowsAffected) throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetOrganization() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idOrganizacionVinculada")).thenReturn(ORGANIZATION_ID);
        when(resultSet.getString("nombreOV")).thenReturn(ORGANIZATION_NAME);
        when(resultSet.getString("ciudad")).thenReturn(ORGANIZATION_CITY);
        when(resultSet.getString("estado")).thenReturn(ORGANIZATION_STATE);
        when(resultSet.getString("sector")).thenReturn(ORGANIZATION_SECTOR);
        when(resultSet.getString("correo")).thenReturn(ORGANIZATION_EMAIL);
        when(resultSet.getString("telefono")).thenReturn(ORGANIZATION_PHONE);
        when(resultSet.getInt("numUsuariosDirectos")).thenReturn(DIRECT_USERS);
        when(resultSet.getInt("numUsuariosIndirectos")).thenReturn(INDIRECT_USERS);
    }

    private void mockResultSetTwoOrganizationNames() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("nombreOV"))
            .thenReturn(ORGANIZATION_NAME, SECOND_ORGANIZATION_NAME);
    }

    private void mockResultSetProjectEntry() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME);
        when(resultSet.getString("descripcion")).thenReturn(PROJECT_DESCRIPTION);
    }

    private void mockResultSetCompleteProject() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idProyecto")).thenReturn(PROJECT_ID);
        when(resultSet.getString("nombre")).thenReturn(PROJECT_NAME);
        when(resultSet.getString("descripcion")).thenReturn(PROJECT_DESCRIPTION);
        when(resultSet.getString("objetivo")).thenReturn(PROJECT_OBJECTIVE);
        when(resultSet.getInt("cupo")).thenReturn(PROJECT_CAPACITY);
        when(resultSet.getString("metodologiaProyecto")).thenReturn(PROJECT_METHODOLOGY);
        when(resultSet.getBoolean("estado")).thenReturn(PROJECT_ACTIVE);
        when(resultSet.getString("nombreOV")).thenReturn(ORGANIZATION_NAME);
    }

    private AffiliatedOrganization builderExpectedOrganization() {
        AffiliatedOrganization expectedOrganization = new AffiliatedOrganization();
        expectedOrganization.setId(ORGANIZATION_ID);
        expectedOrganization.setName(ORGANIZATION_NAME);
        expectedOrganization.setCity(ORGANIZATION_CITY);
        expectedOrganization.setState(ORGANIZATION_STATE);
        expectedOrganization.setSector(ORGANIZATION_SECTOR);
        expectedOrganization.setEmail(ORGANIZATION_EMAIL);
        expectedOrganization.setPhoneNumber(ORGANIZATION_PHONE);
        expectedOrganization.setNumberOfDirectUsers(DIRECT_USERS);
        expectedOrganization.setNumberOfIndirectUsers(INDIRECT_USERS);
        return expectedOrganization;
    }

    private Project builderExpectedProject() {
        Project expectedProject = new Project();
        expectedProject.setId(PROJECT_ID);
        expectedProject.setName(PROJECT_NAME);
        expectedProject.setDescription(PROJECT_DESCRIPTION);
        expectedProject.setObjective(PROJECT_OBJECTIVE);
        expectedProject.setCapacity(PROJECT_CAPACITY);
        expectedProject.setMethodology(PROJECT_METHODOLOGY);
        expectedProject.setActive(PROJECT_ACTIVE);
        expectedProject.setAffiliatedOrganizationName(ORGANIZATION_NAME);
        return expectedProject;
    }

    @Test
    void getOrganizationById_found_returnsOrganization() throws Exception {
        mockQueryExecution();
        mockResultSetOrganization();
        Optional<AffiliatedOrganization> expectedOrganization =
            Optional.of(builderExpectedOrganization());

        assertEquals(expectedOrganization,
            affiliatedOrganizationDAO.getOrganizationById(ORGANIZATION_ID));
    }

    @Test
    void getOrganizationById_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationById(ORGANIZATION_ID));
    }

    @Test
    void getOrganizationById_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationById(ORGANIZATION_ID));
    }

    @Test
    void registerOrganization_successful_returnsTrue() throws Exception {
        mockGeneratedKeyUpdate(ROWS_AFFECTED);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(GENERATED_KEY_COLUMN)).thenReturn(GENERATED_ID);

        assertTrue(affiliatedOrganizationDAO.registerOrganization(builderExpectedOrganization()));
    }

    @Test
    void registerOrganization_noRowsAffected_throwsOperationException() throws Exception {
        mockGeneratedKeyUpdate(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.registerOrganization(builderExpectedOrganization()));
    }

    @Test
    void registerOrganization_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.registerOrganization(builderExpectedOrganization()));
    }

    @Test
    void modifyOrganization_successful_returnsTrue() throws Exception {
        mockSimpleUpdate(ROWS_AFFECTED);

        assertTrue(affiliatedOrganizationDAO.modifyOrganization(builderExpectedOrganization()));
    }

    @Test
    void modifyOrganization_noRowsAffected_throwsOperationException() throws Exception {
        mockSimpleUpdate(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.modifyOrganization(builderExpectedOrganization()));
    }

    @Test
    void modifyOrganization_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.modifyOrganization(builderExpectedOrganization()));
    }

    @Test
    void inactivateOrganization_successful_returnsTrue() throws Exception {
        mockSimpleUpdate(ROWS_AFFECTED);

        assertTrue(affiliatedOrganizationDAO.inactivateOrganization(ORGANIZATION_NAME));
    }

    @Test
    void inactivateOrganization_noRowsAffected_throwsOperationException() throws Exception {
        mockSimpleUpdate(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.inactivateOrganization(ORGANIZATION_NAME));
    }

    @Test
    void inactivateOrganization_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.inactivateOrganization(ORGANIZATION_NAME));
    }

    @Test
    void getAllOrganizationNames_namesExist_returnsNameList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoOrganizationNames();
        List<String> expectedNames = List.of(ORGANIZATION_NAME, SECOND_ORGANIZATION_NAME);

        assertEquals(expectedNames, affiliatedOrganizationDAO.getAllOrganizationNames());
    }

    @Test
    void getAllOrganizationNames_noNames_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO.getAllOrganizationNames().isEmpty());
    }

    @Test
    void getAllOrganizationNames_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getAllOrganizationNames());
    }

    @Test
    void getOrganizationIdByName_found_returnsId() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idOrganizacionVinculada")).thenReturn(ORGANIZATION_ID);

        assertEquals(ORGANIZATION_ID,
            affiliatedOrganizationDAO.getOrganizationIdByName(ORGANIZATION_NAME));
    }

    @Test
    void getOrganizationIdByName_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationIdByName(ORGANIZATION_NAME));
    }

    @Test
    void getOrganizationIdByName_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationIdByName(ORGANIZATION_NAME));
    }

    @Test
    void getOrganizationBySupervisorName_found_returnsOrganizationName() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("nombreOV")).thenReturn(ORGANIZATION_NAME);

        assertEquals(Optional.of(ORGANIZATION_NAME),
            affiliatedOrganizationDAO.getOrganizationBySupervisorName(SUPERVISOR_NAME));
    }

    @Test
    void getOrganizationBySupervisorName_notFound_returnsEmptyOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO.getOrganizationBySupervisorName(SUPERVISOR_NAME)
            .isEmpty());
    }

    @Test
    void getOrganizationBySupervisorName_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationBySupervisorName(SUPERVISOR_NAME));
    }

    @Test
    void isOrganizationInactive_inactiveOrganization_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estadoEnBD")).thenReturn(INACTIVE_STATE_VALUE);

        assertTrue(affiliatedOrganizationDAO.isOrganizationInactive(ORGANIZATION_NAME));
    }

    @Test
    void isOrganizationInactive_activeOrganization_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estadoEnBD")).thenReturn(ACTIVE_STATE_VALUE);

        assertFalse(affiliatedOrganizationDAO.isOrganizationInactive(ORGANIZATION_NAME));
    }

    @Test
    void isOrganizationInactive_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.isOrganizationInactive(ORGANIZATION_NAME));
    }

    @Test
    void isOrganizationInactive_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.isOrganizationInactive(ORGANIZATION_NAME));
    }

    @Test
    void searchActiveOrganizationsByNamePrefix_matchesExist_returnsNameList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoOrganizationNames();
        List<String> expectedNames = List.of(ORGANIZATION_NAME, SECOND_ORGANIZATION_NAME);

        assertEquals(expectedNames,
            affiliatedOrganizationDAO.searchActiveOrganizationsByNamePrefix(ORGANIZATION_PREFIX));
    }

    @Test
    void searchActiveOrganizationsByNamePrefix_noMatches_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO
            .searchActiveOrganizationsByNamePrefix(ORGANIZATION_PREFIX).isEmpty());
    }

    @Test
    void searchActiveOrganizationsByNamePrefix_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO
                .searchActiveOrganizationsByNamePrefix(ORGANIZATION_PREFIX));
    }

    @Test
    void getOrganizationByName_found_returnsOrganization() throws Exception {
        mockQueryExecution();
        mockResultSetOrganization();
        Optional<AffiliatedOrganization> expectedOrganization =
            Optional.of(builderExpectedOrganization());

        assertEquals(expectedOrganization,
            affiliatedOrganizationDAO.getOrganizationByName(ORGANIZATION_NAME));
    }

    @Test
    void getOrganizationByName_notFound_returnsEmptyOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO.getOrganizationByName(ORGANIZATION_NAME).isEmpty());
    }

    @Test
    void getOrganizationByName_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getOrganizationByName(ORGANIZATION_NAME));
    }

    @Test
    void getProjectsByOrganization_projectsExist_returnsProjectEntryList() throws Exception {
        mockQueryExecution();
        mockResultSetProjectEntry();
        List<String> expectedProjectEntries = List.of(
            "ID: " + PROJECT_ID + " — " + PROJECT_NAME + " (" + PROJECT_DESCRIPTION + ")");

        assertEquals(expectedProjectEntries,
            affiliatedOrganizationDAO.getProjectsByOrganization(ORGANIZATION_NAME));
    }

    @Test
    void getProjectsByOrganization_noProjects_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO.getProjectsByOrganization(ORGANIZATION_NAME).isEmpty());
    }

    @Test
    void getProjectsByOrganization_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getProjectsByOrganization(ORGANIZATION_NAME));
    }

    @Test
    void hasProjectsActives_projectsExist_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);

        assertTrue(affiliatedOrganizationDAO.hasProjectsActives(ORGANIZATION_NAME));
    }

    @Test
    void hasProjectsActives_noProjects_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(affiliatedOrganizationDAO.hasProjectsActives(ORGANIZATION_NAME));
    }

    @Test
    void hasProjectsActives_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.hasProjectsActives(ORGANIZATION_NAME));
    }

    @Test
    void getCompleteProjectsByOrganization_projectsExist_returnsProjectList() throws Exception {
        mockQueryExecution();
        mockResultSetCompleteProject();
        List<Project> expectedProjects = List.of(builderExpectedProject());

        assertEquals(expectedProjects,
            affiliatedOrganizationDAO.getCompleteProjectsByOrganization(ORGANIZATION_NAME));
    }

    @Test
    void getCompleteProjectsByOrganization_noProjects_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(affiliatedOrganizationDAO
            .getCompleteProjectsByOrganization(ORGANIZATION_NAME).isEmpty());
    }

    @Test
    void getCompleteProjectsByOrganization_sqlError_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString()))
            .thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> affiliatedOrganizationDAO.getCompleteProjectsByOrganization(ORGANIZATION_NAME));
    }
}