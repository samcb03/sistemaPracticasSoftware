package src.test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudentDAOTest {

    private MySQLConnectionManager connectionManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private StudentDAO studentDAO;

    @BeforeEach
    void setUp() throws Exception {
        connectionManager = mock(MySQLConnectionManager.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(connectionManager.getConnection()).thenReturn(connection);

        studentDAO = new StudentDAO(connectionManager);
    }

    // ============================
    // 🟢 TEST: getStudentById SUCCESS
    // ============================
    @Test
    void testGetStudentByIdSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idUsuario")).thenReturn(1);
        when(resultSet.getString("matricula")).thenReturn("S123");
        when(resultSet.getString("nombre")).thenReturn("Denisse");
        when(resultSet.getString("apellidos")).thenReturn("Reyes");
        when(resultSet.getDate("fechaNacimiento")).thenReturn(new Date(System.currentTimeMillis()));
        when(resultSet.getString("genero")).thenReturn("Femenino");
        when(resultSet.getBoolean("lenguaIndigena")).thenReturn(false);

        Student student = studentDAO.getStudentById("S123");

        assertNotNull(student);
        assertEquals("S123", student.getIdStudent());
        assertEquals("Denisse", student.getFirstName());
    }

    // ============================
    // 🔴 TEST: getStudentById NOT FOUND
    // ============================
    @Test
    void testGetStudentByIdNotFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () -> {
            studentDAO.getStudentById("S999");
        });
    }

    // ============================
    // 🟢 TEST: registerStudent SUCCESS
    // ============================
    @Test
    void testRegisterStudentSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");
        student.setBirthDate(new java.util.Date());
        student.setGender("Femenino");
        student.setIndigenousLanguage(false);
        student.setInactive(false);

        boolean result = studentDAO.registerStudent(student);

        assertTrue(result);
    }

    // ============================
    // 🔴 TEST: registerStudent ERROR
    // ============================
    @Test
    void testRegisterStudentFail() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");
        student.setBirthDate(new java.util.Date());

        assertThrows(OperationException.class, () -> {
            studentDAO.registerStudent(student);
        });
    }

    // ============================
    // 🟢 TEST: modifyStudent SUCCESS
    // ============================
    @Test
    void testModifyStudentSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");
        student.setFirstName("Nuevo");
        student.setLastName("Nombre");

        boolean result = studentDAO.modifyStudent(student);

        assertTrue(result);
    }

    // ============================
    // 🟢 TEST: inactivateStudent SUCCESS
    // ============================
    @Test
    void testInactivateStudentSuccess() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");

        boolean result = studentDAO.inactivateStudent(student);

        assertTrue(result);
    }
}