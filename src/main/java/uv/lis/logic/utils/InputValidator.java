package uv.lis.logic.utils;

public final class InputValidator {

    private InputValidator() {}

    public static final int MAX_TEXT_LENGTH = 255;
    public static final int STUDENT_ID_LENGTH = 9;

    public static final int MIN_POSITIVE_INTEGER = 1;
    public static final int INVALID_ID = -1;

    public static final String LETTERS_ONLY_REGEX = "[\\p{L}\\s]+";
    public static final String ONLY_NUMBERS_REGEX = "\\d+";
    public static final String EMAIL_REGEX = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_REGEX = "^[0-9]{7,15}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{12,}$";
}