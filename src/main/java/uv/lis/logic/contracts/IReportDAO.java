package uv.lis.logic.contracts;

import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Report;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for partial, monthly and final reports.
 */
public interface IReportDAO {

    /**
     * Retrieves all the registered reports.
     *
     * @return the list of reports, empty if there are none
     * @throws OperationException if the reports cannot be retrieved
     */
    List<Report> getAllReports() throws OperationException;

    /**
     * Retrieves the partial report identified by the given id.
     *
     * @param idPartialReport the identifier of the partial report to retrieve
     * @return the partial report if it exists, empty otherwise
     * @throws OperationException if the partial report cannot be retrieved
     */
    Optional<PartialReport> getPartialReportById(int idPartialReport) throws OperationException;

    /**
     * Registers a new partial report in the system.
     *
     * @param partialReport the partial report data to register
     * @return true if the partial report was registered, false otherwise
     * @throws OperationException if the partial report cannot be registered
     */
    boolean registerPartialReport(PartialReport partialReport) throws OperationException;

    /**
     * Updates the data of an existing partial report.
     *
     * @param partialReport the partial report data to update
     * @return true if the partial report was updated, false otherwise
     * @throws OperationException if the partial report cannot be updated
     */
    boolean modifyPartialReport(PartialReport partialReport) throws OperationException;

    /**
     * Retrieves the final report identified by the given id.
     *
     * @param idFinalReport the identifier of the final report to retrieve
     * @return the final report if it exists, empty otherwise
     * @throws OperationException if the final report cannot be retrieved
     */
    Optional<FinalReport> getFinalReportById(int idFinalReport) throws OperationException;

    /**
     * Registers a new final report in the system.
     *
     * @param finalReport the final report data to register
     * @return true if the final report was registered, false otherwise
     * @throws OperationException if the final report cannot be registered
     */
    boolean registerFinalReport(FinalReport finalReport) throws OperationException;

    /**
     * Updates the data of an existing final report.
     *
     * @param finalReport the final report data to update
     * @return true if the final report was updated, false otherwise
     * @throws OperationException if the final report cannot be updated
     */
    boolean modifyFinalReport(FinalReport finalReport) throws OperationException;

    /**
     * Registers a new monthly report in the system.
     *
     * @param monthlyReport the monthly report data to register
     * @return true if the monthly report was registered, false otherwise
     * @throws OperationException if the monthly report cannot be registered
     */
    boolean registerMonthlyReport(MonthlyReport monthlyReport) throws OperationException;

    /**
     * Saves the evaluation results of a report.
     *
     * @param report the report that holds the evaluation results to save
     * @throws OperationException if the evaluation cannot be saved
     */
    void evaluationReport(Report report) throws OperationException;

    /**
     * Indicates whether a student has a saved report of the given document type.
     *
     * @param idStudent the identifier of the student to verify
     * @param idTypeDocument the document type identifier of the report to look up
     * @return true if a report of that type exists for the student, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean hasReportOfType(String idStudent, int idTypeDocument) throws OperationException;

    /**
     * Counts the number of monthly reports registered for a student.
     *
     * @param studentId the identifier of the student to verify
     * @return the number of monthly reports registered for the student
     * @throws OperationException if the count cannot be retrieved
     */
    int countMonthlyReportsByStudent(String studentId) throws OperationException;
}