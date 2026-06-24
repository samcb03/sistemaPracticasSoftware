package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations that build the context needed to generate reports.
 */
public interface IReportContextDAO {

    /**
     * Retrieves the context required to generate a student's final report.
     *
     * @param studentId the identifier of the student whose context is retrieved
     * @return the final report context of the student
     * @throws OperationException if the context cannot be retrieved
     */
    FinalReport getFinalReportContextByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the context required to generate a student's partial report.
     *
     * @param studentId the identifier of the student whose context is retrieved
     * @return the partial report context of the student
     * @throws OperationException if the context cannot be retrieved
     */
    PartialReport getPartialReportContextByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the total reported hours of a student.
     *
     * @param studentId the identifier of the student to query
     * @return the total reported hours of the student
     * @throws OperationException if the reported hours cannot be retrieved
     */
    String getTotalReportedHoursByStudentId(String studentId) throws OperationException;

    /**
     * Retrieves the data required to generate a student's monthly report.
     *
     * @param studentId the identifier of the student whose data is retrieved
     * @return the monthly report data of the student
     * @throws OperationException if the data cannot be retrieved
     */
    MonthlyReport getMonthlyReportData(String studentId) throws OperationException;

    /**
     * Retrieves an activity of a student by its name.
     *
     * @param studentId the identifier of the student who owns the activity
     * @param activityName the name of the activity to retrieve
     * @return the activity that matches the given name
     * @throws OperationException if the activity cannot be retrieved
     */
    Activity getActivityByName(String studentId, String activityName) throws OperationException;

    /**
     * Retrieves the recorded activities of a student.
     *
     * @param studentId the identifier of the student whose activities are retrieved
     * @return the list of recorded activities, empty if there are none
     * @throws OperationException if the activities cannot be retrieved
     */
    List<Activity> getRecordedActivities(String studentId) throws OperationException;

    /**
     * Retrieves the recorded activities of a student for a given month and year.
     *
     * @param studentId the identifier of the student to query
     * @param month the month for which the activities are retrieved
     * @param year the year for which the activities are retrieved
     * @return the list of recorded activities, empty if there are none
     * @throws OperationException if the activities cannot be retrieved
     */
    List<Activity> getRecordedActivitiesByMonth(String studentId, int month, int year) throws OperationException;

    /**
     * Retrieves the sum of reported hours of a student for a given month and year.
     *
     * @param studentId the identifier of the student to query
     * @param month the month for which the hours are summed
     * @param year the year for which the hours are summed
     * @return the total reported hours for the period
     * @throws OperationException if the reported hours cannot be retrieved
     */
    int getSumOfReportedHours(String studentId, int month, int year) throws OperationException;

    /**
     * Indicates whether a student's report has already been generated for a month.
     *
     * @param studentId the identifier of the student to verify
     * @param month the month to check for an existing report
     * @return true if the report already exists, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasReportAlreadyBeenGenerated(String studentId, String month) throws OperationException;
}