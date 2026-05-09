package uv.lis.logic.contracts;


import java.util.ArrayList;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;


public interface ISchoolPeriodDAO {
    ArrayList<String> getAllSchoolPeriodsNames() throws OperationException;
    
    String getSchoolPeriodIdByName(String periodName) throws OperationException;

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws OperationException;

    boolean existsSchoolPeriod(int idPeriod) throws OperationException;
}
