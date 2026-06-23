package uv.lis.logic.contracts;

import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for student activities.
 */
public interface IActivityDAO {

    /**
     * Retrieves all the registered activities.
     *
     * @return the list of activities, empty if there are none
     * @throws OperationException if the activities cannot be retrieved
     */
    List<Activity> getAllActivities() throws OperationException;

    /**
     * Retrieves the activity identified by the given id.
     *
     * @param idActivity the identifier of the activity to retrieve
     * @return the activity if it exists, empty otherwise
     * @throws OperationException if the activity cannot be retrieved
     */
    Optional<Activity> getActivityById(int idActivity) throws OperationException;

    /**
     * Registers a new activity in the system.
     *
     * @param activity the activity data to register
     * @return true if the activity was registered, false otherwise
     * @throws OperationException if the activity cannot be registered
     */
    boolean registerActivity(Activity activity) throws OperationException;

    /**
     * Updates the data of an existing activity.
     *
     * @param activity the activity data to update
     * @return true if the activity was updated, false otherwise
     * @throws OperationException if the activity cannot be updated
     */
    boolean modifyActivity(Activity activity) throws OperationException;

    /**
     * Retrieves the activities that belong to a student.
     *
     * @param studentId the identifier of the student whose activities are retrieved
     * @return the list of activities for the student, empty if there are none
     * @throws OperationException if the activities cannot be retrieved
     */
    List<Activity> getActivitiesByStudentId(String studentId) throws OperationException;

    /**
     * Calculates the total hours reported across all the activities of a project.
     *
     * @param projectId the identifier of the project whose activity hours are summed
     * @return the total reported hours, zero if there are no activities
     * @throws OperationException if the total cannot be calculated
     */
    int getTotalActivityHoursByProject(int projectId) throws OperationException;
}