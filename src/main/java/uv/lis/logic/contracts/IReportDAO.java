package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.dto.PartialReport;


public interface IReportDAO {
    List<Report> getReports() throws SQLException, OperationException;

    PartialReport getPartialReportById(int idPartialReport) throws SQLException, OperationException;

    boolean registerPartialReport(PartialReport partialReport) throws SQLException, OperationException;

    boolean modifyPartialReport(PartialReport partialReport) throws SQLException, OperationException;

    FinalReport getFinalReportById(int idFinalReport) throws SQLException, OperationException;

    boolean registerFinalReport(FinalReport finalReport) throws SQLException, OperationException;

    boolean modifyFinalReport(FinalReport finalReport) throws SQLException, OperationException;

    void evaluationReport(Report report)throws SQLException, OperationException;
}
