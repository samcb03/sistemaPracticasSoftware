package src.test.java;


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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class SubjectDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    private SubjectDAO subjectDAO;

    @BeforeEach
    void setUp() throws Exception {
        subjectDAO = new SubjectDAO();
        Field field = SubjectDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(subjectDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private Subject buildSubject() {
        Subject subject = new Subject();
        subject.setNrc(12345);
        subject.setSchoolPeriodId(1);
        subject.setProfessorPersonnelNumber("UV-001");
        return subject;
    }


    @Test
    void registerSubject_bothInsertsSuccessful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1, 1);

        assertTrue(subjectDAO.registerSubject(buildSubject()));
    }

    @Test
    void registerSubject_firstInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0, 1);

        assertFalse(subjectDAO.registerSubject(buildSubject()));
    }

    @Test
    void registerSubject_secondInsertFails_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1, 0);

        assertFalse(subjectDAO.registerSubject(buildSubject()));
    }

    @Test
    void registerSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo de conexión"));

        assertThrows(OperationException.class, () ->
            subjectDAO.registerSubject(buildSubject()));
    }

    @Test
    void registerSubject_prepareStatementError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error al preparar"));

        assertThrows(OperationException.class, () ->
            subjectDAO.registerSubject(buildSubject()));
    }


    @Test
    void getAllSubjectsNRCName_withResults_returnsPopulatedList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("NRC")).thenReturn(12345, 67890);
        when(resultSet.getString("nombreExperiencia"))
            .thenReturn("Sistemas Operativos", "Bases de Datos");

        assertEquals(2, subjectDAO.getAllSubjectsNRCName().size());
    }

    @Test
    void getAllSubjectsNRCName_formattedCorrectly() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("NRC")).thenReturn(12345);
        when(resultSet.getString("nombreExperiencia")).thenReturn("Sistemas Operativos");

        String result = subjectDAO.getAllSubjectsNRCName().get(0);

        assertEquals("12345 - Sistemas Operativos", result);
    }

    @Test
    void getAllSubjectsNRCName_emptyResultSet_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(subjectDAO.getAllSubjectsNRCName().isEmpty());
    }

    @Test
    void getAllSubjectsNRCName_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo de conexión"));

        assertThrows(OperationException.class, () ->
            subjectDAO.getAllSubjectsNRCName());
    }

    @Test
    void assignStudentToSubject_successful_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(subjectDAO.assignStudentToSubject("S23013127", 12345));
    }

    @Test
    void assignStudentToSubject_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertFalse(subjectDAO.assignStudentToSubject("S23013127", 12345));
    }

    @Test
    void assignStudentToSubject_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo de conexión"));

        assertThrows(OperationException.class, () ->
            subjectDAO.assignStudentToSubject("S23013127", 12345));
    }


    @Test
    void getSubjectNRCByStudentID_found_returnsNRC() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("NRC")).thenReturn("12345");

        assertEquals("12345", subjectDAO.getSubjectNRCByStudentID("S23013127"));
    }

    @Test
    void getSubjectNRCByStudentID_notFound_returnsDefaultMessage() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertEquals("No tiene asignada una experiencia",
            subjectDAO.getSubjectNRCByStudentID("S99999999"));
    }

    @Test
    void getSubjectNRCByStudentID_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo de conexión"));

        assertThrows(OperationException.class, () ->
            subjectDAO.getSubjectNRCByStudentID("S23013127"));
    }
}