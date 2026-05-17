package uv.lis.logic.contracts;

import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;

public interface IActivityDAO {
    List<Activity> getAllActivities() throws OperationException;

    Optional<Activity> getActivityById(int idActivity) throws OperationException;

    boolean registerActivity(Activity activity) throws OperationException;

    boolean modifyActivity(Activity activity) throws OperationException;
}
