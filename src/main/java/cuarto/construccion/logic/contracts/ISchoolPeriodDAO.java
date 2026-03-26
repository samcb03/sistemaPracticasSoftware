package cuarto.construccion.logic.contracts;


import java.util.List;

import cuarto.construccion.logic.dto.SchoolPeriod;

public interface ISchoolPeriodDAO {

    List<SchoolPeriod> getSchoolPeriodbyId(int idSchoolPeriod);

    boolean registerSchoolPeriod(SchoolPeriod schoolPeriod);

    boolean modifySchoolPeriod(SchoolPeriod schoolPeriod);   
}
