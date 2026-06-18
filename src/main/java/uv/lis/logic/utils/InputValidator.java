package uv.lis.logic.utils;


import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides reusable validation rules for user input fields across the application.
 *
 * Each method returns an Optional that is empty when the value is valid,
 * or contains a user-facing error message when it is not, so callers
 * can decide how to present the result without coupling validation logic to the UI.
 */
public final class InputValidator {

    private InputValidator() {}

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
    private static final int FALL_TERM_YEAR_OFFSET = 1;
    public static final int POSTAL_CODE_LENGTH = 5;
    public static final String PERIOD_TERM_FALL = "01";
    public static final String PERIOD_TERM_SPRING = "51";
    public static final String LETTERS_ONLY_REGEX = "^[\\p{L}\\s]+$";
    public static final String LEADING_SPACE_REGEX = "^\\s.*";
    public static final String TRAILING_SPACE_REGEX = ".*\\s$";
    public static final String CONSECUTIVE_SPACES_REGEX = ".*\\s{2,}.*";
    public static final String ONLY_NUMBERS_REGEX = "\\d+";
    public static final String EMAIL_REGEX
        = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^[0-9]{7,15}$";
    public static final String PASSWORD_REGEX
        = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"|,.<>/?-]).{8,}$";
    public static final String REPEAT_LETTERS_REGEX = "(?i)^.*([\\p{L}])\\1{2,}.*$";
    public static final String STUDENT_ENROLLMENT = "^[Ss]\\d{8}$";
    public static final String REGISTER_REGEX = "^[\\p{L}0-9\\s.,-]+$";
    public static final String ADDRESS_NUMBER_REGEX = "^[a-zA-Z0-9\\s/#-]+$";

