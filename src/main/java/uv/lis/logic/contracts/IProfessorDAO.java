package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public interface IProfessorDAO {
    Optional<String> getProfessorPersonnelNumberByName(String firstName, String lastName) throws OperationException;

    boolean registerProfessor(Professor professor) throws OperationException;

    boolean modifyProfessor(Professor professor) throws OperationException;

    LinkedHashMap<String, String> getAllActiveProfessorsMap() throws OperationException;

    Optional<Professor> getProfessorById(int id) throws OperationException;

    int getIdUserByProfessorPersonnelNumber(String personnelNumber) throws OperationException;

    ArrayList<String> searchProfessorPersonalNumbers(String prefix) throws OperationException;

    boolean isProfessorInactive(String personnelNumber) throws OperationException;

    boolean inactivateProfessor(String personalNumber) throws OperationException;

    boolean hasSubjectAssigned(String personnelNumber) throws OperationException;

    ArrayList<String> getSubjectsByProfessor(String personnelNumber) throws OperationException;
}
