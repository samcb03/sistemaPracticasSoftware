package uv.lis.logic.contracts;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;

public interface IUserDAO {

    boolean registerUser(User user);

    Professor getProfessorById(int idProfessor);

    boolean registerProfessor(Professor professor);

    boolean modifyProfessor(Professor professor);

    boolean inactiveProfessor(Professor professor);

    Student getStudentById(int idStudent);

    boolean registerStudent(Student student);

    boolean modifyStudent(Student student);

    boolean inactiveStudent(Student student);


}