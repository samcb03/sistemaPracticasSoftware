package src.test.java;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;


public class StudentDAOTest {
    private StudentDAO studentDAO;

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); 

        when(connectionManager.getConnection()).thenReturn(connection);
        studentDAO = new StudentDAO(connectionManager);
    }

    @Test
    void getStudentById_successful_returnsStudent() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idUsuario")).thenReturn(1);
        when(resultSet.getString("matricula")).thenReturn("S123");
        when(resultSet.getString("nombre")).thenReturn("Denisse");
        when(resultSet.getString("apellidos")).thenReturn("Reyes");
        when(resultSet.getString("contraseña")).thenReturn("123456");
        when(resultSet.getDate("fechaNacimiento")).thenReturn(new Date(System.currentTimeMillis()));
        when(resultSet.getString("genero")).thenReturn("Femenino");

        Student expectedStudent = new Student(1, "Denisse", "Reyes",
            "Deni@3234581", "deni@gmail.com", 1, false, "S123", 
            new Date(System.currentTimeMillis()), 0, "Femenino");

        assertEquals(expectedStudent, studentDAO.getStudentById("S123"));
    }


    @Test
    void getStudentById_failure_returnsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        assertThrows(OperationException.class, () -> {
            studentDAO.getStudentById("S999");
        });
    }

    @Test
    void registerStudent_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");
        student.setBirthDate(new java.util.Date());
        student.setGender("Femenino");
        student.setActive(true);

        boolean result = studentDAO.registerStudent(student);

        assertTrue(result);
    }

    @Test
    void registerStudent_failure_returnsFalse() throws Exception {
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

    @Test
    void modifyStudent_successful_returnsTrue() throws Exception {
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

    @Test
    void inactivateStudent_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Student student = new Student();
        student.setId(1);
        student.setIdStudent("S123");

        boolean result = studentDAO.inactivateStudent(student);

        assertTrue(result);
    }
}