package uv.lis.logic.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Handles secure storage and verification of passwords
 * so that plain-text passwords are never persisted in the database.
 *
 * Uses the Argon2id algorithm, which is resistant to both
 * GPU-based and side-channel attacks.
 */
public class PasswordHasher {
    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 1;

    /**
     * Produces a secure hash from a plain-text password
     * that can be safely stored in the database.
     *
     * @param plainPassword the password as entered by the user
     * 
     * @return the hashed representation of the password
     */
    public static String hashPassword(String plainPassword) {
        String hashedPassword = ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, plainPassword.toCharArray());
        return hashedPassword;
    }

    /**
     * Confirms whether a plain-text password matches a previously stored hash,
     * allowing the application to authenticate a user without ever storing
     * their original password.
     *
     * @param plainPassword  the password as entered by the user at login
     * 
     * @param hashedPassword the hash stored in the database at registration
     * 
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        boolean isValid = ARGON2.verify(hashedPassword, plainPassword.toCharArray());
        return isValid;
    }
}