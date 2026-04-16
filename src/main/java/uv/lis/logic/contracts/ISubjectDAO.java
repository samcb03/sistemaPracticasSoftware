package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.ArrayList;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public interface ISubjectDAO {
    boolean registerSubject(Subject subject) throws SQLException, OperationException;

    ArrayList<Subject> getAllSubjects() throws SQLException, OperationException;
}
