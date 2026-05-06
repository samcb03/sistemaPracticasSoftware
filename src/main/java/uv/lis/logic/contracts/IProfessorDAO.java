package uv.lis.logic.contracts;


import java.util.ArrayList;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;


public interface IProfessorDAO {
    String getProfessorPersonnelNumberByName(String firstName, String lastName) throws OperationException;

    boolean registerProfessor(Professor professor) throws OperationException;

    boolean modifyProfessor(Professor professor) throws OperationException;

    boolean inactivateProfessor(Professor professor) throws OperationException;

    ArrayList<String> getAllActiveProfessorsNames() throws OperationException;
}
