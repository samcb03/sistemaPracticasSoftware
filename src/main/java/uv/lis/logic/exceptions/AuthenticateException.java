package uv.lis.logic.exceptions;

public class AuthenticateException extends Exception {

    public AuthenticateException(String message, Throwable cause) {
        super(message, cause);
    }

     public AuthenticateException(String message) {
        super(message);
    }

}
