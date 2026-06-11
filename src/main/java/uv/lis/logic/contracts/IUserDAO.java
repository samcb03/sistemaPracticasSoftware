package uv.lis.logic.contracts;

import java.util.Optional;

import uv.lis.logic.dto.User;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for users and authentication.
 */
public interface IUserDAO {

    /**
     * Registers a new user in the system.
     *
     * @param user the user data to register
     * @return the generated identifier of the registered user
     * @throws OperationException if the user cannot be registered
     */
    int registerUser(User user) throws OperationException;

    /**
     * Authenticates a user from their email and password.
     *
     * @param email the email of the user to authenticate
     * @param password the password of the user to authenticate
     * @return the authenticated user if the credentials are valid, empty otherwise
     * @throws AuthenticateException if the authentication cannot be completed
     */
    Optional<User> authenticate(String email, String password) throws AuthenticateException;

    /**
     * Indicates whether an active coordinator exists in the system.
     *
     * @return true if an active coordinator exists, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean existActiveCoordinator() throws OperationException;

    /**
     * Updates whether a user must receive the email authentication code.
     *
     * @param userId the identifier of the user to update
     * @param isActive true to keep email authentication, false to disable it
     * @return true if the preference was updated, false otherwise
     * @throws OperationException if the update cannot be completed
     */
    boolean updateEmailAuthenticationPreference(int userId, boolean isActive) throws OperationException;
}