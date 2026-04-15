package uv.lis.logic.contracts;

import java.sql.SQLException;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public interface IProfessorDAO {
    Professor getProfessorByPersonalNumber(String idProfessor) throws SQLException, OperationException;

    boolean registerProfessor(Professor professor) throws SQLException, OperationException;

    boolean modifyProfessor(Professor professor) throws SQLException, OperationException;

    boolean inactivateProfessor(Professor professor) throws SQLException, OperationException;
}
