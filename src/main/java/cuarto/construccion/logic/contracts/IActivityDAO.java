package cuarto.construccion.logic.contracts;


import java.util.List;

import cuarto.construccion.logic.dto.Activity;

public interface IActivityDAO {

    List<Activity> getActivities ();

    List<Activity> getActivitiesById(int idActivity);

    boolean registerActivity(Activity activity);

    boolean modifyActivity(Activity activity);
}
