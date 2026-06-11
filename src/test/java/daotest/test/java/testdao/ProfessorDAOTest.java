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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

class ProfessorDAOTest {

    private static final int ROWS_AFFECTED = 1;
    private static final int NO_ROWS = 0;
    private static final int USER_ID = 10;
    private static final int ROLE_PROFESSOR = 2;
    private static final int ACTIVE_STATE = 1;
    private static final int INACTIVE_STATE = 0;
    private static final int COUNT_ONE = 1;
    private static final int COUNT_ZERO = 0;
    private static final int FIRST_COLUMN_INDEX = 1;

    private static final String PERSONNEL_NUMBER = "ABC123";
    private static final String SECOND_PERSONNEL_NUMBER = "XYZ789";
    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Pérez";
    private static final String SECOND_FIRST_NAME = "María";
    private static final String SECOND_LAST_NAME = "García";
    private static final String FULL_NAME = "Juan Pérez";
    private static final String SECOND_FULL_NAME = "María García";
    private static final String NRC = "NRC: 1 — Cálculo — ISW (2024-1)";
    private static final String SECOND_NRC = "NRC: 2 — Álgebra — ISW (2024-1)";
    private static final String PREFIX = "ABC";
    private static final String CONNECTION_ERROR = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_LAST_NAME = "apellidos";
    private static final String COLUMN_PERSONNEL_NUMBER = "numeroPersonal";
    private static final String COLUMN_ROLE_ID = "idRol";
    private static final String COLUMN_STATUS = "estado";
    private static final String COLUMN_USER_ID = "idUsuario";
    private static final String COLUMN_NRC = "NRC";
    private static final String COLUMN_SUBJECT_NAME = "nombreExperiencia";
    private static final String COLUMN_CAREER = "carrera";
    private static final String COLUMN_PERIOD = "periodo";

    private static final String NRC_VALUE_ONE = "Cálculo";
    private static final String NRC_VALUE_TWO = "Álgebra";
    private static final String CAREER_VALUE = "ISW";
    private static final String PERIOD_VALUE = "2024-1";
    private static final int NRC_NUMBER_ONE = 1;
    private static final int NRC_NUMBER_TWO = 2;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private ProfessorDAO professorDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        professorDAO = new ProfessorDAO();
        Field field = ProfessorDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(professorDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setId(USER_ID);
        professor.setPersonnelNumber(PERSONNEL_NUMBER);
        professor.setFirstName(FIRST_NAME);
        professor.setLastName(LAST_NAME);
        professor.setIsCoordinator(false);
        return professor;
    }

    private Professor buildCoordinatorProfessor() {
        Professor professor = new Professor();
        professor.setId(USER_ID);
        professor.setPersonnelNumber(PERSONNEL_NUMBER);
        professor.setFirstName(FIRST_NAME);
        professor.setLastName(LAST_NAME);
        professor.setIsCoordinator(true);
        return professor;
    }

    @Test
    void getAllActiveProfessorsMap_withResults_returnsExpectedMap() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_NAME, SECOND_FIRST_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(LAST_NAME, SECOND_LAST_NAME);
        when(resultSet.getString(COLUMN_PERSONNEL_NUMBER)).thenReturn(PERSONNEL_NUMBER, SECOND_PERSONNEL_NUMBER);

        LinkedHashMap<String, String> expectedMap = new LinkedHashMap<>();
        expectedMap.put(FULL_NAME, PERSONNEL_NUMBER);
        expectedMap.put(SECOND_FULL_NAME, SECOND_PERSONNEL_NUMBER);

