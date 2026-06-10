package uv.lis.logic.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import uv.lis.logic.dto.Activity;

public final class WorkProgressCalculator {

    private static final int TOTAL_PROGRESS_PERCENTAGE = 100;
    private static final int MINIMUM_WEEKS = 1;
    private static final int ZERO_PROGRESS = 0;
    private static final double DAYS_PER_WEEK = 7.0;

    private WorkProgressCalculator() {

    }

    public static int calculateWeeklyPlannedAdvance(Activity activity) {
        int weeklyAdvance = ZERO_PROGRESS;

        if (hasValidSchedule(activity)) {
            int totalWeeks = calculateActivityWeeks(activity);
            weeklyAdvance = TOTAL_PROGRESS_PERCENTAGE / totalWeeks;
        }
        return weeklyAdvance;
    }

    public static int calculateWeeklyRealAdvance(int writtenRealAdvance, Activity activity) {
        int activityWeeks = calculateActivityWeeks(activity);
        int advancePerWeek = writtenRealAdvance / activityWeeks;
        return advancePerWeek;
    }

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

    private static boolean hasValidSchedule(Activity activity) {
        boolean isEqual = false;
        isEqual = activity != null
            && activity.getStartDate() != null
            && activity.getEndDate() != null
            && !activity.getEndDate().isBefore(activity.getStartDate());
        return isEqual;
    }
}