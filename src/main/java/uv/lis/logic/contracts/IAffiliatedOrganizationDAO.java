package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public interface IAffiliatedOrganizationDAO {
    Optional<AffiliatedOrganization> getOrganizationById(int idAfilliatedOrganization) throws OperationException;

    boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean inactivateOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    ArrayList<String> getAllOrganizationNames() throws OperationException;

    int getOrganizationIdByName(String name) throws OperationException;

    Optional<String> getOrganizationBySupervisorName(String nombreSupervisor) throws OperationException;

    boolean isOrganizationInactive(int organizationId) throws OperationException;

    ArrayList<String> searchOrganizationByName(String prefix) throws OperationException;
}