        assertEquals(expectedMap, professorDAO.getAllActiveProfessorsMap());
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
    void getProfessorPersonnelNumberByName_found_returnsExpectedPersonnelNumber() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_PERSONNEL_NUMBER)).thenReturn(PERSONNEL_NUMBER);

        assertEquals(Optional.of(PERSONNEL_NUMBER),
            professorDAO.getProfessorPersonnelNumberByName(FIRST_NAME, LAST_NAME));
    }

    @Test
    void getProfessorPersonnelNumberByName_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorPersonnelNumberByName(FIRST_NAME, LAST_NAME));
    }

    @Test
    void getProfessorPersonnelNumberByName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.getProfessorPersonnelNumberByName(FIRST_NAME, LAST_NAME));
    }

    @Test
    void getProfessorById_found_returnsExpectedProfessor() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_PERSONNEL_NUMBER)).thenReturn(PERSONNEL_NUMBER);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(LAST_NAME);
        when(resultSet.getInt(COLUMN_ROLE_ID)).thenReturn(ROLE_PROFESSOR);

        assertEquals(Optional.of(buildProfessor()), professorDAO.getProfessorById(USER_ID));
    }

    @Test
    void getProfessorById_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () -> professorDAO.getProfessorById(USER_ID));
    }

    @Test
    void getProfessorById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> professorDAO.getProfessorById(USER_ID));
    }

    @Test
    void registerProfessor_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(professorDAO.registerProfessor(buildProfessor()));
    }

    @Test
    void registerProfessor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class, () -> professorDAO.registerProfessor(buildProfessor()));
    }

    @Test
    void registerProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> professorDAO.registerProfessor(buildProfessor()));
    }

    @Test
    void modifyProfessor_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(ROWS_AFFECTED);

        assertTrue(professorDAO.modifyProfessor(buildProfessor()));
    }

    @Test
    void modifyProfessor_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS);

        assertThrows(OperationException.class, () -> professorDAO.modifyProfessor(buildProfessor()));
    }

    @Test
    void modifyProfessor_asCoordinatorWithAnotherActiveCoordinator_throwsOperationException()
            throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(COUNT_ONE);

        assertThrows(OperationException.class,
            () -> professorDAO.modifyProfessor(buildCoordinatorProfessor()));
    }

    @Test
    void modifyProfessor_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class, () -> professorDAO.modifyProfessor(buildProfessor()));
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

    @Test
    void isProfessorInactive_inactiveProfessor_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(INACTIVE_STATE);

        assertTrue(professorDAO.isProfessorInactive(PERSONNEL_NUMBER));
    }

    @Test
    void isProfessorInactive_activeProfessor_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(ACTIVE_STATE);

        assertFalse(professorDAO.isProfessorInactive(PERSONNEL_NUMBER));
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
    void getIdUserByProfessorPersonnelNumber_found_returnsExpectedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_USER_ID)).thenReturn(USER_ID);

        assertEquals(USER_ID,
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
    void searchProfessorPersonalNumbers_withResults_returnsExpectedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_PERSONNEL_NUMBER))
            .thenReturn(PERSONNEL_NUMBER, SECOND_PERSONNEL_NUMBER);

        assertEquals(List.of(PERSONNEL_NUMBER, SECOND_PERSONNEL_NUMBER),
            professorDAO.searchProfessorPersonalNumbers(PREFIX));
    }

    @Test
    void searchProfessorPersonalNumbers_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(professorDAO.searchProfessorPersonalNumbers(PREFIX).isEmpty());
    }

    @Test
    void searchProfessorPersonalNumbers_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.searchProfessorPersonalNumbers(PREFIX));
    }

    @Test
    void hasSubjectAssigned_withAssignedSubject_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(professorDAO.hasSubjectAssigned(PERSONNEL_NUMBER));
    }

    @Test
    void hasSubjectAssigned_withNoAssignedSubject_returnsFalse() throws Exception {
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
    void getSubjectsByProfessor_withResults_returnsExpectedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt(COLUMN_NRC)).thenReturn(NRC_NUMBER_ONE, NRC_NUMBER_TWO);
        when(resultSet.getString(COLUMN_SUBJECT_NAME)).thenReturn(NRC_VALUE_ONE, NRC_VALUE_TWO);
        when(resultSet.getString(COLUMN_CAREER)).thenReturn(CAREER_VALUE, CAREER_VALUE);
        when(resultSet.getString(COLUMN_PERIOD)).thenReturn(PERIOD_VALUE, PERIOD_VALUE);

        assertEquals(List.of(NRC, SECOND_NRC),
            professorDAO.getSubjectsByProfessor(PERSONNEL_NUMBER));
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

    @Test
    void isAnotherCoordinatorActive_withActiveCoordinator_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(COUNT_ONE);

        assertTrue(professorDAO.isAnotherCoordinatorActive(PERSONNEL_NUMBER));
    }

    @Test
    void isAnotherCoordinatorActive_withNoActiveCoordinator_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_COLUMN_INDEX)).thenReturn(COUNT_ZERO);

        assertFalse(professorDAO.isAnotherCoordinatorActive(PERSONNEL_NUMBER));
    }

    @Test
    void isAnotherCoordinatorActive_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> professorDAO.isAnotherCoordinatorActive(PERSONNEL_NUMBER));
    }
}