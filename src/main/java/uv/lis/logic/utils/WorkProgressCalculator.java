package uv.lis.logic.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import uv.lis.logic.dto.Activity;

/**
 * Calculates progress indicators for activities based on their scheduled dates,
 * so that controllers and views do not need to handle date arithmetic directly.
 */
public final class WorkProgressCalculator {

    private static final int TOTAL_PROGRESS_PERCENTAGE = 100;
    private static final int MINIMUM_WEEKS = 1;
    private static final int ZERO_PROGRESS = 0;
    private static final int NO_WEEK_OFFSET = 0;
    private static final double DAYS_PER_WEEK = 7.0;

    private WorkProgressCalculator() {}

    /**
     * Returns how much progress the activity should accumulate per week
     * to stay on track with its planned schedule.
     *
     * @param activity the activity to evaluate
     * 
     * @return the expected weekly progress percentage,
     *         or zero if the activity has no valid schedule
     */
    public static int calculateWeeklyPlannedAdvance(Activity activity) {
        int weeklyAdvance = ZERO_PROGRESS;
        if (hasValidSchedule(activity)) {
            int totalWeeks = calculateActivityWeeks(activity);
            weeklyAdvance = TOTAL_PROGRESS_PERCENTAGE / totalWeeks;
        }
        return weeklyAdvance;
    }

    /**
     * Returns the portion of real advance that corresponds to each week,
     * so it can be compared against the planned weekly progress.
     *
     * @param writtenRealAdvance the total real advance reported for the activity
     * 
     * @param activity the activity whose weekly contribution is calculated
     * 
     * @return the real advance per week
     */
    public static int calculateWeeklyRealAdvance(int writtenRealAdvance, Activity activity) {
        int activityWeeks = calculateActivityWeeks(activity);
        return writtenRealAdvance / activityWeeks;
    }

    /**
     * Returns the duration of an activity expressed in weeks.
     *
     * @param activity the activity to evaluate
     * 
     * @return the number of weeks the activity spans,
     *         or one if the schedule is missing or shorter than a week
     */
    public static int calculateActivityWeeks(Activity activity) {
        int weeks = MINIMUM_WEEKS;
        if (hasValidSchedule(activity)) {
            LocalDate startDate = activity.getStartDate();
            LocalDate endDate = activity.getEndDate();
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            long roundedWeeks = (long) Math.ceil(daysBetween / DAYS_PER_WEEK);
            if (roundedWeeks > MINIMUM_WEEKS) {
                weeks = (int) roundedWeeks;
            }
        }
        return weeks;
    }

    /**
     * Returns how many weeks after the report period start an activity begins,
     * so it can be placed in its matching week column instead of always week one.
     *
     * @param activity the activity to evaluate
     * 
     * @param periodStart the start date of the report period (earliest activity start)
     * 
     * @return the zero-based week offset where the activity starts,
     *         or zero if it begins on or before the period start
     */
    public static int calculateStartWeekOffset(Activity activity, LocalDate periodStart) {
        int weekOffset = NO_WEEK_OFFSET;
        if (hasValidSchedule(activity) && periodStart != null) {
            LocalDate startDate = activity.getStartDate();
            if (startDate.isAfter(periodStart)) {
                long daysFromStart = ChronoUnit.DAYS.between(periodStart, startDate);
                weekOffset = (int) Math.floor(daysFromStart / DAYS_PER_WEEK);
            }
        }
        return weekOffset;
    }

    /**
     * Confirms that an activity has enough scheduling information
     * to produce meaningful progress calculations.
     *
     * @param activity the activity to evaluate
     * 
     * @return true if start and end dates are present and consistent,
     *         false otherwise
     */
    private static boolean hasValidSchedule(Activity activity) {
        return activity != null
            && activity.getStartDate() != null
            && activity.getEndDate() != null
            && !activity.getEndDate().isBefore(activity.getStartDate());
    }
}