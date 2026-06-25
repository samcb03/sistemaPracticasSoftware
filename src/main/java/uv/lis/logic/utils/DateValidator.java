package uv.lis.logic.utils;

import static uv.lis.logic.utils.InputValidator.PERIOD_TERM_FALL;
import static uv.lis.logic.utils.InputValidator.PERIOD_TERM_SPRING;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Optional;

import uv.lis.logic.dto.SchoolPeriod;

/**
 * Provides reusable validation rules for date fields across the application.
 *
 * Each method returns an Optional that is empty when the value is valid,
 * or contains a user-facing error message when it is not, so callers
 * can decide how to present the result without coupling validation logic to the UI.
 */
public final class DateValidator {

    private static final int MINIMUM_AGE = 18;
    private static final int MAXIMUM_AGE = 70;
    private static final int MAX_PAST_MONTHS = 6;
    private static final int FALL_TERM_YEAR_OFFSET = 1;

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
            } else if(age > MAXIMUM_AGE) {
                validationResult = Optional.of("El alumno no puede ser mayor de " + MAXIMUM_AGE + " años");
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
     * Verifies that a date falls within the date range of the given school period.
     *
     * @param date the date to evaluate
     *
     * @param period the school period that defines the allowed range
     *
     * @param fieldName the field label used in the error message
     *
     * @return an error message if the date is outside the period, empty otherwise
     */
    public static Optional<String> validateDateWithinPeriod(LocalDate date, SchoolPeriod period, String fieldName) {
        Optional<String> validationResult = Optional.empty();
        boolean canValidate = date != null && period != null
            && period.getStartDate() != null && period.getEndDate() != null;
        if (canValidate) {
            LocalDate periodStart = period.getStartDate().toLocalDate();
            LocalDate periodEnd = period.getEndDate().toLocalDate();
            if (date.isBefore(periodStart) || date.isAfter(periodEnd)) {
                validationResult = Optional.of(fieldName + " debe estar dentro del periodo escolar");
            }
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

    private DateValidator() {
        
    }
}