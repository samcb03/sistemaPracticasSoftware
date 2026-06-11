package uv.lis.logic.dto;

import java.time.LocalDateTime;

public class VerificationChallenge {
    private String email;
    private String hashedCode;
    private LocalDateTime expirationTime;

    public VerificationChallenge() {

    }

    public VerificationChallenge(String email, String hashedCode, LocalDateTime expirationTime) {
        this.email = email;
        this.hashedCode = hashedCode;
        this.expirationTime = expirationTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedCode() {
        return hashedCode;
    }

    public void setHashedCode(String hashedCode) {
        this.hashedCode = hashedCode;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}