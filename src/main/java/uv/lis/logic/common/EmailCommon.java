package uv.lis.logic.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import uv.lis.logic.dto.VerificationChallenge;
import uv.lis.logic.exceptions.EmailException;
import uv.lis.logic.utils.PasswordHasher;

/**
 * Generates, sends and verifies the one-time codes used for the email-based
 * two-step authentication. The plain code travels to the user's email while its
 * hash and expiration time are kept in a {@link VerificationChallenge}.
 */
public class EmailCommon {
    private static final Logger LOGGER = Logger.getLogger(EmailCommon.class.getName());

    private static final int MIN_CODE_VALUE = 100000;
    private static final int MAX_CODE_VALUE = 1000000;
    private static final int CODE_EXPIRATION_MINUTES = 10;

    private static final String PROPERTIES_FILE = "emailConfig/email.properties";
    private static final String KEY_HOST = "mail.host";
    private static final String KEY_PORT = "mail.port";
    private static final String KEY_USER = "mail.user";
    private static final String KEY_PASSWORD = "mail.password";

    private static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    private static final String SMTP_STARTTLS_PROPERTY = "mail.smtp.starttls.enable";
    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String SMTP_ENABLED_VALUE = "true";

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String EMAIL_SUBJECT = "Código de verificación";
    private static final String EMAIL_BODY_TEMPLATE = "Su código de verificación es: {0}. Vigencia de {1} minutos.";

    private final SecureRandom secureRandom = new SecureRandom();
    private Properties properties = new Properties();

    /**
     * Creates the component and loads the email configuration from the external
     * properties file.
     */
    public EmailCommon() {
        loadProperties();
    }

    /**
     * Generates a verification code, sends it to the given email and returns the
     * challenge holding the hashed code and its expiration time.
     *
     * @param email the email address where the code is sent
     * @return the challenge used later to verify the code entered by the user
     * @throws EmailException if the email cannot be sent
     */
    public VerificationChallenge sendVerificationCode(String email) throws EmailException {
        String plainCode = generateCode();
        String hashedCode = PasswordHasher.hashPassword(plainCode);
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);

        sendVerificationEmail(email, plainCode);

        return new VerificationChallenge(email, hashedCode, expirationTime);
    }

    /**
     * Checks whether the code entered by the user matches the challenge and has
     * not expired.
     *
     * @param challenge the challenge created when the code was sent
     * @param inputCode the code entered by the user
     * @return true if the code is valid and still in force, false otherwise
     */
    public boolean verifyCode(VerificationChallenge challenge, String inputCode) {
        boolean isValid = false;

        if (LocalDateTime.now().isBefore(challenge.getExpirationTime())) {
            isValid = PasswordHasher.verifyPassword(inputCode, challenge.getHashedCode());
        }

        return isValid;
    }

    private String generateCode() {
        int code = secureRandom.nextInt(MIN_CODE_VALUE, MAX_CODE_VALUE);
        String finalCode = String.valueOf(code);
        return finalCode;
    }

    private void sendVerificationEmail(String email, String code) throws EmailException {
        String host = properties.getProperty(KEY_HOST);
        String port = properties.getProperty(KEY_PORT);
        String username = properties.getProperty(KEY_USER);
        String password = properties.getProperty(KEY_PASSWORD);

        Session session = Session.getInstance(buildMailProperties(host, port));

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(EMAIL_SUBJECT, CHARSET_UTF8);
            message.setText(MessageFormat.format(EMAIL_BODY_TEMPLATE, code, CODE_EXPIRATION_MINUTES), CHARSET_UTF8);

            Transport.send(message, username, password);
            LOGGER.log(Level.INFO, "Código de verificación enviado al correo: {0}", email);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Error al enviar el correo de verificación", e);
            throw new EmailException("No se pudo enviar el código de verificación. Intente más tarde", e);
        }
    }

    private Properties buildMailProperties(String host, String port) {
        Properties mailProperties = new Properties();
        mailProperties.put(SMTP_AUTH_PROPERTY, SMTP_ENABLED_VALUE);
        mailProperties.put(SMTP_STARTTLS_PROPERTY, SMTP_ENABLED_VALUE);
        mailProperties.put(SMTP_HOST_PROPERTY, host);
        mailProperties.put(SMTP_PORT_PROPERTY, port);
        return mailProperties;
    }

    private void loadProperties() {
        try (FileInputStream propertiesFile = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(propertiesFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar propiedades de correo", e);
        }
    }
}