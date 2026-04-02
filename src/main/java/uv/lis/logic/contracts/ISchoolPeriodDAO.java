package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.SchoolPeriod;


public interface ISchoolPeriodDAO {
    List<SchoolPeriod> getSchoolPeriodbyId(int idSchoolPeriod) throws SQLException;

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod) throws SQLException;

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod) throws SQLException;
}
