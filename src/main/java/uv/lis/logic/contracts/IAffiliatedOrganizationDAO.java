package uv.lis.logic.contracts;

import uv.lis.logic.dto.AffiliatedOrganization;

public interface IAffiliatedOrganizationDAO {

    public AffiliatedOrganization getAffiliatedOrganizationById(int idAfilliatedOrganization);

    boolean registerAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization);

    boolean modifyAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization);

    boolean inactiveAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization);

}
