package uv.lis.logic.contracts;


import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;


public interface IStudentDAO {
    Student getStudentById(String idStudent) throws OperationException;

    boolean registerStudent(Student student) throws OperationException;

    boolean modifyStudent(Student student) throws OperationException;

    boolean inactivateStudent(Student student) throws OperationException;
}
