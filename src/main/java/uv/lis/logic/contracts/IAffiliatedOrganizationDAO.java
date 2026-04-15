package uv.lis.logic.contracts;


import java.sql.SQLException;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;


public interface IAffiliatedOrganizationDAO {
    public AffiliatedOrganization getAffiliatedOrganizationById(int idAfilliatedOrganization) throws SQLException, OperationException;

    boolean registerAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException, OperationException;

    boolean modifyAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException, OperationException;

    boolean inactivateAffiliatedOrganization(AffiliatedOrganization affiliatedOrganization) throws SQLException, OperationException;
}
