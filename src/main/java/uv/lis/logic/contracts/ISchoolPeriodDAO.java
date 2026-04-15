package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.exceptions.OperationException;


public interface ISchoolPeriodDAO {
    SchoolPeriod getSchoolPeriodbyId(int idSchoolPeriod) throws SQLException, OperationException;

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws SQLException, OperationException;

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws SQLException, OperationException;

    boolean existsSchoolPeriod(int idPeriod) throws SQLException, OperationException;
}
