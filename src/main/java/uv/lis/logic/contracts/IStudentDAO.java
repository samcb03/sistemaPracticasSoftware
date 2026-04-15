package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;


public interface IStudentDAO {
    Student getStudentById(String idStudent) throws SQLException, OperationException;

    boolean registerStudent(Student student) throws SQLException, OperationException;

    boolean modifyStudent(Student student) throws SQLException, OperationException;

    boolean inactivateStudent(Student student) throws SQLException, OperationException;
}
