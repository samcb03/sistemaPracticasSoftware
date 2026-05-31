package utilstest;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import uv.lis.logic.utils.PasswordHasher;

class PasswordHasherTest {

    @Test
    void hashPassword_validPassword_returnsNonNullHash() {
        String result = PasswordHasher.hashPassword("miContraseña123");

        assertNotNull(result);
    }

    @Test
    void hashPassword_validPassword_hashDiffersFromPlain() {
        String plainPassword = "miContraseña123";

        String result = PasswordHasher.hashPassword(plainPassword);

        assertNotEquals(plainPassword, result);
    }

    @Test
    void hashPassword_samePasswordTwice_producesDifferentHashes() {
        String password = "miContraseña123";

        String firstHash  = PasswordHasher.hashPassword(password);
        String secondHash = PasswordHasher.hashPassword(password);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void verifyPassword_correctPassword_returnsTrue() {
        String plainPassword = "miContraseña123";
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        boolean result = PasswordHasher.verifyPassword(plainPassword, hashedPassword);

        assertTrue(result);
    }

    @Test
    void verifyPassword_wrongPassword_returnsFalse() {
        String hashedPassword = PasswordHasher.hashPassword("miContraseña123");

        boolean result = PasswordHasher.verifyPassword("otraContraseña", hashedPassword);

        assertFalse(result);
    }
    @Test
    void verifyPassword_emptyPassword_returnsFalse() {
        String hashedPassword = PasswordHasher.hashPassword("miContraseña123");

        boolean result = PasswordHasher.verifyPassword("", hashedPassword);

        assertFalse(result);
    }
}