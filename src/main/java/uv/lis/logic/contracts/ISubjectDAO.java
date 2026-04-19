package uv.lis.logic.contracts;


import java.util.ArrayList;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

public interface ISubjectDAO {
    boolean registerSubject(Subject subject) throws OperationException;

    ArrayList<Subject> getAllSubjects() throws OperationException;
}
