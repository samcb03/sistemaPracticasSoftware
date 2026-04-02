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

    boolean inactiveProfessor(Professor professor) throws SQLException;

    Student getStudentById(int idStudent) throws SQLException;

    boolean registerStudent(Student student) throws SQLException;

    boolean modifyStudent(Student student) throws SQLException;

    boolean inactiveStudent(Student student) throws SQLException;
}