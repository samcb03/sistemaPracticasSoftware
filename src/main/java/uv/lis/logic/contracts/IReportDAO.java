package uv.lis.logic.contracts;

import java.util.List;

import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

public interface IReportDAO {
    List<Report> getReports() throws  OperationException;

    PartialReport getPartialReportById(int idPartialReport) throws  OperationException;

    boolean registerPartialReport(PartialReport partialReport) throws  OperationException;

    boolean modifyPartialReport(PartialReport partialReport) throws  OperationException;

    FinalReport getFinalReportById(int idFinalReport) throws  OperationException;

    boolean registerFinalReport(FinalReport finalReport) throws  OperationException;

    boolean modifyFinalReport(FinalReport finalReport) throws  OperationException;

    void evaluationReport(Report report)throws  OperationException;
}
