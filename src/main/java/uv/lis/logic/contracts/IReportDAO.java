package uv.lis.logic.contracts;

import java.util.List;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.dto.PartialReport;

public interface IReportDAO {

    List<Report> getReports();

    PartialReport getReportById(int idPartialReport);

    boolean registerPartialReport(PartialReport partialReport);

    boolean modifyPartialReport(PartialReport partialReport);

    FinalReport getFinalReportById(int idFinalReport);

    boolean registerFinalReport(FinalReport finalReport);

    boolean modifyFinalReport(FinalReport idFinalReport);
    
}
