package uv.lis.logic.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {
    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 1;

    public static String hashPassword(String plainPassword) {
        String hashedPassword = ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, plainPassword.toCharArray());
        return hashedPassword;
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        boolean isValid = ARGON2.verify(hashedPassword, plainPassword.toCharArray());
        return isValid;
    }

    
}