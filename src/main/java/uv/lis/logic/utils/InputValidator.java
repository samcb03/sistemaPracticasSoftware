package uv.lis.logic.utils;


import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;


public final class InputValidator {

    private InputValidator() {

    }

    public static final int MAX_TEXT_LENGTH = 255;
    public static final int STUDENT_ID_LENGTH = 9;
    public static final int PROFESSOR_ID_LENGTH = 10;
    public static final int MIN_POSITIVE_INTEGER = 1;
    public static final int INVALID_ID = -1;
    private static final int MINIMUM_AGE = 18;
    public static final String LETTERS_ONLY_REGEX = "[\\p{L}\\s]+";
    public static final String ONLY_NUMBERS_REGEX = "\\d+";
    public static final String EMAIL_REGEX = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";
    public static final String PHONE_REGEX = "^[0-9]{7,15}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*_]).{12,}$";
    public static final String REPEAT_LETTERS_REGEX = ".*(.)\\1{2,}.*";

    public static Optional<String> validateLettersOnly(String fieldValue, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.isEmpty() || fieldValue.length() > MAX_TEXT_LENGTH) {
            validationResult = Optional.of(fieldName + " no puede estar vacío o tener más de "
                + MAX_TEXT_LENGTH + " caracteres");
        } else if (!fieldValue.matches(LETTERS_ONLY_REGEX)) {
            validationResult = Optional.of(fieldName + " solo acepta letras");
        } else if (fieldValue.matches(REPEAT_LETTERS_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener caracteres repetidos consecutivamente");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validateEmail(String emailValue) {
        Optional<String> validationResult;
        if (emailValue.isEmpty()) {
            validationResult = Optional.of("El correo electrónico no puede estar vacío");
        } else if (!emailValue.matches(EMAIL_REGEX)) {
            validationResult = Optional.of("El correo electrónico no tiene un formato válido");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validatePhoneNumber(String phoneValue) {
        Optional<String> validationResult;
        if (phoneValue.isEmpty()) {
            validationResult = Optional.of("El número de teléfono no puede estar vacío");
        } else if (!phoneValue.matches(PHONE_REGEX)) {
            validationResult = Optional.of("El número de teléfono solo acepta entre 7 y 15 dígitos");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validatePassword(String passwordValue) {
        passwordValue = passwordValue.trim();
        
        Optional<String> validationResult;
        if (passwordValue.isEmpty()) {
            validationResult = Optional.of("La contraseña no puede estar vacía");
        } else if (!passwordValue.matches(PASSWORD_REGEX)) {
            validationResult = Optional.of("La contraseña debe tener una mayúscula, un carácter especial y un número");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validatePositiveInteger(String integerValue, String fieldName) {
        Optional<String> validationResult;
        if (integerValue.isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else {
            validationResult = validateIntegerRange(integerValue, fieldName);
        }
        return validationResult;
    }

    private static Optional<String> validateIntegerRange(String integerValue, String fieldName) {
        Optional<String> validationResult;
        try {
            int parsedNumber = Integer.parseInt(integerValue);
            if (parsedNumber < MIN_POSITIVE_INTEGER) {
                validationResult = Optional.of(fieldName + " debe ser un número positivo");
            } else {
                validationResult = Optional.empty();
            }
        } catch (NumberFormatException numberFormatException) {
            validationResult = Optional.of(fieldName + " debe ser un número entero válido");
        }
        return validationResult;
    }

    public static Optional<String> validateExactLength(String fieldValue, int requiredLength, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.isEmpty() || fieldValue.length() != requiredLength) {
            validationResult = Optional.of(fieldName + " debe tener exactamente " + requiredLength + " caracteres");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validateComboBox(Object selectedValue, String fieldName) {
        Optional<String> validationResult;
        if (selectedValue == null) {
            validationResult = Optional.of("Seleccione " + fieldName);
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validateBirthDate(LocalDate birthDate) {
        Optional<String> validationResult;
        
        if (birthDate == null) {
            validationResult = Optional.of("Seleccione una fecha de nacimiento");
        } else {
            LocalDate today = LocalDate.now();
            int age = Period.between(birthDate, today).getYears();
            
            if (birthDate.isAfter(today)) {
                validationResult = Optional.of("La fecha de nacimiento no puede ser futura");
            } else if (age < MINIMUM_AGE) {
                validationResult = Optional.of("El estudiante debe ser mayor de " + MINIMUM_AGE + " años");
            } else {
                validationResult = Optional.empty();
            }
        }
        
        return validationResult;
    }
}