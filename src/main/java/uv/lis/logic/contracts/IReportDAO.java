package uv.lis.logic.contracts;

import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

public interface IReportDAO {

    List<Report> getReports() throws OperationException;

    Optional<PartialReport> getPartialReportById(int idPartialReport) throws OperationException;

    boolean registerPartialReport(PartialReport partialReport) throws OperationException;

    boolean modifyPartialReport(PartialReport partialReport) throws OperationException;

    Optional<FinalReport> getFinalReportById(int idFinalReport) throws OperationException;

    boolean registerFinalReport(FinalReport finalReport) throws OperationException;

    boolean modifyFinalReport(FinalReport finalReport) throws OperationException;

    boolean registerMonthlyReport(MonthlyReport monthlyReport) throws OperationException;

    void evaluationReport(Report report) throws OperationException;
}