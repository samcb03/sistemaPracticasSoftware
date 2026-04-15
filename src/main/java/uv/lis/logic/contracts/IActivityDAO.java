package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.Activity;
import uv.lis.logic.exceptions.OperationException;


public interface IActivityDAO {
    List<Activity> getActivities() throws SQLException, OperationException;

    List<Activity> getActivitiesById(int idActivity) throws SQLException, OperationException;

    boolean registerActivity(Activity activity) throws SQLException, OperationException;

    boolean modifyActivity(Activity activity) throws SQLException, OperationException;
}
