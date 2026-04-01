package uv.lis.logic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IReportDAO;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;

public class ReportDAO implements IReportDAO{

    @Override
    public List<Report> getReports() {
        List<Report> reports = new ArrayList<>();
        try {
            Connection databaseConnection = MySQLConnectionManager.getConnection();
            String reportQuery = "SELECT *FROM Reporte;";
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(reportQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            Report report = new Report();
            report.setId(resultSet.getInt("idReporte"));

        }


        } catch (SQLException e){

        }

    }

    @Override
    public Report getReportById(int idReporte) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReportById'");
    }

    @Override
    public PartialReport getPartialReportById(int idPartialReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPartialReportById'");
    }

    @Override
    public boolean registerPartialReport(PartialReport partialReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerPartialReport'");
    }

    @Override
    public boolean modifyPartialReport(PartialReport partialReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modifyPartialReport'");
    }

    @Override
    public PartialReport getMensualReportById(int idMensualReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMensualReportById'");
    }

    @Override
    public boolean registerMensualReport(PartialReport mensualReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerMensualReport'");
    }

    @Override
    public boolean modifyMensualReport(PartialReport mensualReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modifyMensualReport'");
    }

    @Override
    public FinalReport getFinalReportById(int idFinalReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFinalReportById'");
    }

    @Override
    public boolean registerFinalReport(FinalReport finalReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerFinalReport'");
    }

    @Override
    public boolean modifyFinalReport(FinalReport finalReport) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modifyFinalReport'");
    }
    
}
