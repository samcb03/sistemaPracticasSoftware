package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

public interface IReportContextDAO {

    Report getReportContextByStudentId(String studentId) throws OperationException;

    String getTotalReportedHoursByStudentId(String studentId) throws OperationException;

    MonthlyReport getMonthlyReportData(String studentId) throws OperationException;

    List<Activity> getRecordedActivities(String studentId) throws OperationException;

    boolean registerMonthlyReport(MonthlyReport monthlyReport) throws OperationException;
}