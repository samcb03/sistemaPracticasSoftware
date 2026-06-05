package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.exceptions.OperationException;

public interface IReportContextDAO {

    FinalReport getFinalReportContextByStudentId(String studentId) throws OperationException;

    PartialReport getPartialReportContextByStudentId(String studentId) throws OperationException;

    String getTotalReportedHoursByStudentId(String studentId) throws OperationException;

    MonthlyReport getMonthlyReportData(String studentId) throws OperationException;

    Activity getActivityByName(String studentId, String activityName) throws OperationException;

    List<Activity> getRecordedActivities(String studentId) throws OperationException;

    List<Activity> getRecordedActivitiesByMonth(int idProyecto, int mes, int anio) throws OperationException;

    int getSumOfReportedHours(int projectId, int month, int year) throws OperationException;

    boolean hasReportAlreadyBeenGenerated(String studentId, String month) throws OperationException;
}