package commontest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import uv.lis.logic.common.EmailCommon;
import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.utils.PasswordHasher;

class EmailCommonTest {

    private static final int OFFSET_MINUTES = 5;

    private static final String EMAIL = "juan@example.com";
    private static final String HASHED_CODE = "hashedCode";
    private static final String INPUT_CODE = "123456";

    private EmailCommon emailCommon;

    @BeforeEach
    void setUp() {
        emailCommon = new EmailCommon();
    }

    private VerificationChallenge builderChallenge(LocalDateTime expirationTime) {
        return new VerificationChallenge(EMAIL, HASHED_CODE, expirationTime);
    }

    @Test
    void verifyCode_validNotExpiredCode_returnsTrue() {
        VerificationChallenge challenge = builderChallenge(LocalDateTime.now().plusMinutes(OFFSET_MINUTES));

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword(INPUT_CODE, HASHED_CODE)).thenReturn(true);

            assertTrue(emailCommon.verifyCode(challenge, INPUT_CODE));
        }
    }

    @Test
    void verifyCode_incorrectCode_returnsFalse() {
        VerificationChallenge challenge = builderChallenge(LocalDateTime.now().plusMinutes(OFFSET_MINUTES));

        try (MockedStatic<PasswordHasher> mockedHasher = mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verifyPassword(INPUT_CODE, HASHED_CODE)).thenReturn(false);

            assertFalse(emailCommon.verifyCode(challenge, INPUT_CODE));
        }
    }

    @Test
    void verifyCode_expiredCode_returnsFalse() {
        VerificationChallenge challenge = builderChallenge(LocalDateTime.now().minusMinutes(OFFSET_MINUTES));

        assertFalse(emailCommon.verifyCode(challenge, INPUT_CODE));
    }
}