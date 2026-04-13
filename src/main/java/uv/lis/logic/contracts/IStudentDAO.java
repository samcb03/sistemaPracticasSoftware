package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.Student;


public interface IStudentDAO {
    Student getStudentById(String idStudent) throws SQLException;

    boolean registerStudent(Student student) throws SQLException;

    boolean modifyStudent(Student student) throws SQLException;

    boolean inactivateStudent(Student student) throws SQLException;
}
