package src.test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class AffiliatedOrganizationDAOTest {

    @Mock
    private MySQLConnectionManager connectionManager;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

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

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetOrganization() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idOrganizacionVinculada")).thenReturn(1);
        when(resultSet.getString("nombreOV")).thenReturn("Tech Corp");
        when(resultSet.getString("ciudad")).thenReturn("Xalapa");
        when(resultSet.getString("estado")).thenReturn("Veracruz");
        when(resultSet.getString("sector")).thenReturn("Tecnología");
        when(resultSet.getString("correo")).thenReturn("tech@corp.com");
        when(resultSet.getString("telefono")).thenReturn("2281234567");
        when(resultSet.getInt("numUsuariosDirectos")).thenReturn(10);
        when(resultSet.getInt("numUsuariosIndirectos")).thenReturn(50);
    }

    private AffiliatedOrganization buildExpectedOrganization() {
        AffiliatedOrganization expected = new AffiliatedOrganization();
        expected.setId(1);
        expected.setName("Tech Corp");
        expected.setCity("Xalapa");
        expected.setState("Veracruz");
        expected.setSector("Tecnología");
        expected.setEmail("tech@corp.com");
        expected.setPhoneNumber("2281234567");
        expected.setNumberOfDirectUsers(10);
        expected.setNumberOfIndirectUsers(50);
        return expected;
    }

    @Test
    void getOrganizationById_succesful_returnsOrganization() throws Exception {
        mockQueryExecution();
        mockResultSetOrganization();

        AffiliatedOrganization expectedOrganization = buildExpectedOrganization();

        assertEquals(expectedOrganization, affiliatedOrganizationDAO.getOrganizationById(1));
    }

    @Test
    void getOrganizationById_failure_returnsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.getOrganizationById(99));
    }

    @Test
    void getOrganizationById_sqlError_returnsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.getOrganizationById(1));
    }

    @Test
    void registerOrganization_succesful_returnsTrue() throws Exception {
        mockUpdateExecution(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        AffiliatedOrganization organizationToRegister = buildExpectedOrganization();

        assertTrue(affiliatedOrganizationDAO.registerOrganization(organizationToRegister));
    }

    @Test
    void registerOrganization_failure_returnsFalse() throws Exception {
        mockUpdateExecution(0);

        AffiliatedOrganization organizationToRegister = buildExpectedOrganization();

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.registerOrganization(organizationToRegister));
    }

    @Test
    void registerOrganization_sqlError_returnsOperationException() throws Exception {
        when(connection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("DB error"));

        AffiliatedOrganization organizationToRegister = buildExpectedOrganization();

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.registerOrganization(organizationToRegister));
    }

    @Test
    void modifyOrganization_succesful_returnsAffiliatedOrganization() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        AffiliatedOrganization organizationToModify = buildExpectedOrganization();

        assertTrue(affiliatedOrganizationDAO.modifyOrganization(organizationToModify));
    }

    @Test
    void modifyOrganization_failure_returnsAffiliatedOrganization() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        AffiliatedOrganization organizationToModify = buildExpectedOrganization();

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.modifyOrganization(organizationToModify));
    }

    @Test
    void modifyOrganization_sqlError_returnsOperationException() throws Exception {

    }

    @Test
    void inactivateOrganization_succesful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        AffiliatedOrganization organizationToInactivate = buildExpectedOrganization();

        assertTrue(affiliatedOrganizationDAO.inactivateOrganization(organizationToInactivate));
    }

    @Test
    void inactivateOrganization_failure_returnsFalse() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        AffiliatedOrganization organizationToInactivate = buildExpectedOrganization();

        assertThrows(OperationException.class, 
            () -> affiliatedOrganizationDAO.inactivateOrganization(organizationToInactivate));
    }

    @Test
    void inactivateOrganization_sqlError_returnsOperationException() throws Exception {

    }
}
