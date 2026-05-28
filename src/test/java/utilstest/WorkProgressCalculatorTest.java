package utilstest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.utils.WorkProgressCalculator;

class WorkProgressCalculatorTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 2, 9);
    private static final int MINIMUM_WEEKS = 1;
    private static final int ZERO_PROGRESS = 0;
    private static final int FOURTEEN_DAYS = 14;
    private static final int TWENTY_ONE_DAYS = 21;
    private static final int TWO_WEEKS = 2;
    private static final int THREE_WEEKS = 3;
    private static final int ADVANCE_TWO_WEEKS = 50;
    private static final int ADVANCE_THREE_WEEKS = 33;

    private Activity builderActivity(LocalDate startDate, LocalDate endDate) {
        Activity activity = new Activity();
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        return activity;
    }

    @ParameterizedTest
    @CsvSource({
        "14, 2",
        "21, 3"
    })
    void calculateActivityWeeks_validSchedule_returnsRoundedWeeks(int daysBetween,
            int expectedWeeks) {
        Activity activity = builderActivity(BASE_DATE, BASE_DATE.plusDays(daysBetween));

        int result = WorkProgressCalculator.calculateActivityWeeks(activity);

        assertEquals(expectedWeeks, result);
    }

    @Test
    void calculateActivityWeeks_shortSchedule_returnsMinimumWeeks() {
        Activity activity = builderActivity(BASE_DATE, BASE_DATE.plusDays(MINIMUM_WEEKS));

        int result = WorkProgressCalculator.calculateActivityWeeks(activity);

        assertEquals(MINIMUM_WEEKS, result);
    }

    @Test
    void calculateActivityWeeks_nullActivity_returnsMinimumWeeks() {
        int result = WorkProgressCalculator.calculateActivityWeeks(null);

        assertEquals(MINIMUM_WEEKS, result);
    }

    @Test
    void calculateActivityWeeks_nullDates_returnsMinimumWeeks() {
        Activity activity = builderActivity(null, null);

        int result = WorkProgressCalculator.calculateActivityWeeks(activity);

        assertEquals(MINIMUM_WEEKS, result);
    }

    @Test
    void calculateActivityWeeks_endBeforeStart_returnsMinimumWeeks() {
        Activity activity = builderActivity(BASE_DATE, BASE_DATE.minusDays(FOURTEEN_DAYS));

        int result = WorkProgressCalculator.calculateActivityWeeks(activity);

        assertEquals(MINIMUM_WEEKS, result);
    }

    @ParameterizedTest
    @CsvSource({
        "14, 50",
        "21, 33"
    })
    void calculateWeeklyPlannedAdvance_validSchedule_returnsAdvance(int daysBetween,
            int expectedAdvance) {
        Activity activity = builderActivity(BASE_DATE, BASE_DATE.plusDays(daysBetween));

        int result = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);

        assertEquals(expectedAdvance, result);
    }

    @Test
    void calculateWeeklyPlannedAdvance_nullActivity_returnsZeroProgress() {
        int result = WorkProgressCalculator.calculateWeeklyPlannedAdvance(null);

        assertEquals(ZERO_PROGRESS, result);
    }

    @Test
    void calculateWeeklyPlannedAdvance_endBeforeStart_returnsZeroProgress() {
        Activity activity = builderActivity(BASE_DATE, BASE_DATE.minusDays(FOURTEEN_DAYS));

        int result = WorkProgressCalculator.calculateWeeklyPlannedAdvance(activity);

        assertEquals(ZERO_PROGRESS, result);
    }
}