package cuarto.construccion.logic.contracts;


import java.util.List;

import cuarto.construccion.logic.dto.Subject;

public interface ISubjectDAO {

    List<Subject> getSubjectbyId(int idSubject);

    boolean registerSubject(Subject subject);

    boolean modifySubject(Subject subject);
    
}
