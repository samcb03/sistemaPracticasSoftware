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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class ProfessorDAOTest {

    private static final int EXPECTED_USER_ID = 5;
    private static final int COORDINATOR_ROLE_ID = 3;
    private static final int PROFESSOR_ROLE_ID = 2;
    private static final int INACTIVE_STATUS = 0;
    private static final int ACTIVE_STATUS = 1;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS = 0;
    private static final int FIRST_NRC = 12345;
    private static final int SECOND_NRC = 67890;
    private static final int SAMPLE_ID = 10;
    private static final String PERSONNEL_NUMBER = "UV-001";
    private static final String SEARCH_PREFIX = "UV";
    private static final String CONNECTION_ERROR = "Fallo";

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

    private Professor builderProfessor(boolean isCoordinator) {
        Professor professor = new Professor();
        professor.setId(SAMPLE_ID);
        professor.setPersonnelNumber(PERSONNEL_NUMBER);
        professor.setFirstName("Ana");
        professor.setLastName("García");
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

        assertEquals(EXPECTED_LIST_SIZE, professorDAO.getAllActiveProfessorsMap().size());
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
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> professorDAO.getAllActiveProfessorsMap());
    }

    @Test
    void getProfessorPersonnelNumberByName_found_returnsPersonnelNumber() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("numeroPersonal")).thenReturn(PERSONNEL_NUMBER);

        assertEquals(PERSONNEL_NUMBER,
            professorDAO.getProfessorPersonnelNumberByName("Juan", "Pérez").get());
    }

    @Test
    void getProfessorPersonnelNumberByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorPersonnelNumberByName("No", "Existe"));
    }

    @Test
    void getProfessorPersonnelNumberByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorPersonnelNumberByName("Juan", "Pérez"));
    }

    @ParameterizedTest
    @CsvSource({
        "false",
        "true"
    })
    void registerProfessor_validProfessor_returnsTrue(boolean isCoordinator) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(professorDAO.registerProfessor(builderProfessor(isCoordinator)));
    }

    @Test
    void registerProfessor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> professorDAO.registerProfessor(builderProfessor(false)));
    }

    @Test
    void registerProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.registerProfessor(builderProfessor(false)));
    }

    @ParameterizedTest
    @CsvSource({
        "false",
        "true"
    })
    void modifyProfessor_validProfessor_returnsTrue(boolean isCoordinator) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(professorDAO.modifyProfessor(builderProfessor(isCoordinator)));
    }

    @Test
    void modifyProfessor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> professorDAO.modifyProfessor(builderProfessor(false)));
    }

    @Test
    void modifyProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.modifyProfessor(builderProfessor(false)));
    }

    @Test
    void inactivateProfessor_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(professorDAO.inactivateProfessor(PERSONNEL_NUMBER));
    }

    @Test
    void inactivateProfessor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class,
            () -> professorDAO.inactivateProfessor(PERSONNEL_NUMBER));
    }

    @Test
    void inactivateProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.inactivateProfessor(PERSONNEL_NUMBER));
    }

    @ParameterizedTest
    @CsvSource({
        "2, false",
        "3, true"
    })
    void getProfessorById_byRole_returnsExpectedCoordinatorFlag(int roleId, boolean isCoordinator)
            throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("numeroPersonal")).thenReturn(PERSONNEL_NUMBER);
        when(resultSet.getString("nombre")).thenReturn("Juan");
        when(resultSet.getString("apellidos")).thenReturn("Pérez");
        when(resultSet.getInt("idRol")).thenReturn(roleId);

        assertEquals(isCoordinator,
            professorDAO.getProfessorById(EXPECTED_USER_ID).get().getIsCoordinator());
    }

    @Test
    void getProfessorById_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorById(EXPECTED_USER_ID));
    }

    @Test
    void getProfessorById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorById(EXPECTED_USER_ID));
    }

    @ParameterizedTest
    @CsvSource({
        "0, true",
        "1, false"
    })
    void isProfessorInactive_byStatus_returnsExpected(int status, boolean expectedInactive) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estado")).thenReturn(status);

        assertEquals(expectedInactive, professorDAO.isProfessorInactive(PERSONNEL_NUMBER));
    }

    @Test
    void isProfessorInactive_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> professorDAO.isProfessorInactive(PERSONNEL_NUMBER));
    }

    @Test
    void isProfessorInactive_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.isProfessorInactive(PERSONNEL_NUMBER));
    }

    @Test
    void getIdUserByProfessorPersonnelNumber_found_returnsIdUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idUsuario")).thenReturn(EXPECTED_USER_ID);

        assertEquals(EXPECTED_USER_ID,
            professorDAO.getIdUserByProfessorPersonnelNumber(PERSONNEL_NUMBER));
    }

    @Test
    void getIdUserByProfessorPersonnelNumber_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> professorDAO.getIdUserByProfessorPersonnelNumber(PERSONNEL_NUMBER));
    }

    @Test
    void getIdUserByProfessorPersonnelNumber_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.getIdUserByProfessorPersonnelNumber(PERSONNEL_NUMBER));
    }

    @Test
    void searchProfessorPersonalNumbers_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("numeroPersonal")).thenReturn("UV-001", "UV-002");

        assertEquals(EXPECTED_LIST_SIZE,
            professorDAO.searchProfessorPersonalNumbers(SEARCH_PREFIX).size());
    }

    @Test
    void searchProfessorPersonalNumbers_noMatches_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(professorDAO.searchProfessorPersonalNumbers(SEARCH_PREFIX).isEmpty());
    }

    @Test
    void searchProfessorPersonalNumbers_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.searchProfessorPersonalNumbers(SEARCH_PREFIX));
    }

    @Test
    void hasSubjectAssigned_hasAssignment_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(professorDAO.hasSubjectAssigned(PERSONNEL_NUMBER));
    }

    @Test
    void hasSubjectAssigned_noAssignment_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(professorDAO.hasSubjectAssigned(PERSONNEL_NUMBER));
    }

    @Test
    void hasSubjectAssigned_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.hasSubjectAssigned(PERSONNEL_NUMBER));
    }

    @Test
    void getSubjectsByProfessor_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("NRC")).thenReturn(FIRST_NRC, SECOND_NRC);
        when(resultSet.getString("nombreExperiencia")).thenReturn("Practicas", "Estancia");
        when(resultSet.getString("carrera")).thenReturn("ISW", "ISW");
        when(resultSet.getString("periodo"))
            .thenReturn("Febrero-Julio 2026", "Agosto 2026-Enero 2027");

        assertEquals(EXPECTED_LIST_SIZE,
            professorDAO.getSubjectsByProfessor(PERSONNEL_NUMBER).size());
    }

    @Test
    void getSubjectsByProfessor_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(professorDAO.getSubjectsByProfessor(PERSONNEL_NUMBER).isEmpty());
    }

    @Test
    void getSubjectsByProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.getSubjectsByProfessor(PERSONNEL_NUMBER));
    }
}