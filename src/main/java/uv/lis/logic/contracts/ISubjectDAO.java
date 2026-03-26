package uv.lis.logic.contracts;


import java.util.List;

import uv.lis.logic.dto.Subject;

public interface ISubjectDAO {

    List<Subject> getSubjectbyId(int idSubject);

    boolean registerSubject(Subject subject);

    boolean modifySubject(Subject subject);
    
}
