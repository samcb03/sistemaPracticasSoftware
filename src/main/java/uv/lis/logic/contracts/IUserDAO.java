package uv.lis.logic.contracts;


import java.sql.SQLException;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;


public interface IUserDAO {
    boolean registerUser(User user) throws SQLException;

    Professor getProfessorById(int idProfessor) throws SQLException;

    boolean registerProfessor(Professor professor) throws SQLException;

    boolean modifyProfessor(Professor professor) throws SQLException;

    boolean inactivateProfessor(Professor professor) throws SQLException;

    Student getStudentById(String idStudent) throws SQLException;

    boolean registerStudent(Student student) throws SQLException;

    boolean modifyStudent(Student student) throws SQLException;

    boolean inactivateStudent(Student student) throws SQLException;

    User authenticate(String identification, String password);

    String getUserType(User user) throws SQLException;
}