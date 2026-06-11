package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
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
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

class StudentDAOTest {

    private static final int EXPECTED_USER_ID = 4;
    private static final int ROWS_AFFECTED = 1;
    private static final int HAS_PROJECT = 1;
    private static final int DEFAULT_ID_USER = 0;
    private static final int DEFAULT_ROLE_ID = 2;
    private static final int DEFAULT_HOURS = 5;
    private static final boolean INACTIVE_USER = false;
    private static final String FIRST_STUDENT_ID = "S12345678";
    private static final String SECOND_STUDENT_ID = "S124DDDa";
    private static final String INVALID_STUDENT_ID = "Z99";
    private static final String FIRST_NAME = "Denisse";
    private static final String LAST_NAME = "Reyes";
    private static final String SECOND_FIRST_NAME = "Carlos";
    private static final String SECOND_LAST_NAME = "Gomez";
    private static final String GENDER = "Mujer";
    private static final String SEARCH_PREFIX = "S1";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";
    private static final String CONNECTION_MANAGER_FIELD = "connectionManager";

    private static final String DEFAULT_PASSWORD = null;
    private static final String DEFAULT_EMAIL = null;
    private static final String DEFAULT_GENDER = null;
    private static final Date DEFAULT_BIRTH_DATE = null;

    private static final boolean EMAIL_AUTHENTICATION_ACTIVE = true;

    private static final String COLUMN_STUDENT_ID = "matricula";
    private static final String COLUMN_NAME = "nombre";
    private static final String COLUMN_LAST_NAME = "apellidos";
    private static final String COLUMN_BIRTH_DATE = "fechaNacimiento";
    private static final String COLUMN_GENDER = "genero";
    private static final String COLUMN_STATUS = "estado";
    private static final String COLUMN_USER_ID = "idUsuario";

    private static final Date BIRTH_DATE = Date.valueOf("2000-05-15");

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private StudentDAO studentDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        studentDAO = new StudentDAO(connectionManager);
        Field field = StudentDAO.class.getDeclaredField(CONNECTION_MANAGER_FIELD);
        field.setAccessible(true);
        field.set(studentDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSingleStudent() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(FIRST_STUDENT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(LAST_NAME);
        when(resultSet.getDate(COLUMN_BIRTH_DATE)).thenReturn(BIRTH_DATE);
        when(resultSet.getString(COLUMN_GENDER)).thenReturn(GENDER);
    }

    private void mockResultSetTwoStudents() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(FIRST_STUDENT_ID, SECOND_STUDENT_ID);
        when(resultSet.getString(COLUMN_NAME)).thenReturn(FIRST_NAME, SECOND_FIRST_NAME);
        when(resultSet.getString(COLUMN_LAST_NAME)).thenReturn(LAST_NAME, SECOND_LAST_NAME);
    }

    private Student builderFirstStudent() {
        return new Student(DEFAULT_ID_USER, FIRST_NAME,
            LAST_NAME, DEFAULT_PASSWORD, DEFAULT_EMAIL,
            DEFAULT_ROLE_ID, INACTIVE_USER, FIRST_STUDENT_ID,
            DEFAULT_BIRTH_DATE, DEFAULT_HOURS, DEFAULT_GENDER, EMAIL_AUTHENTICATION_ACTIVE
        );
    }

    private Student builderSecondStudent() {
        return new Student(DEFAULT_ID_USER, SECOND_FIRST_NAME,
            SECOND_LAST_NAME, DEFAULT_PASSWORD, DEFAULT_EMAIL,
            DEFAULT_ROLE_ID, INACTIVE_USER, SECOND_STUDENT_ID,
            DEFAULT_BIRTH_DATE, DEFAULT_HOURS, DEFAULT_GENDER, EMAIL_AUTHENTICATION_ACTIVE
        );
    }

    private List<Student> builderExpectedStudents() {
        return List.of(builderFirstStudent(), builderSecondStudent());
    }

    @Test
    void getStudentById_successful_returnsStudent() throws Exception {
        mockQueryExecution();
        mockResultSetSingleStudent();

        Student student = new Student();
        student.setId(EXPECTED_USER_ID);
        student.setIdStudent(FIRST_STUDENT_ID);
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setBirthDate(BIRTH_DATE);
        student.setGender(GENDER);

        Optional<Student> expectedStudent = Optional.of(student);

        assertEquals(expectedStudent, studentDAO.getStudentById(EXPECTED_USER_ID));
    }

