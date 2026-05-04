package uv.lis.logic.contracts;


import java.util.ArrayList;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;


public interface IAffiliatedOrganizationDAO {
    public AffiliatedOrganization getOrganizationById(int idAfilliatedOrganization) throws OperationException;

    boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean inactivateOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    ArrayList<String> getAllOrganizationNames() throws OperationException;

    int getOrganizationIdByName(String name) throws OperationException;
}
