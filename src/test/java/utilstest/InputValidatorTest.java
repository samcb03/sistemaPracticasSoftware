package utilstest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import uv.lis.logic.utils.InputValidator;

class InputValidatorTest {

    private static final String FIELD_NAME = "Campo";
    private static final int MAX_LENGTH = 10;
    private static final int EXACT_LENGTH = 5;
    private static final int STUDENT_ID_LENGTH = 9;

    @ParameterizedTest
    @CsvSource({
        "Hola, false",
        "'', true",
        "'   ', true",
    })
    void validateNotEmpty_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateNotEmpty(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Corto, false",
        "EstaCadenaEsDemasiadoLarga, true"
    })
    void validateMaxLength_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateMaxLength(value, MAX_LENGTH, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan, false",
        "Juan Perez, false",
        "Juan123, true",
        "Juan@, true"
    })
    void validateLettersOnly_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateLettersOnly(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan, false",
        "' Juan', true"
    })
    void validateNoLeadingSpace_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateNoLeadingSpace(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan, false",
        "'Juan ', true"
    })
    void validateNoTrailingSpace_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateNoTrailingSpace(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan Perez, false",
        "'Juan  Perez', true"
    })
    void validateNoConsecutiveSpaces_variousInputs_returnsExpectedResult(String value,
            boolean hasError) {
        boolean result = InputValidator.validateNoConsecutiveSpaces(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan, false",
        "Aaaron, true"
    })
    void validateNoConsecutiveRepeatedLetters_variousInputs_returnsExpectedResult(String value,
            boolean hasError) {
        boolean result =
            InputValidator.validateNoConsecutiveRepeatedCharacters(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Juan Perez, false",
        "Juan123, true",
        "'Juan  Perez', true",
        "' Juan', true"
    })
    void validateText_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateText(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @Test
    void validateText_textExceedingMaxLength_returnsError() {
        String longText = "ab".repeat(InputValidator.MAX_TEXT_LENGTH);

        boolean result = InputValidator.validateText(longText, FIELD_NAME).isPresent();

        assertEquals(true, result);
    }

    @ParameterizedTest
    @CsvSource({
        "correo@dominio.com, false",
        "'', true",
        "correoInvalido, true",
        "correo@sin, true"
    })
    void validateEmail_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateEmail(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "2288421700, false",
        "'', true",
        "123, true",
        "abcdefgh, true"
    })
    void validatePhoneNumber_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validatePhoneNumber(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Gom_Ram002, false",
        "'', true",
        "sinmayuscula1!, true",
        "Corta1!, true"
    })
    void validatePassword_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validatePassword(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "5, false",
        "'', true",
        "0, true",
        "-3, true",
        "abc, true"
    })
    void validatePositiveInteger_variousInputs_returnsExpectedResult(String value,
            boolean hasError) {
        boolean result = InputValidator.validatePositiveInteger(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "1, false",
        "50, false",
        "100, false",
        "0, true",
        "101, true",
        "-5, true",
        "'', true",
        "abc, true"
    })
    void validatePercentage_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validatePercentage(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @Test
    void validatePercentage_integerLargerThanIntRange_returnsMaximumMessage() {
        String hugeInteger = "1234567890123456789";

        String message = InputValidator.validatePercentage(hugeInteger, FIELD_NAME).orElse("");

        assertEquals(FIELD_NAME + " no puede ser mayor a 100", message);
    }

    @ParameterizedTest
    @CsvSource({
        "12345, false",
        "'', true",
        "123, true"
    })
    void validateExactLength_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result =
            InputValidator.validateExactLength(value, EXACT_LENGTH, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "S23013127, false",
        "'', true",
        "S2301, true",
        "X23013127, true"
    })
    void validateIdStudent_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result =
            InputValidator.validateIdStudent(value, STUDENT_ID_LENGTH, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "seleccion, false",
        ", true"
    })
    void validateComboBox_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateComboBox(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }


    @ParameterizedTest
    @CsvSource({
        "Calle Hidalgo, false",
        "Av. 5 de Mayo, false",
        "Colonia Sur-Norte, false",
        "'', true",
        "' Calle', true",
        "'Calle ', true",
        "'Calle  Hidalgo', true",
        "Calle@#!, true"
    })
    void validateRegister_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateRegister(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "23, false",
        "23-B, false",
        "S/N, false",
        "12 A, false",
        "'', true",
        "' 23', true",
        "'23 ', true",
        "'23  B', true",
        "23@!, true"
    })
    void validateAddressNumber_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateAddressNumber(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "10, 0, false",
        "10, -3, false",
        "'', 5, true",
        "41, 5, true",
        "40, 5, false",
        "abc, 5, true",
        "99999999999, 5, true",
        "8, 1, false"
    })
    void validateMaxHoursForDuration_variousInputs_returnsExpectedResult(String hoursValue,
            long durationInDays, boolean hasError) {
        boolean result =
            InputValidator.validateMaxHoursForDuration(hoursValue, durationInDays, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "Actividad de desarrollo., false",
        "Revisión del módulo, false",
        "Análisis inicial, false",
        "Texto con signos: hola., false",
        "'', true",
        "Texto@invalido, true",
        "' Texto valido.', true",
        "'Texto valido. ', true",
        "'Texto  valido.', true",
        "Texto terminando con coma,', true",
        "Texto terminando con signo!, true",
        "Termina con numero3, true",
        "Texto terminando con hola., false"
    })
    void validateDescriptiveText_variousInputs_returnsExpectedResult(String value, boolean hasError) {
        boolean result = InputValidator.validateDescriptiveText(value, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @Test
    void validateDescriptiveText_textExceedingMaxLength_returnsError() {
        String longText = "ab".repeat(InputValidator.MAX_TEXT_LENGTH);

        boolean result = InputValidator.validateDescriptiveText(longText, FIELD_NAME).isPresent();

        assertEquals(true, result);
    }
}