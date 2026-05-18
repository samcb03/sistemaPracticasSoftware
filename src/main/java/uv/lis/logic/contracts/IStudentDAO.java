package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.Student;
import uv.lis.logic.exceptions.OperationException;

public interface IStudentDAO {
    Optional<Student> getStudentById(int idStudent) throws OperationException;

    ArrayList<Student> getActiveStudentsNotInSubject() throws OperationException;

    boolean registerStudent(Student student) throws OperationException;

    boolean modifyStudent(Student student) throws OperationException;

    boolean inactivateStudent(String studentId) throws OperationException;

    Optional<Integer> getIdUserByStudentId(String studentId) throws OperationException;

    ArrayList<String> searchStudentIds(String prefix) throws OperationException;

    boolean isStudentInactive(String studentId) throws OperationException;
}
