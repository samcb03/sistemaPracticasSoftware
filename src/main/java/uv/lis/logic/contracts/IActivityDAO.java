package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Activity;


public interface IActivityDAO {
    List<Activity> getActivities() throws SQLException;

    List<Activity> getActivitiesById(int idActivity) throws SQLException;

    boolean registerActivity(Activity activity) throws SQLException;

    boolean modifyActivity(Activity activity) throws SQLException;
}
