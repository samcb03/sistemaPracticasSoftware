package uv.lis.logic.contracts;


import java.util.List;

import uv.lis.logic.dto.SchoolPeriod;

public interface ISchoolPeriodDAO {

    List<SchoolPeriod> getSchoolPeriodbyId(int idSchoolPeriod);

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod);

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod);   
}