    /**
     * Verifies that a field is not null or blank.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value is empty, empty otherwise
     */
    public static Optional<String> validateNotEmpty(String fieldValue, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that a field does not exceed the allowed character limit.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param maxLength  the maximum number of characters allowed
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value exceeds the limit, empty otherwise
     */
    public static Optional<String> validateMaxLength(String fieldValue, int maxLength, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.length() > maxLength) {
            validationResult = Optional.of(fieldName + " no puede exceder los " + maxLength + " caracteres");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that a field contains only letters and spaces.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value contains other characters, empty otherwise
     */
    public static Optional<String> validateLettersOnly(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        if (!fieldValue.matches(LETTERS_ONLY_REGEX)) {
            validationResult = Optional.of(fieldName + " solo acepta letras y espacios");
        }
        return validationResult;
    }

    /**
     * Verifies that a field does not start with a space.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value starts with a space, empty otherwise
     */
    public static Optional<String> validateNoLeadingSpace(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        if (fieldValue.matches(LEADING_SPACE_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede comenzar con un espacio");
        }
        return validationResult;
    }

    /**
     * Verifies that a field does not end with a space.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value ends with a space, empty otherwise
     */
    public static Optional<String> validateNoTrailingSpace(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        if (fieldValue.matches(TRAILING_SPACE_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede terminar con un espacio");
        }
        return validationResult;
    }

    /**
     * Verifies that a field does not contain two or more consecutive spaces.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if consecutive spaces are found, empty otherwise
     */
    public static Optional<String> validateNoConsecutiveSpaces(String fieldValue, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        if (fieldValue.matches(CONSECUTIVE_SPACES_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener espacios consecutivos");
        }
        return validationResult;
    }

    /**
     * Verifies that a field does not contain the same letter repeated three or more times in a row.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the pattern is found, empty otherwise
     */
    public static Optional<String> validateNoConsecutiveRepeatedLetters(String fieldValue, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.matches(REPEAT_LETTERS_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener la misma letra repetida 3 veces o más");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that a text field passes all standard rules for names and descriptive fields,
     * returning the first violation found so the user receives one clear message at a time.
     *
     * @param name the value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return the first error found, or empty if all rules pass
     */
    public static Optional<String> validateText(String name, String fieldName) {
        return Stream.of(
                InputValidator.validateNotEmpty(name, fieldName),
                InputValidator.validateLettersOnly(name, fieldName),
                InputValidator.validateNoLeadingSpace(name, fieldName),
                InputValidator.validateNoTrailingSpace(name, fieldName),
                InputValidator.validateNoConsecutiveSpaces(name, fieldName),
                InputValidator.validateNoConsecutiveRepeatedLetters(name, fieldName))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
    }

    /**
     * Verifies that a field contains a well-formed email address.
     *
     * @param emailValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value is empty or has an invalid format, empty otherwise
     */
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

    /**
     * Verifies that a field contains a valid phone number.
     *
     * @param phoneValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the value is empty or has an invalid format, empty otherwise
     */
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

    /**
     * Verifies that a password is not empty and meets the required security criteria.
     *
     * @param passwordValue the value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the value is empty or does not meet the criteria, empty otherwise
     */
    public static Optional<String> validatePassword(String passwordValue, String fieldName) {
        validateNoTrailingSpace(passwordValue, fieldName);
        validateNoConsecutiveSpaces(passwordValue, fieldName);
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

    /**
     * Verifies that a field contains a positive integer.
     *
     * @param integerValue the value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the value is empty or not a positive integer, empty otherwise
     */
    public static Optional<String> validatePositiveInteger(String integerValue, String fieldName) {
        Optional<String> validationResult;
        if (integerValue.isEmpty()) {
            validationResult = Optional.of(fieldName + " no puede estar vacío");
        } else {
            validationResult = validateIntegerRange(integerValue, fieldName);
        }
        return validationResult;
    }

    /**
     * Verifies that a string value represents a valid integer within the allowed range,
     * so that non-numeric input is rejected before it reaches any business logic.
     *
     * @param integerValue the value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the value is not a valid integer or is below the minimum, empty otherwise
     */
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

    /**
     * Verifies that a field has exactly the required number of characters.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param requiredLength the exact number of characters required
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the length does not match, empty otherwise
     */
    public static Optional<String> validateExactLength(String fieldValue, int requiredLength, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.isEmpty() || fieldValue.length() != requiredLength) {
            validationResult = Optional.of(fieldName + " debe tener exactamente " + requiredLength + " caracteres");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that an integer field does not exceed a given maximum value.
     *
     * @param integerValue the value to evaluate
     * 
     * @param maxInt the maximum value allowed
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the value exceeds the maximum, empty otherwise
     */
    public static Optional<String> validateMaxIntValue(String integerValue, int maxInt, String fieldName) {
        Optional<String> validationResult = validateNotEmpty(integerValue, fieldName);
        if (validationResult.isEmpty()) {
            try {
                int parsedNumber = Integer.parseInt(integerValue);
                if (parsedNumber > maxInt) {
                    validationResult = Optional.of(fieldName + " no puede tener un valor mayor a " + maxInt);
                }
            } catch (NumberFormatException numberFormatException) {
                validationResult = Optional.of(fieldName + " debe ser un número entero válido");
            }
        }
        return validationResult;
    }

    /**
     * Verifies that a student ID is not empty, has the required length,
     * and follows the established enrollment format.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param requiredLenght the exact number of characters required
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if any rule is violated, empty otherwise
     */
    public static Optional<String> validateIdStudent(String fieldValue, int requiredLenght, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.isEmpty() || fieldValue.length() != requiredLenght) {
            validationResult = Optional.of(fieldName + "debe tener exactamente " + requiredLenght + " caracteres");
        } else if (!fieldValue.matches(STUDENT_ENROLLMENT)) {
            validationResult = Optional.of(fieldName + " no sigue el formato establecido empezando con una S ");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that a combo box has a selected value.
     *
     * @param selectedValue the selected item to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if no item is selected, empty otherwise
     */
    public static Optional<String> validateComboBox(Object selectedValue, String fieldName) {
        Optional<String> validationResult;
        if (selectedValue == null) {
            validationResult = Optional.of("Seleccione " + fieldName);
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that a birth date is not null, is not in the future,
     * and corresponds to a person of at least 18 years of age.
     *
     * @param birthDate the date to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if any rule is violated, empty otherwise
     */
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

    /**
     * Verifies that a start date is not null and is not in the past.
     *
     * @param startDate the date to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the date is null or in the past, empty otherwise
     */
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

    /**
     * Verifies that an end date is not null and is not before the start date.
     *
     * @param startDate the reference start date
     * 
     * @param endDate the date to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the end date is null or precedes the start date, empty otherwise
     */
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

    /**
     * Verifies that the start date matches the month required by the period term:
     * August for term 01 and February for term 51.
     *
     * @param term the selected period term (01 or 51)
     *
     * @param startDate the date to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the start month does not match the term, empty otherwise
     */
    public static Optional<String> validatePeriodStartDate(String term, LocalDate startDate, String fieldName) {
        Optional<String> validationResult;
        if (startDate == null) {
            validationResult = Optional.of("Seleccione una fecha de inicio");
        } else if (PERIOD_TERM_FALL.equals(term) && startDate.getMonth() != Month.AUGUST) {
            validationResult = Optional.of(fieldName + " debe iniciar en agosto para el periodo 01");
        } else if (PERIOD_TERM_SPRING.equals(term) && startDate.getMonth() != Month.FEBRUARY) {
            validationResult = Optional.of(fieldName + " debe iniciar en febrero para el periodo 51");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies that the end date matches the term: January of the following year for
     * term 01, and July of the same year for term 51.
     *
     * @param term the selected period term (01 or 51)
     *
     * @param startDate the reference start date
     *
     * @param endDate the date to evaluate
     *
     * @return an error message if the end date does not match the term, empty otherwise
     */
    public static Optional<String> validatePeriodEndDate(String term, LocalDate startDate, LocalDate endDate) {
        Optional<String> validationResult;
        if (endDate == null) {
            validationResult = Optional.of("Seleccione una fecha de finalización");
        } else if (startDate == null) {
            validationResult = Optional.of("Seleccione primero la fecha de inicio");
        } else if (PERIOD_TERM_FALL.equals(term) && !isFallTermEnd(startDate, endDate)) {
            validationResult = Optional.of("El periodo 01 debe terminar en enero del año siguiente");
        } else if (PERIOD_TERM_SPRING.equals(term) && !isSpringTermEnd(startDate, endDate)) {
            validationResult = Optional.of("El periodo 51 debe terminar en julio del mismo año");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    private static boolean isFallTermEnd(LocalDate startDate, LocalDate endDate) {
        boolean isValidEnd = endDate.getMonth() == Month.JANUARY
            && endDate.getYear() == startDate.getYear() + FALL_TERM_YEAR_OFFSET;
        return isValidEnd;
    }

    private static boolean isSpringTermEnd(LocalDate startDate, LocalDate endDate) {
        boolean isValidEnd = endDate.getMonth() == Month.JULY
            && endDate.getYear() == startDate.getYear();
        return isValidEnd;
    }

    /**
     * Verifies that a project does not exceed the maximum number of students allowed.
     *
     * @param capacity the capacity value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the capacity exceeds the allowed maximum, empty otherwise
     */
    public static Optional<String> validateProjectCapacity(int capacity, String fieldName) {
        Optional<String> validateResult;
        if (capacity > MAX_PROJECT_CAPACITY) {
            validateResult = Optional.of(fieldName + " no puede tener una capacidad mayor a " + MAX_PROJECT_CAPACITY
                + " alumnos");
        } else {
            validateResult = Optional.empty();
        }
        return validateResult;
    }

    /**
     * Verifies that a start date is not null, is not in the future,
     * and is not older than 6 months from today.
     *
     * @param startDate the date to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if any rule is violated, empty otherwise
     */
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

    /**
     * Verifies that a postal code is not empty and contains exactly the required number of digits.
     *
     * @param postalCodeValue the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the value is empty or does not match the required length, empty otherwise
     */
    public static Optional<String> validatePostalCode(String postalCodeValue, String fieldName) {
        return Stream.of(
                InputValidator.validateNotEmpty(postalCodeValue, fieldName),
                InputValidator.validatePositiveInteger(postalCodeValue, fieldName),
                InputValidator.validateExactLength(postalCodeValue, POSTAL_CODE_LENGTH, fieldName))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
    }

    /**
     * Verifies that a registration field is not empty, contains only letters,
     * digits, spaces, periods, commas, and hyphens, and does not contain
     * leading, trailing, or consecutive spaces, or repeated letters.
     *
     * @param registerValue the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return the first error found, or empty if all rules pass
     */
    public static Optional<String> validateRegister(String registerValue, String fieldName) {
        Optional<String> validateRegister = Optional.empty();
        if(!registerValue.matches(REGISTER_REGEX)) {
            validateRegister = Optional.of(fieldName + " no acepta carácteres especiales");
        } else {
            return Stream.of(
                InputValidator.validateNotEmpty(registerValue, fieldName),
                InputValidator.validateNoConsecutiveRepeatedLetters(registerValue, fieldName),
                InputValidator.validateNoConsecutiveSpaces(registerValue, fieldName),
                InputValidator.validateNoTrailingSpace(registerValue, fieldName),
                InputValidator.validateNoLeadingSpace(registerValue, fieldName))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
        }
        return validateRegister;
    }

    /**
     * Verifies that an address number is not empty, contains only letters,
     * digits, spaces, slashes, hyphens, and pound signs, and does not contain
     * leading, trailing, or consecutive spaces.
     *
     * @param addressNumber the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return the first error found, or empty if all rules pass
     */

    public static Optional<String> validateAddressNumber(String addressNumber, String fieldName) {
        Optional<String> validateAddressNumber = Optional.empty();
        if(!addressNumber.matches(ADDRESS_NUMBER_REGEX)) {
            validateAddressNumber = Optional.of(fieldName + " no acepta carácteres especial");
        } else {
            return Stream.of(
                InputValidator.validateNotEmpty(addressNumber, fieldName),
                InputValidator.validateNoConsecutiveSpaces(addressNumber, fieldName),
                InputValidator.validateNoTrailingSpace(addressNumber, fieldName),
                InputValidator.validateNoLeadingSpace(addressNumber, fieldName))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
        }
        return validateAddressNumber;
    }
}