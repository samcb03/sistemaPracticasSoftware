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
import java.sql.SQLException;
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

    private Student builderStudent(int id, String idStudent, String firstName, 
        String lastName, Date birthDate, String gender) {
        Student student = new Student();
        student.setId(id);
        student.setIdStudent(idStudent);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setBirthDate(birthDate);
        student.setGender(gender);
        return student;
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
        when(resultSet.getDate("fechaNacimiento")).thenReturn(new Date(System.currentTimeMillis()));
        when(resultSet.getString("genero")).thenReturn("Femenino");

        assertEquals(1, studentDAO.getStudentById(1).getId());
    }

    @Test
    void getStudentById_notFound_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.getStudentById(1)
        );
        assertTrue(exception.getMessage().contains("No se encontró un alumno con la matricula"));
    }

    @Test
    void getStudentById_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.getStudentById(1)
        );
        assertTrue(exception.getMessage().contains("No se pudo buscar el alumno"));
    }

    @Test
    void registerStudent_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.registerStudent(
            builderStudent(1, "S123", "Denisse", "Reyes",
                new Date(System.currentTimeMillis()), "Femenino")));
    }

    @Test
    void registerStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.registerStudent(
                builderStudent(1, "S123", "Denisse", "Reyes",
                    new Date(System.currentTimeMillis()), "Femenino")));
        assertTrue(exception.getMessage().contains("No se pudo registrar al alumno"));
    }

    @Test
    void registerStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.registerStudent(
                builderStudent(1, "S123", "Denisse", "Reyes",
                    new Date(System.currentTimeMillis()), "Femenino")));
        assertTrue(exception.getMessage().contains("No se pudo registrar al alumno"));
    }

    @Test
    void modifyStudent_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.modifyStudent(
            builderStudent(1, "S123", "Samuel", "Carreto", 
                new Date(System.currentTimeMillis()), "Masculino")));
    }

    @Test
    void modifyStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.modifyStudent(
                builderStudent(1, "S123", "Samuel", "Carreto",
                    new Date(System.currentTimeMillis()), "Masculino")));
        assertTrue(exception.getMessage().contains("No se pudo modificar al alumno"));
    }

    @Test
    void modifyStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.modifyStudent(
                builderStudent(1, "S123", "Samuel", "Carreto",
                    new Date(System.currentTimeMillis()), "Masculino")));
        assertTrue(exception.getMessage().contains("No se pudo modificar al alumno"));
    }

    @Test
    void inactivateStudent_successful_returnsTrue() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(studentDAO.inactivateStudent(
            builderStudent(1, "S123", null, null, null, null)));
    }

    @Test
    void inactivateStudent_noRowsAffected_throwsOperationException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.inactivateStudent(
                builderStudent(1, "S123", null, null, null, null)));
        assertTrue(exception.getMessage().contains("No se pudo inactivar al alumno"));
    }

    @Test
    void inactivateStudent_sqlError_throwsOperationException() throws Exception {
        when(connectionManager.getConnection()).thenThrow(new SQLException("Fallo"));

        OperationException exception = assertThrows(OperationException.class, () ->
            studentDAO.inactivateStudent(
                builderStudent(1, "S123", null, null, null, null)));
        assertTrue(exception.getMessage().contains("No se pudo inactivar al alumno"));
    }
}