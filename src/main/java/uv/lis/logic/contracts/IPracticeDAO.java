package uv.lis.logic.contracts;

import uv.lis.logic.dto.Practice;
import uv.lis.logic.exceptions.OperationException;

public interface IPracticeDAO {
    Practice getPracticeByStudent(String idStudent) throws OperationException;

    boolean registerPractice(Practice practice) throws OperationException;

    boolean existsByStudent(String idStudent) throws OperationException;
}