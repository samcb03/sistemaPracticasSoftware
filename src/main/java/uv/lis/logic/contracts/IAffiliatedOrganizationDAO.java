package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.AffiliatedOrganization;


public interface IAffiliatedOrganizationDAO {
    public AffiliatedOrganization getAffiliatedOrganizationById(int idAfilliatedOrganization) throws SQLException;

    boolean registerAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException;

    boolean modifyAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException;

    boolean inactiveAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException;
}
