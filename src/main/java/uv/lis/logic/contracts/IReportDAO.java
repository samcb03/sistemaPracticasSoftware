package uv.lis.logic.contracts;

import java.util.List;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.dto.PartialReport;

public interface IReportDAO {

    List<Report> getReports();

    Report getReportById(int idReporte);

    PartialReport getPartialReportById(int idPartialReport);

    boolean registerPartialReport(PartialReport partialReport);

    boolean modifyPartialReport(PartialReport partialReport);

    PartialReport getMensualReportById(int idMensualReport);

    boolean registerMensualReport(PartialReport mensualReport);

    boolean modifyMensualReport(PartialReport mensualReport);

    FinalReport getFinalReportById(int idFinalReport);

    boolean registerFinalReport(FinalReport finalReport);

    boolean modifyFinalReport(FinalReport finalReport);
    
}
