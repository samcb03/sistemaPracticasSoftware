package uv.lis.logic.contracts;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public interface IProfessorDAO {
    Professor getProfessorByPersonalNumber(String idProfessor) throws OperationException;

    boolean registerProfessor(Professor professor) throws OperationException;

    boolean modifyProfessor(Professor professor) throws OperationException;

    boolean inactivateProfessor(Professor professor) throws OperationException;
}
