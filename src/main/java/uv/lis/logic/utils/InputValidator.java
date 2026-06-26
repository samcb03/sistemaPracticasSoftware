package uv.lis.logic.utils;

import java.math.BigInteger;
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
    public static final int IS_COORDINATOR = 3;
    public static final int NO_VALUE = 0;
    public static final int MAX_GRADE = 10;
    public static final int MAX_REQUESTS = 3;
    public static final int STATUS_REQUESTED = 1;
    public static final int STATUS_ASSIGNED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int MAX_TEXT_LENGTH = 255;
    public static final int STUDENT_ID_LENGTH = 9;
    public static final int PROFESSOR_ID_LENGTH = 5;
    public static final int MIN_POSITIVE_INTEGER = 1;
    public static final int INVALID_ID = -1;
    private static final int MAX_PROJECT_CAPACITY = 2;
    public static final int POSTAL_CODE_LENGTH = 5;
    public static final int MAX_HOURS_PER_PARTIAL_REPORT = 210;
    public static final int MAX_HOURS_PER_DAY = 8;
    public static final int MAX_PERCENTAGE = 100;
    public static final String PERIOD_TERM_FALL = "01";
    public static final String PERIOD_TERM_SPRING = "51";
    public static final String LETTERS_ONLY_REGEX = "^[\\p{L}\\s]+$";
    public static final String LEADING_SPACE_REGEX = "^\\s.*";
    public static final String TRAILING_SPACE_REGEX = ".*\\s$";
    public static final String CONSECUTIVE_SPACES_REGEX = ".*\\s{2,}.*";
    public static final String ONLY_NUMBERS_REGEX = "\\d+";
    private static final String INTEGER_REGEX = "^-?\\d+$";
    public static final String EMAIL_REGEX
        = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^[0-9]{7,15}$";
    public static final String PASSWORD_REGEX
        = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\"|,.<>/?-]).{8,}$";
    public static final String REPEAT_CHARACTERS_REGEX = "(?i)^.*([\\p{L}\\d])\\1{2,}.*$";
    public static final String STUDENT_ENROLLMENT = "^[Ss]\\d{8}$";
    public static final String REGISTER_REGEX = "^[\\p{L}0-9\\s.,-]+$";
    public static final String ADDRESS_NUMBER_REGEX = "^[a-zA-Z0-9\\s/#-]+$";
    public static final String REDACTER_REGEX = "^[\\p{L}\\s.,:!?()]+$";
    private static final String INVALID_ENDING_REGEX = ".*[^\\p{L}.]$";

    private InputValidator() {
        // Utility class, instantiation is not allowed
    }

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
     * Verifies that a field does not contain the same character repeated three or more times in a row.
     *
     * @param fieldValue the value to evaluate
     * 
     * @param fieldName  the field label used in the error message
     * 
     * @return an error message if the pattern is found, empty otherwise
     */
    public static Optional<String> validateNoConsecutiveRepeatedCharacters(String fieldValue, String fieldName) {
        Optional<String> validationResult;
        if (fieldValue.matches(REPEAT_CHARACTERS_REGEX)) {
            validationResult = Optional.of(fieldName + " no puede contener el mismo carácter repetido 3 veces o más");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Returns the first validation error found among the given results, so the
     * caller can present one clear message at a time. Centralizes the stream
     * traversal that was previously duplicated across several validators.
     *
     * @param validationResults the validation results to evaluate in order
     * 
     * @return the first non-empty result, or empty if every result is valid
     */
    @SafeVarargs
    private static Optional<String> firstError(Optional<String>... validationResults) {
        Optional<String> firstError = Stream.of(validationResults)
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty());
        return firstError;
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
        Optional<String> error = firstError(
            validateNotEmpty(name, fieldName),
            validateMaxLength(name, MAX_TEXT_LENGTH, fieldName),
            validateLettersOnly(name, fieldName),
            validateNoLeadingSpace(name, fieldName),
            validateNoTrailingSpace(name, fieldName),
            validateNoConsecutiveSpaces(name, fieldName),
            validateNoConsecutiveRepeatedCharacters(name, fieldName));
        return error;
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
     * Verifies that a value represents a valid progress percentage between 1 and 100,
     * so that a final report cannot store an advance greater than full completion.
     *
     * @param percentageValue the value to evaluate
     * 
     * @param fieldName the field label used in the error message
     * 
     * @return an error message if the value is empty, not an integer or above 100, empty otherwise
     */
    public static Optional<String> validatePercentage(String percentageValue, String fieldName) {
        Optional<String> validationResult = validatePositiveInteger(percentageValue, fieldName);
        if (validationResult.isEmpty() && exceedsMaximum(percentageValue, MAX_PERCENTAGE)) {
            validationResult = Optional.of(fieldName + " no puede ser mayor a " + MAX_PERCENTAGE);
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
        if (!integerValue.matches(INTEGER_REGEX)) {
            validationResult = Optional.of(fieldName + " debe ser un número entero válido");
        } else if (isBelowMinimum(integerValue, MIN_POSITIVE_INTEGER)) {
            validationResult = Optional.of(fieldName + " debe ser un número positivo");
        } else {
            validationResult = Optional.empty();
        }
        return validationResult;
    }

    /**
     * Verifies whether a numeric string is strictly below a minimum, using BigInteger so that
     * values outside the int range are compared correctly instead of overflowing.
     *
     * @param integerValue the numeric string to compare, already validated as an integer
     * 
     * @param minimum the minimum value allowed
     * 
     * @return true if the value is below the minimum, false otherwise
     */
    private static boolean isBelowMinimum(String integerValue, int minimum) {
        boolean isBelowMinimum = new BigInteger(integerValue).compareTo(BigInteger.valueOf(minimum)) < NO_VALUE;
        return isBelowMinimum;
    }

    /**
     * Verifies whether a numeric string is strictly above a maximum, using BigInteger so that
     * values outside the int range are compared correctly instead of overflowing.
     *
     * @param integerValue the numeric string to compare, already validated as an integer
     * 
     * @param maximum the maximum value allowed
     * 
     * @return true if the value is above the maximum, false otherwise
     */
    private static boolean exceedsMaximum(String integerValue, int maximum) {
        boolean isExceeded = new BigInteger(integerValue).compareTo(BigInteger.valueOf(maximum)) > NO_VALUE;
        return isExceeded;
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
        validationResult = validateIntegerRange(integerValue, fieldName);
        if (validationResult.isEmpty()) {
            if (exceedsMaximum(integerValue, maxInt)) {
                validationResult = Optional.of(fieldName + " no puede tener un valor mayor a " + maxInt);
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
            validationResult = Optional.of(fieldName + " debe tener exactamente " + requiredLenght + " caracteres");
        } else if (!fieldValue.matches(STUDENT_ENROLLMENT)) {
            validationResult = Optional.of(fieldName + " no sigue el formato establecido ");
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
     * Verifies that a postal code is not empty and contains exactly the required number of digits.
     *
     * @param postalCodeValue the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the value is empty or does not match the required length, empty otherwise
     */
    public static Optional<String> validatePostalCode(String postalCodeValue, String fieldName) {
        Optional<String> firstError = firstError(
            validateNotEmpty(postalCodeValue, fieldName),
            validatePositiveInteger(postalCodeValue, fieldName),
            validateExactLength(postalCodeValue, POSTAL_CODE_LENGTH, fieldName));
        return firstError;
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
        Optional<String> validationResult;
        if (!registerValue.matches(REGISTER_REGEX)) {
            validationResult = Optional.of(fieldName + " no acepta carácteres especiales");
        } else {
            validationResult = firstError(
                validateNotEmpty(registerValue, fieldName),
                validateMaxLength(registerValue, MAX_TEXT_LENGTH, fieldName),
                validateNoTrailingSpace(registerValue, fieldName),
                validateNoLeadingSpace(registerValue, fieldName),
                validateNoConsecutiveRepeatedCharacters(registerValue, fieldName),
                validateNoConsecutiveSpaces(registerValue, fieldName));
        }
        return validationResult;
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
        Optional<String> validationResult;
        if (!addressNumber.matches(ADDRESS_NUMBER_REGEX)) {
            validationResult = Optional.of(fieldName + " no acepta carácteres especial");
        } else {
            validationResult = firstError(
                validateNotEmpty(addressNumber, fieldName),
                validateNoConsecutiveSpaces(addressNumber, fieldName),
                validateNoTrailingSpace(addressNumber, fieldName),
                validateNoLeadingSpace(addressNumber, fieldName));
        }
        return validationResult;
    }

    /**
     * Verifies that the reported hours do not exceed the maximum allowed for the
     * activity's duration, calculated as the number of days the activity spans
     * multiplied by the daily hour limit.
     *
     * @param hoursValue the reported hours to evaluate
     *
     * @param durationInDays the number of days the activity spans (0 if not yet known)
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the hours exceed the allowed maximum for the duration, empty otherwise
     */
    public static Optional<String> validateMaxHoursForDuration(String hoursValue, long durationInDays, 
        String fieldName) {
        Optional<String> validationResult;
        if (durationInDays <= NO_VALUE) {
            validationResult = Optional.empty();
        } else {
            int maxAllowedHours =(int) durationInDays * MAX_HOURS_PER_DAY;
            validationResult = validateMaxIntValue(hoursValue, maxAllowedHours, fieldName);
        }
        return validationResult;
    }

    /**
     * Verifies that a descriptive text field is not empty, does not exceed the character limit,
     * contains only letters, spaces, and common punctuation marks used in writing, and ends
     * with a letter or a period.
     *
     * @param fieldValue the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return the first error found, or empty if all rules pass
     */
    public static Optional<String> validateDescriptiveText(String fieldValue, String fieldName) {
        Optional<String> validationResult;
        validationResult = validateNotEmpty(fieldValue, fieldName);
        if (validationResult.isEmpty()) {
            if (!fieldValue.matches(REDACTER_REGEX)) {
                validationResult = Optional.of(fieldName + " contiene caracteres no permitidos");
            } else if (fieldValue.matches(INVALID_ENDING_REGEX)) {
                validationResult = Optional.of(fieldName + " debe terminar con una letra o un punto");
            } else {
                validationResult = firstError(
                    validateMaxLength(fieldValue, MAX_TEXT_LENGTH, fieldName),
                    validateNoLeadingSpace(fieldValue, fieldName),
                    validateNoTrailingSpace(fieldValue, fieldName),
                    validateNoConsecutiveSpaces(fieldValue, fieldName),
                    validateNoConsecutiveRepeatedCharacters(fieldValue, fieldName)
                );
            }
        }
        return validationResult;
    }

    /**
     * Verifies that a grade value is a valid integer between 0 and 10 inclusive.
     *
     * @param gradeValue the value to evaluate
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the value is empty, not an integer, or out of range, empty otherwise
     */
    public static Optional<String> validateGrade(String gradeValue, String fieldName) {
        Optional<String> validationResult = validateNotEmpty(gradeValue, fieldName);

        if (validationResult.isEmpty()) {
            BigInteger value = new BigInteger(gradeValue);

            if (value.compareTo(BigInteger.valueOf(NO_VALUE)) < NO_VALUE) {
                validationResult = Optional.of(fieldName + " no puede ser menor a " + NO_VALUE);
            } else if (value.compareTo(BigInteger.valueOf(MAX_GRADE)) > NO_VALUE) {
                validationResult = Optional.of(fieldName + " no puede ser mayor a " + MAX_GRADE);
            } else {
                validationResult = Optional.empty();
            }
        }
        return validationResult;
    }
}