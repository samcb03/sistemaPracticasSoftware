package uv.lis.logic.contracts;

import java.util.ArrayList;
import java.util.Optional;

import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.exceptions.OperationException;

public interface IAffiliatedOrganizationDAO {
    Optional<AffiliatedOrganization> getOrganizationById(int idAfilliatedOrganization) throws OperationException;

    boolean registerOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean modifyOrganization(AffiliatedOrganization affiliatedOrganization) throws OperationException;

    boolean inactivateOrganization(String organizationName) throws OperationException;

    ArrayList<String> getAllOrganizationNames() throws OperationException;

    int getOrganizationIdByName(String name) throws OperationException;

    Optional<String> getOrganizationBySupervisorName(String nombreSupervisor) throws OperationException;

    boolean isOrganizationInactive(String organizationName) throws OperationException;

    ArrayList<String> searchOrganizationByName(String prefix) throws OperationException;

    Optional<AffiliatedOrganization> getOrganizationByName(String organizationName) throws OperationException;

    ArrayList<String> getProjectsByOrganization(String organizationName) throws OperationException;

    boolean hasProjectsActives(String organizationName) throws OperationException;
}
