package uv.lis.logic.contracts;

import java.sql.SQLException;

import uv.lis.logic.dto.Professor;

public interface IProfessorDAO {
    Professor getProfessorByPersonalNumber(String idProfessor) throws SQLException;

    boolean registerProfessor(Professor professor) throws SQLException;

    boolean modifyProfessor(Professor professor) throws SQLException;

    boolean inactivateProfessor(Professor professor) throws SQLException;
}
