package uv.lis.logic.contracts;

import java.util.ArrayList;

import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public interface IStudentDAO {
    Student getStudentById(int idStudent) throws OperationException;

    ArrayList<Student> getActiveStudentsNotInSubject() throws OperationException;

    boolean registerStudent(Student student) throws OperationException;

    boolean modifyStudent(Student student) throws OperationException;

    boolean inactivateStudent(String studentId) throws OperationException;

    int getIdUserByStudentId(String studentId) throws OperationException;

    ArrayList<String> searchStudentIds(String prefix) throws OperationException;

    boolean isStudentInactive(String studentId) throws OperationException;
}
