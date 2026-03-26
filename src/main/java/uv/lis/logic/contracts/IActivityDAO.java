package uv.lis.logic.contracts;


import java.util.List;

import uv.lis.logic.dto.Activity;

public interface IActivityDAO {

    List<Activity> getActivities ();

    List<Activity> getActivitiesById(int idActivity);

    boolean registerActivity(Activity activity);

    boolean modifyActivity(Activity activity);
}
