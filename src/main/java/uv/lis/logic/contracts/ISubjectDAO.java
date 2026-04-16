package uv.lis.logic.contracts;


import java.sql.SQLException;

import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public interface ISubjectDAO {
    Subject getSubjectById(int idSubject) throws SQLException, OperationException;

    boolean registerSubject(Subject subject) throws SQLException, OperationException;

    boolean modifySubject(Subject subject) throws SQLException, OperationException;
}
