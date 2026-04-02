package uv.lis.logic.contracts;


import java.sql.SQLException;
import java.util.List;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.dto.PartialReport;


public interface IReportDAO {
    List<Report> getReports();

    PartialReport getReportById(int idPartialReport) throws SQLException;

    boolean registerPartialReport(PartialReport partialReport) throws SQLException;

    boolean modifyPartialReport(PartialReport partialReport) throws SQLException;

    FinalReport getFinalReportById(int idFinalReport) throws SQLException;

    boolean registerFinalReport(FinalReport finalReport) throws SQLException;

    boolean modifyFinalReport(FinalReport finalReport) throws SQLException;
}
