package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.lang.reflect.Field;
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
import uv.lis.logic.dao.RequestProjectDAO;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

class RequestProjectDAOTest {

    private static final int FIRST_PROJECT_ID     = 9;
    private static final int ROWS_AFFECTED        = 1;  
    private static final int REQUEST_COUNT_ZERO   = 0;
    private static final int CAPACITY_AVAILABLE   = 5;
    private static final int CAPACITY_FULL        = 3;

    private static final String FIRST_STUDENT_ID = "S23013127";
    private static final String FIRST_STUDENT_NAME = "Ana";
    private static final String FIRST_STUDENT_SURNAMES = "Gomez Ramirez";
    private static final String FIRST_PROJECT_NAME = "Proyecto Test";
    private static final String FIRST_PROJECT_DESCRIPTION = "Descripcion test";
    private static final String FIRST_PROJECT_METHODOLOGY = "Scrum";
    private static final String FIRST_PROJECT_OBJECTIVE = "Objetivo test";
    private static final String NO_PROJECT_MESSAGE = "Sin proyecto asignado";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_STUDENT_ID = "matricula";
    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_LAST_NAME = "apellidos";
    private static final String COLUMN_PROJECT_ID = "idProyecto";
    private static final String COLUMN_DESCRIPTION = "descripcion";
    private static final String COLUMN_CAPACITY = "cupo";
    private static final String COLUMN_METHODOLOGY = "metodologiaProyecto";
    private static final String COLUMN_OBJECTIVE = "objetivo";
    private static final String COLUMN_REQUESTS = "solicitudes";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private RequestProjectDAO requestProjectDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        requestProjectDAO = new RequestProjectDAO();
        Field field = RequestProjectDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(requestProjectDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSingleStudent() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(FIRST_STUDENT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_STUDENT_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(FIRST_STUDENT_SURNAMES);
    }

    private void mockResultSetSingleProject() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt(COLUMN_PROJECT_ID)).thenReturn(FIRST_PROJECT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_PROJECT_NAME);
        when(resultSet.getString(COLUMN_DESCRIPTION)).thenReturn(FIRST_PROJECT_DESCRIPTION);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY_AVAILABLE);
        when(resultSet.getString(COLUMN_METHODOLOGY)).thenReturn(FIRST_PROJECT_METHODOLOGY);
        when(resultSet.getString(COLUMN_OBJECTIVE)).thenReturn(FIRST_PROJECT_OBJECTIVE);
    }

    private Project builderProject() {
        Project project = new Project();
        project.setId(FIRST_PROJECT_ID);
        project.setName(FIRST_PROJECT_NAME);
        project.setDescription(FIRST_PROJECT_DESCRIPTION);
        project.setCapacity(CAPACITY_AVAILABLE);
        project.setMethodology(FIRST_PROJECT_METHODOLOGY);
        project.setObjective(FIRST_PROJECT_OBJECTIVE);
        return project;
    }

    private Student builderStudent() {
        Student student = new Student();
        student.setIdStudent(FIRST_STUDENT_ID);
        student.setFirstName(FIRST_STUDENT_NAME);
        student.setLastName(FIRST_STUDENT_SURNAMES);
        return student;
    }

    @Test
    void getActiveRequestCountByStudentId_studentHasRequests_returnsCount() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(CAPACITY_FULL);

        assertEquals(CAPACITY_FULL,
            requestProjectDAO.getActiveRequestCountByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getActiveRequestCountByStudentId_studentHasNoRequests_returnsZero() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(REQUEST_COUNT_ZERO);

        assertEquals(REQUEST_COUNT_ZERO,
            requestProjectDAO.getActiveRequestCountByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getActiveRequestCountByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getActiveRequestCountByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getAvailableProjects_projectsExist_returnsExpectedList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleProject();

        assertEquals(List.of(builderProject()), requestProjectDAO.getAvailableProjects());
    }

    @Test
    void getAvailableProjects_noProjects_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(requestProjectDAO.getAvailableProjects().isEmpty());
    }

    @Test
    void getAvailableProjects_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getAvailableProjects());
    }

    @Test
    void hasAlreadyRequested_studentAlreadyRequested_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(ROWS_AFFECTED);

        assertTrue(requestProjectDAO.hasAlreadyRequested(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void hasAlreadyRequested_studentHasNotRequested_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(REQUEST_COUNT_ZERO);

        assertFalse(requestProjectDAO.hasAlreadyRequested(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void hasAlreadyRequested_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.hasAlreadyRequested(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void hasAvailableCapacity_projectHasCapacity_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY_AVAILABLE);
        when(resultSet.getInt(COLUMN_REQUESTS)).thenReturn(CAPACITY_FULL);

        assertTrue(requestProjectDAO.hasAvailableCapacity(FIRST_PROJECT_ID));
    }

    @Test
    void hasAvailableCapacity_projectIsFull_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY_FULL);
        when(resultSet.getInt(COLUMN_REQUESTS)).thenReturn(CAPACITY_FULL);

        assertFalse(requestProjectDAO.hasAvailableCapacity(FIRST_PROJECT_ID));
    }

    @Test
    void hasAvailableCapacity_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.hasAvailableCapacity(FIRST_PROJECT_ID));
    }

    @Test
    void requestProject_validRequest_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(requestProjectDAO.requestProject(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void requestProject_noRowsAffected_returnsFalse() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertFalse(requestProjectDAO.requestProject(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void requestProject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.requestProject(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void validateProjectRequest_maxRequestsReached_returnsError() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(Integer.MAX_VALUE);

        Optional<String> result =
            requestProjectDAO.validateProjectRequest(FIRST_STUDENT_ID, FIRST_PROJECT_ID);

        assertTrue(result.isPresent());
    }

    @Test
    void validateProjectRequest_validRequest_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_TOTAL)).thenReturn(REQUEST_COUNT_ZERO);
        when(resultSet.getInt(COLUMN_CAPACITY)).thenReturn(CAPACITY_AVAILABLE);
        when(resultSet.getInt(COLUMN_REQUESTS)).thenReturn(ROWS_AFFECTED);

        assertEquals(Optional.empty(), requestProjectDAO.validateProjectRequest(
            FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void validateProjectRequest_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.validateProjectRequest(FIRST_STUDENT_ID, FIRST_PROJECT_ID));
    }

    @Test
    void getApplicantsByProjectId_applicantsExist_returnsExpectedList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleStudent();

        assertEquals(List.of(builderStudent()),
            requestProjectDAO.getApplicantsByProjectId(FIRST_PROJECT_ID));
    }

    @Test
    void getApplicantsByProjectId_noApplicants_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(requestProjectDAO.getApplicantsByProjectId(FIRST_PROJECT_ID).isEmpty());
    }

    @Test
    void getApplicantsByProjectId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getApplicantsByProjectId(FIRST_PROJECT_ID));
    }

    @Test
    void getProjectAssignedToStudent_projectFound_returnsProjectName() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_PROJECT_NAME);

        assertEquals(FIRST_PROJECT_NAME,
            requestProjectDAO.getProjectAssignedToStudent(FIRST_STUDENT_ID));
    }

    @Test
    void getProjectAssignedToStudent_noProjectAssigned_returnsDefaultMessage() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertEquals(NO_PROJECT_MESSAGE,
            requestProjectDAO.getProjectAssignedToStudent(FIRST_STUDENT_ID));
    }

    @Test
    void getProjectAssignedToStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getProjectAssignedToStudent(FIRST_STUDENT_ID));
    }

    @Test
    void getAssignedStudentsByProjectId_studentsExist_returnsExpectedList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleStudent();

        assertEquals(List.of(builderStudent()),
            requestProjectDAO.getAssignedStudentsByProjectId(FIRST_PROJECT_ID));
    }

    @Test
    void getAssignedStudentsByProjectId_noStudents_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(requestProjectDAO.getAssignedStudentsByProjectId(FIRST_PROJECT_ID).isEmpty());
    }

    @Test
    void getAssignedStudentsByProjectId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.getAssignedStudentsByProjectId(FIRST_PROJECT_ID));
    }

    @Test
    void unassignStudentFromProject_validStudent_executesWithoutException() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        requestProjectDAO.unassignStudentFromProject(FIRST_STUDENT_ID);

        assertTrue(true);
    }

    @Test
    void unassignStudentFromProject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> requestProjectDAO.unassignStudentFromProject(FIRST_STUDENT_ID));
    }
}