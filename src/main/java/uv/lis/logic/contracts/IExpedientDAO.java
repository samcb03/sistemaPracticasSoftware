package uv.lis.logic.contracts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for student expedient documents.
 */
public interface IExpedientDAO {

    /**
     * Registers a document in a student's expedient.
     *
     * @param expedient the expedient document data to register
     * @return the generated identifier of the registered document
     * @throws OperationException if the document cannot be registered
     */
    int saveDocument(Expedient expedient) throws OperationException;

    /**
     * Retrieves all the registered expedient documents.
     *
     * @return the list of documents, empty if there are none
     * @throws OperationException if the documents cannot be retrieved
     */
    List<Expedient> getAllDocuments() throws OperationException;

    /**
     * Retrieves the identifier of a document type from its name.
     *
     * @param name the name of the document type to look up
     * @return the document type identifier if it exists, empty otherwise
     * @throws OperationException if the identifier cannot be retrieved
     */
    Optional<Integer> getIdDocumentTypeByName(String name) throws OperationException;

    /**
     * Uploads a document file to a student's expedient.
     *
     * @param idStudent the identifier of the student who owns the expedient
     * @param typeDocument the type of the document to upload
     * @param file the document file to upload
     * @throws OperationException if the document cannot be uploaded
     */
    void uploadDocument(String idStudent, String typeDocument, File file) throws OperationException;

    /**
     * Uploads a monthly report document linked to a specific generated report.
     * If a rejected document already exists for that report, it is replaced.
     *
     * @param idStudent the identifier of the student who owns the expedient
     * @param file the monthly report file to upload
     * @param idReport the identifier of the report the document corresponds to
     * @throws OperationException if the document cannot be uploaded
     */
    void uploadMonthlyReport(String idStudent, File file, int idReport) throws OperationException;

    /**
     * Retrieves the names of all the available document types.
     *
     * @return the list of document type names, empty if there are none
     * @throws OperationException if the document types cannot be retrieved
     */
    ArrayList<String> getAllDocumentsTypes() throws OperationException;

    /**
     * Retrieves the expedient documents that belong to a student.
     *
     * @param idStudent the identifier of the student whose documents are retrieved
     * @return the list of documents for the student, empty if there are none
     * @throws OperationException if the documents cannot be retrieved
     */
    List<Expedient> getDocumentsByStudentId(String idStudent) throws OperationException;

    /**
     * Updates the status of an expedient document.
     *
     * @param idExpedient the identifier of the expedient document to update
     * @param idStatus the new status identifier from the document status catalog
     * @return true if the status was updated, false otherwise
     * @throws OperationException if the status cannot be updated
     */
    boolean updateDocumentStatus(int idExpedient, int idStatus) throws OperationException;

    /**
     * Indicates whether a student's final report has been validated by the professor.
     *
     * @param idStudent the identifier of the student to verify
     * @return true if the final report exists and is validated, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isFinalReportValidated(String idStudent) throws OperationException;

    /**
     * Indicates whether every initial document of a student has been validated.
     *
     * @param idStudent the identifier of the student to verify
     * @return true if all the initial documents exist and are validated, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean areInitialDocumentsValidated(String idStudent) throws OperationException;

    /**
     * Indicates whether a student already has a validated document of a given type.
     *
     * @param idStudent the identifier of the student to verify
     * @param idTypeDocument the document type identifier to verify
     * @return true if a validated document of that type exists, false otherwise
     * @throws OperationException if the verification cannot be completed
     */
    boolean isDocumentTypeValidated(String idStudent, int idTypeDocument) throws OperationException;

    /**
     * Counts the number of documents a student has uploaded of a given type.
     *
     * @param studentId the identifier of the student to verify
     * @param typeId the document type identifier to count
     * @return the number of documents uploaded of that type
     * @throws OperationException if the count cannot be retrieved
     */
    int countDocumentsByStudentAndType(String studentId, int typeId) throws OperationException;

    /**
     * Retrieves the identifiers of the students enrolled in an educational experience
     * who have uploaded at least one initial document.
     *
     * @param nrc the identifier of the educational experience to filter by
     * @return the list of student identifiers, empty if there are none
     * @throws OperationException if the student identifiers cannot be retrieved
     */
    List<String> getStudentIdsWithInitialDocuments(int nrc) throws OperationException;

    /**
     * Retrieves the identifiers of the students enrolled in an educational experience
     * who have uploaded at least one document of a given type.
     *
     * @param nrc the identifier of the educational experience to filter by
     * @param idTypeDocument the document type identifier to filter by
     * @return the list of student identifiers, empty if there are none
     * @throws OperationException if the student identifiers cannot be retrieved
     */
    List<String> getStudentIdsWithDocumentType(int nrc, int idTypeDocument) throws OperationException;

    //TODO falta el javadoc
    List<String> getStudentIdsWithLiberationLetter() throws OperationException;
}