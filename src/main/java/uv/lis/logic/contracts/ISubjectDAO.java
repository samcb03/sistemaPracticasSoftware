package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.Subject;


public interface ISubjectDAO {
    Subject getSubjectById(int idSubject) throws SQLException;

    boolean registerSubject(Subject subject) throws SQLException;

    boolean modifySubject(Subject subject) throws SQLException;

    boolean existsSchoolPeriod(int idPeriod) throws SQLException;
}
