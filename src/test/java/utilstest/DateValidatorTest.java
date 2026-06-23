package utilstest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import uv.lis.logic.dto.SchoolPeriod;
import uv.lis.logic.utils.DateValidator;

class DateValidatorTest {

    private static final String FIELD_NAME = "Campo";
    private static final String PERIOD_START = "2026-02-09";
    private static final String PERIOD_END = "2026-07-03";
    private static final String DATE_INSIDE = "2026-04-15";
    private static final String DATE_BEFORE = "2026-01-10";
    private static final String DATE_AFTER = "2026-08-01";

    @ParameterizedTest
    @CsvSource({
        "20, false",
        "10, true",
        "-1, true"
    })
    void validateBirthDate_variousAges_returnsExpectedResult(int yearsAgo, boolean hasError) {
        LocalDate birthDate = LocalDate.now().minusYears(yearsAgo);

        boolean result = DateValidator.validateBirthDate(birthDate, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "5, false",
        "-5, true"
    })
    void validateStartDate_variousDates_returnsExpectedResult(int daysFromNow, boolean hasError) {
        LocalDate startDate = LocalDate.now().plusDays(daysFromNow);

        boolean result = DateValidator.validateStartDate(startDate, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @ParameterizedTest
    @CsvSource({
        "10, false",
        "-10, true"
    })
    void validateEndDate_variousDates_returnsExpectedResult(int daysAfterStart, boolean hasError) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAfterStart);

        boolean result = DateValidator.validateEndDate(startDate, endDate, FIELD_NAME).isPresent();

        assertEquals(hasError, result);
    }

    @Test
    void validateDateWithinPeriod_dateInsidePeriod_returnsEmpty() {
        LocalDate date = LocalDate.parse(DATE_INSIDE);

        boolean result = DateValidator.validateDateWithinPeriod(date, builderPeriod(), FIELD_NAME).isPresent();

        assertFalse(result);
    }

    @Test
    void validateDateWithinPeriod_dateBeforePeriod_returnsError() {
        LocalDate date = LocalDate.parse(DATE_BEFORE);

        boolean result = DateValidator.validateDateWithinPeriod(date, builderPeriod(), FIELD_NAME).isPresent();

        assertTrue(result);
    }

    @Test
    void validateDateWithinPeriod_dateAfterPeriod_returnsError() {
        LocalDate date = LocalDate.parse(DATE_AFTER);

        boolean result = DateValidator.validateDateWithinPeriod(date, builderPeriod(), FIELD_NAME).isPresent();

        assertTrue(result);
    }

    @Test
    void validateDateWithinPeriod_nullPeriod_returnsEmpty() {
        LocalDate date = LocalDate.parse(DATE_INSIDE);

        boolean result = DateValidator.validateDateWithinPeriod(date, null, FIELD_NAME).isPresent();

        assertFalse(result);
    }

    private SchoolPeriod builderPeriod() {
        SchoolPeriod period = new SchoolPeriod();
        period.setStartDate(Date.valueOf(PERIOD_START));
        period.setEndDate(Date.valueOf(PERIOD_END));
        return period;
    }
}