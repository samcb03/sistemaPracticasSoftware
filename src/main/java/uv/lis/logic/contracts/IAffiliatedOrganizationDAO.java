package uv.lis.logic.contracts;


import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;


public interface IAffiliatedOrganizationDAO {
    public AffiliatedOrganization getAffiliatedOrganizationById(int idAfilliatedOrganization) throws OperationException;

    boolean registerAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean modifyAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean inactivateAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;
}
