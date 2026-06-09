package uv.lis.logic.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

public final class InputValidator {

    private InputValidator() {

    }
    
    public static final int IS_COORDINATOR = 3;
    public static final int NO_ROWS_AFFECTED = 0;
    public static final int MAX_REQUESTS = 3;
    public static final int STATUS_REQUESTED = 1;
    public static final int STATUS_ASSIGNED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int MAX_TEXT_LENGTH = 255;
    public static final int STUDENT_ID_LENGTH = 9;
    public static final int PROFESSOR_ID_LENGTH = 5;
    public static final int MIN_POSITIVE_INTEGER = 1;
    public static final int INVALID_ID = -1;
    private static final int MINIMUM_AGE = 18;
    private static final int MAX_PROJECT_CAPACITY = 2;
    private static final int MAX_PAST_MONTHS = 6;
    public static final String LETTERS_ONLY_REGEX = "^[\\p{L}\\s]+$";
    public static final String LEADING_SPACE_REGEX = "^\\s.*";
    public static final String TRAILING_SPACE_REGEX = ".*\\s$";
    public static final String CONSECUTIVE_SPACES_REGEX = ".*\\s{2,}.*";
    public static final String ONLY_NUMBERS_REGEX = "\\d+";
    public static final String EMAIL_REGEX 
        = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^[0-9]{7,15}$";
    public static final String PASSWORD_REGEX 
        =  "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"|,.<>/?-]).{8,}$";
    public static final String REPEAT_LETTERS_REGEX = "(?i)^.*([\\p{L}])\\1{2,}.*$";
    public static final String STUDENT_ENROLLMENT = "^[Ss]\\d{8}$";

    public static Optional<String> validateNotEmpty(String fieldValue, String fieldName) {
        Optional<String> validationResult;

        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else {
            validationResult = Optional.empty();
        }

        return validationResult;
    }

    public static Optional<String> validateMaxLength(String fieldValue, int maxLength, String fieldName) {
        Optional<String> validationResult;

        if (fieldValue.length() > maxLength) {
            validationResult = Optional.of(fieldName + " no puede exceder los " + maxLength + " caracteres");
        } else {
            validationResult = Optional.empty();
        }

        return validationResult;
    }

    public static Optional<String> validateLettersOnly(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();

        if (!fieldValue.matches(LETTERS_ONLY_REGEX)) {
            validationResult = Optional.of(fieldName + " solo acepta letras y espacios");
        }

        return validationResult;
    }

    public static Optional<String> validateNoLeadingSpace(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();

        if (fieldValue.matches(LEADING_SPACE_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede comenzar con un espacio");
        }

        return validationResult;
    }

    public static Optional<String> validateNoTrailingSpace(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();

        if (fieldValue.matches(TRAILING_SPACE_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede terminar con un espacio");
        }
        return validationResult;
    }

    public static Optional<String> validateNoConsecutiveSpaces(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();

        if (fieldValue.matches(CONSECUTIVE_SPACES_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener espacios consecutivos");
        }

        return validationResult;
    }

    public static Optional<String> validateNoConsecutiveRepeatedLetters(String fieldValue, String fieldName) {
        Optional<String> validationResult;

        if (fieldValue.matches(REPEAT_LETTERS_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener la misma letra repetida 3 veces o más");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validateText(String name, String fieldName) {
        Optional<String> validText = Stream.of(
                InputValidator.validateNotEmpty(name, fieldName),
                InputValidator.validateLettersOnly(name, fieldName),
                InputValidator.validateNoLeadingSpace(name, fieldName),
                InputValidator.validateNoTrailingSpace(name, fieldName),
                InputValidator.validateNoConsecutiveSpaces(name, fieldName),
                InputValidator.validateNoConsecutiveRepeatedLetters(name, fieldName))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
        return validText;
    }

    public static Optional<String> validateEmail(String emailValue, String fieldName) {
        Optional<String> validationResult;
        if (emailValue.isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else if (!emailValue.matches(EMAIL_REGEX)) {
            validationResult = Optional.of(fieldName + " no tiene un formato válido");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validatePhoneNumber(String phoneValue, String fieldName) {
        Optional<String> validationResult;
        if (phoneValue.isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else if (!phoneValue.matches(PHONE_REGEX)) {
            validationResult = Optional.of(fieldName + " solo acepta entre 7 y 15 dígitos");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validatePassword(String passwordValue, String fieldName) {
        passwordValue = passwordValue.trim();
        Optional<String> validationResult;
        if (passwordValue.isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacía");
        } else if (!passwordValue.matches(PASSWORD_REGEX)) {
            validationResult = Optional.of(fieldName + " debe tener una mayúscula, un carácter especial y un número");
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

    public static Optional<String> validateIdStudent(String fieldValue, int requiredLenght, String fieldName) {
        Optional<String> validationResult;
        if(fieldValue.isEmpty() || fieldValue.length() != requiredLenght) {
            validationResult = Optional.of(fieldName + "debe tener exactamente " + requiredLenght + "caracteres");
        } else if(!fieldValue.matches(STUDENT_ENROLLMENT)) {
            validationResult = Optional.of(fieldName + " no sigue el formato establecido empezando con una 'S " );
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

    public static Optional<String> validateBirthDate(LocalDate birthDate, String fieldName) {
        Optional<String> validationResult;
        if (birthDate == null) {
            validationResult = Optional.of("Seleccione una fecha de nacimiento");
        } else {
            LocalDate today = LocalDate.now();
            int age = Period.between(birthDate, today).getYears();
            
            if (birthDate.isAfter(today)) {
                validationResult = Optional.of(fieldName + " no puede ser futura");
            } else if (age < MINIMUM_AGE) {
                validationResult = Optional.of("El alumno debe ser mayor de " + MINIMUM_AGE + " años");
            } else {
                validationResult = Optional.empty();
            }
        }
        return validationResult;
    }

    public static Optional<String> validateStartDate(LocalDate startDate, String fieldName) {
        Optional<String> validationResult;
        if (startDate == null) {
            validationResult = Optional.of("Seleccione una fecha de inicio");
        } else {
            LocalDate today = LocalDate.now();
            
            if (startDate.isBefore(today)) {
                validationResult = Optional.of(fieldName + " no puede ser pasada");
            } else {
                validationResult = Optional.empty();
            }
        }
        return validationResult;
    }

    public static Optional<String> validateEndDate(LocalDate startDate, LocalDate endDate, String fieldName) {
        Optional<String> validationResult;
        if (endDate == null) {
            validationResult = Optional.of("Seleccione una fecha de finalización");
        } else if (startDate != null && endDate.isBefore(startDate)) {
            validationResult = Optional.of(fieldName + " no puede ser anterior a la fecha de inicio");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    public static Optional<String> validateProjectCapacity(int capacity, String fieldName) {
        Optional<String> validateResult;
        if(capacity > MAX_PROJECT_CAPACITY) {
            validateResult = Optional.of(fieldName + " no puede tener una capacidad mayor a " + MAX_PROJECT_CAPACITY 
                + " alumnos");
        } else {
            validateResult = Optional.empty();
        }
        return validateResult;
    }

    public static Optional<String> validateRecentStartDate(LocalDate startDate, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        if (startDate == null) {
            validationResult = Optional.of("Seleccione una fecha de inicio");
        } else {
            LocalDate today = LocalDate.now();
            LocalDate earliestAllowedDate = today.minusMonths(MAX_PAST_MONTHS);

            if (startDate.isAfter(today)) {
                validationResult = Optional.of(fieldName + " no puede ser futura");
            } else if (startDate.isBefore(earliestAllowedDate)) {
                validationResult = Optional.of(
                    fieldName + " no puede tener más de " + MAX_PAST_MONTHS + " meses de antigüedad");
            }
        }
        return validationResult;
    }
}
