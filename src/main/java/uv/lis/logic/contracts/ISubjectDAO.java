package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Subject;


public interface ISubjectDAO {
    List<Subject> getSubjectbyId(int idSubject) throws SQLException;

    boolean registerSubject(Subject subject) throws SQLException;

    boolean modifySubject(Subject subject) throws SQLException;
}
