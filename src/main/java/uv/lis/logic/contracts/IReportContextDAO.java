package uv.lis.logic.contracts;

import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

public interface IReportContextDAO {

    Report getReportContextByStudentId(String studentId) throws OperationException;

    String getTotalReportedHoursByStudentId(String studentId) throws OperationException;

    MonthlyReport getMonthlyReportData(String studentId, int reportId) throws OperationException;

    String getHoursAccumulate(String studentId) throws OperationException;
}