    @Test
    void getStudentById_notFound_returnsEmptyOptional() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.getStudentById(EXPECTED_USER_ID));
    }

    @Test
    void getStudentById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.getStudentById(EXPECTED_USER_ID));
    }

    @Test
    void getIdUserByStudentId_successful_returnsIdUser() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_USER_ID)).thenReturn(EXPECTED_USER_ID);

        assertEquals(Optional.of(EXPECTED_USER_ID),
            studentDAO.getIdUserByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getIdUserByStudentId_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.getIdUserByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getIdUserByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.getIdUserByStudentId(FIRST_STUDENT_ID));
    }

    @Test
    void getActiveStudentsNotInSubject_withResults_returnsStudentList() throws Exception {
        mockQueryExecution();
        mockResultSetTwoStudents();
        List<Student> expectedStudents = builderExpectedStudents();

        assertEquals(expectedStudents, studentDAO.getActiveStudentsNotInSubject());
    }

    @Test
    void getActiveStudentsNotInSubject_emptyResultSet_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(studentDAO.getActiveStudentsNotInSubject().isEmpty());
    }

    @Test
    void getActiveStudentsNotInSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.getActiveStudentsNotInSubject());
    }

    @Test
    void registerStudent_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(studentDAO.registerStudent(builderFirstStudent()));
    }

    @Test
    void registerStudent_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> studentDAO.registerStudent(builderFirstStudent()));
    }

    @Test
    void registerStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.registerStudent(builderFirstStudent()));
    }

    @Test
    void modifyStudent_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(studentDAO.modifyStudent(builderFirstStudent()));
    }

    @Test
    void modifyStudent_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> studentDAO.modifyStudent(builderFirstStudent()));
    }

    @Test
    void modifyStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.modifyStudent(builderFirstStudent()));
    }

    @Test
    void inactivateStudent_successful_returnsTrue() throws Exception {
        mockUpdateExecution(ROWS_AFFECTED);

        assertTrue(studentDAO.inactivateStudent(FIRST_STUDENT_ID));
    }

    @Test
    void inactivateStudent_noRowsAffected_throwsOperationException() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertThrows(OperationException.class,
            () -> studentDAO.inactivateStudent(FIRST_STUDENT_ID));
    }

    @Test
    void inactivateStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.inactivateStudent(FIRST_STUDENT_ID));
    }

    @Test
    void isStudentInactive_studentIsInactive_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(NO_ROWS_AFFECTED);

        assertTrue(studentDAO.isStudentInactive(FIRST_STUDENT_ID));
    }

    @Test
    void isStudentInactive_studentIsActive_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(COLUMN_STATUS)).thenReturn(ROWS_AFFECTED);

        assertFalse(studentDAO.isStudentInactive(FIRST_STUDENT_ID));
    }

    @Test
    void isStudentInactive_notFound_throwsOperationException() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.isStudentInactive(FIRST_STUDENT_ID));
    }

    @Test
    void isStudentInactive_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.isStudentInactive(FIRST_STUDENT_ID));
    }

    @Test
    void searchStudentIds_successful_returnsStudentIds() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(COLUMN_STUDENT_ID)).thenReturn(FIRST_STUDENT_ID, SECOND_STUDENT_ID);

        assertEquals(List.of(FIRST_STUDENT_ID, SECOND_STUDENT_ID),
            studentDAO.searchStudentIds(SEARCH_PREFIX));
    }

    @Test
    void searchStudentIds_noMatches_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(studentDAO.searchStudentIds(INVALID_STUDENT_ID).isEmpty());
    }

    @Test
    void searchStudentIds_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.searchStudentIds(SEARCH_PREFIX));
    }

    @Test
    void hasProjectAssigned_hasAssignment_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(HAS_PROJECT);

        assertTrue(studentDAO.hasProjectAssigned(FIRST_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_noAssignment_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(NO_ROWS_AFFECTED);

        assertFalse(studentDAO.hasProjectAssigned(FIRST_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_emptyResultSet_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(studentDAO.hasProjectAssigned(FIRST_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> studentDAO.hasProjectAssigned(FIRST_STUDENT_ID));
    }
}