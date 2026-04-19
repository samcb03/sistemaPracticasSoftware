package uv.lis.logic.contracts;


import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;


public interface ISchoolPeriodDAO {
    SchoolPeriod getSchoolPeriodbyId(int idSchoolPeriod) throws OperationException;

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    boolean existsSchoolPeriod(int idPeriod) throws OperationException;
}
