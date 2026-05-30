package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Date;
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
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class StudentDAOTest {

    private static final int EXPECTED_USER_ID = 1;
    private static final int EXPECTED_LIST_SIZE = 2;
    private static final int INACTIVE_STATUS = 0;
    private static final int ACTIVE_STATUS = 1;
    private static final String VALID_STUDENT_ID = "S12345678";
    private static final String SECOND_STUDENT_ID = "S124DDDa";
    private static final String INVALID_STUDENT_ID = "Z99";
    private static final String FIRST_NAME = "Denisse";
    private static final String LAST_NAME = "Reyes";
    private static final String GENDER = "Mujer";
    private static final String SEARCH_PREFIX = "S1";
    private static final String CONNECTION_ERROR = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private StudentDAO studentDAO;

    @BeforeEach
    void setUp() throws Exception {
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
        studentDAO = new StudentDAO(connectionManager);
    }

    private Student builderStudent() {
        Student student = new Student();
        student.setId(EXPECTED_USER_ID);
        student.setIdStudent(VALID_STUDENT_ID);
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setBirthDate(new Date(System.currentTimeMillis()));
        student.setGender(GENDER);
        return student;
    }

    @Test
    void getStudentById_successful_returnsStudent() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("matricula")).thenReturn(VALID_STUDENT_ID);
        when(resultSet.getString("nombre")).thenReturn(FIRST_NAME);
        when(resultSet.getString("apellidos")).thenReturn(LAST_NAME);
        when(resultSet.getDate("fechaNacimiento")).thenReturn(new Date(System.currentTimeMillis()));
        when(resultSet.getString("genero")).thenReturn(GENDER);

        Optional<Student> result = studentDAO.getStudentById(EXPECTED_USER_ID);

        assertTrue(result.isPresent());
    }

    @Test
    void getStudentById_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.getStudentById(EXPECTED_USER_ID));
    }

    @Test
    void getStudentById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.getStudentById(EXPECTED_USER_ID));
    }

    @Test
    void getIdUserByStudentId_successful_returnsIdUser() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idUsuario")).thenReturn(EXPECTED_USER_ID);

        assertEquals(EXPECTED_USER_ID, studentDAO.getIdUserByStudentId(VALID_STUDENT_ID).get());
    }

    @Test
    void getIdUserByStudentId_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.getIdUserByStudentId(VALID_STUDENT_ID));
    }

    @Test
    void getIdUserByStudentId_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.getIdUserByStudentId(VALID_STUDENT_ID));
    }

    @Test
    void getActiveStudentsNotInSubject_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("matricula")).thenReturn(VALID_STUDENT_ID, SECOND_STUDENT_ID);
        when(resultSet.getString("nombre")).thenReturn(FIRST_NAME, "Samuel");
        when(resultSet.getString("apellidos")).thenReturn(LAST_NAME, "Carreto");

        ArrayList<Student> result = studentDAO.getActiveStudentsNotInSubject();

        assertEquals(EXPECTED_LIST_SIZE, result.size());
    }

    @Test
    void getActiveStudentsNotInSubject_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(studentDAO.getActiveStudentsNotInSubject().isEmpty());
    }

    @Test
    void getActiveStudentsNotInSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.getActiveStudentsNotInSubject());
    }

    @Test
    void registerStudent_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.registerStudent(builderStudent()));
    }

    @Test
    void registerStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> studentDAO.registerStudent(builderStudent()));
    }

    @Test
    void registerStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.registerStudent(builderStudent()));
    }

    @Test
    void modifyStudent_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.modifyStudent(builderStudent()));
    }

    @Test
    void modifyStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> studentDAO.modifyStudent(builderStudent()));
    }

    @Test
    void modifyStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.modifyStudent(builderStudent()));
    }

    @Test
    void inactivateStudent_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.inactivateStudent(VALID_STUDENT_ID));
    }

    @Test
    void inactivateStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(OperationException.class,
            () -> studentDAO.inactivateStudent(VALID_STUDENT_ID));
    }

    @Test
    void inactivateStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.inactivateStudent(VALID_STUDENT_ID));
    }

    @Test
    void isStudentInactive_studentIsInactive_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estado")).thenReturn(INACTIVE_STATUS);

        assertTrue(studentDAO.isStudentInactive(VALID_STUDENT_ID));
    }

    @Test
    void isStudentInactive_studentIsActive_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("estado")).thenReturn(ACTIVE_STATUS);

        assertFalse(studentDAO.isStudentInactive(VALID_STUDENT_ID));
    }

    @Test
    void isStudentInactive_notFound_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class,
            () -> studentDAO.isStudentInactive(VALID_STUDENT_ID));
    }

    @Test
    void isStudentInactive_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.isStudentInactive(VALID_STUDENT_ID));
    }

    @Test
    void searchStudentIds_successful_returnsStudentIds() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("matricula")).thenReturn(VALID_STUDENT_ID, SECOND_STUDENT_ID);

        assertEquals(EXPECTED_LIST_SIZE, studentDAO.searchStudentIds(SEARCH_PREFIX).size());
    }

    @Test
    void searchStudentIds_noMatches_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(studentDAO.searchStudentIds(INVALID_STUDENT_ID).isEmpty());
    }

    @Test
    void searchStudentIds_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.searchStudentIds(SEARCH_PREFIX));
    }

    @Test
    void hasProjectAssigned_hasAssignment_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        assertTrue(studentDAO.hasProjectAssigned(VALID_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_noAssignment_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        assertFalse(studentDAO.hasProjectAssigned(VALID_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_emptyResultSet_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(studentDAO.hasProjectAssigned(VALID_STUDENT_ID));
    }

    @Test
    void hasProjectAssigned_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException(CONNECTION_ERROR));

        assertThrows(OperationException.class,
            () -> studentDAO.hasProjectAssigned(VALID_STUDENT_ID));
    }
}