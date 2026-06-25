package daotest.test.java.testdao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static uv.lis.logic.utils.InputValidator.STATUS_REQUESTED;
import static uv.lis.logic.utils.InputValidator.STATUS_REJECTED;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

class ExpedientDAOTest {

    private static final int EXPEDIENT_ID = 2;
    private static final int FIRST_EXPEDIENT_ID = 4;
    private static final int GENERATED_ID = 42;
    private static final int FIRST_VALUE = 1;
    private static final int NO_ROWS_AFFECTED = 0;
    private static final int DOCUMENT_TYPE_ID = 3;
    private static final int MINUS_ONE = -1;
    private static final int NRC = 12345;

    private static final String STATUS_PENDING_NAME = "Pendiente";

    private static final String STUDENT_ID = "S23013127";
    private static final String DOCUMENT_NAME = "documento.pdf";
    private static final String DOCUMENT_TYPE = "Carta de presentación";
    private static final String DOCUMENT_URL = "/ruta/documento.pdf";
    private static final String DOCUMENT_TYPE_COLUMN = "nombreTipoDocumento";
    private static final String STUDENT_ID_COLUMN = "matricula";
    private static final String DATABASE_ERROR_MESSAGE = "Fallo";

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private ExpedientDAO expedientDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        expedientDAO = new ExpedientDAO();
        Field field = ExpedientDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(expedientDAO, connectionManager);

        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    private void mockQueryExecution() throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    private void mockUpdateExecution(int rowsAffected) throws SQLException {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
    }

    private void mockResultSetSingleExpedient() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idExpediente")).thenReturn(FIRST_EXPEDIENT_ID);
        when(resultSet.getString("nombre")).thenReturn(DOCUMENT_NAME);
        when(resultSet.getString("nombreTipoDocumento")).thenReturn(DOCUMENT_TYPE);
        when(resultSet.getString("url")).thenReturn(DOCUMENT_URL);
        when(resultSet.getString("matricula")).thenReturn(STUDENT_ID);
        when(resultSet.getInt("idTipoDocumento")).thenReturn(DOCUMENT_TYPE_ID);
        when(resultSet.getInt("idEstatus")).thenReturn(STATUS_REQUESTED);
        when(resultSet.getString("estatus")).thenReturn(STATUS_PENDING_NAME);
    }

    private Expedient builderExpedient() {
        return new Expedient(DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_URL, STUDENT_ID, DOCUMENT_TYPE_ID);
    }

    @Test
    void saveDocument_successful_returnsGeneratedId() throws Exception {
        mockUpdateExecution(FIRST_VALUE);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(FIRST_VALUE)).thenReturn(GENERATED_ID);

        assertNotEquals(MINUS_ONE, expedientDAO.saveDocument(builderExpedient()));
    }

    @Test
    void saveDocument_noRowsAffected_returnsMinusOne() throws Exception {
        mockUpdateExecution(NO_ROWS_AFFECTED);

        assertEquals(MINUS_ONE, expedientDAO.saveDocument(builderExpedient()));
    }

    @Test
    void saveDocument_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class, () -> expedientDAO.saveDocument(builderExpedient()));
    }

    @Test
    void getAllDocuments_documentsExist_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleExpedient();

        assertFalse(expedientDAO.getAllDocuments().isEmpty());
    }

    @Test
    void getAllDocuments_noDocuments_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getAllDocuments().isEmpty());
    }

    @Test
    void getAllDocuments_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class, () -> expedientDAO.getAllDocuments());
    }

    @Test
    void getDocumentsByStudentId_documentsFound_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        mockResultSetSingleExpedient();

        assertFalse(expedientDAO.getDocumentsByStudentId(STUDENT_ID).isEmpty());
    }

    @Test
    void getDocumentsByStudentId_noDocuments_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getDocumentsByStudentId(STUDENT_ID).isEmpty());
    }

    @Test
    void getDocumentsByStudentId_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.getDocumentsByStudentId(STUDENT_ID));
    }

    @Test
    void getIdDocumentTypeByName_typeFound_returnsNonEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idTipoDocumento")).thenReturn(DOCUMENT_TYPE_ID);

        assertTrue(expedientDAO.getIdDocumentTypeByName(DOCUMENT_TYPE).isPresent());
    }

    @Test
    void getIdDocumentTypeByName_typeNotFound_returnsEmpty() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertFalse(expedientDAO.getIdDocumentTypeByName(DOCUMENT_TYPE).isPresent());
    }

    @Test
    void getIdDocumentTypeByName_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.getIdDocumentTypeByName(DOCUMENT_TYPE));
    }

    @Test
    void updateDocumentStatus_validateSuccessful_returnsTrue() throws Exception {
        mockQueryExecution();
        when(preparedStatement.executeUpdate()).thenReturn(FIRST_VALUE);

        assertTrue(expedientDAO.updateDocumentStatus(EXPEDIENT_ID, STATUS_ASSIGNED));
    }

    @Test
    void updateDocumentStatus_rejectSuccessful_returnsTrue() throws Exception {
        mockQueryExecution();
        when(preparedStatement.executeUpdate()).thenReturn(FIRST_VALUE);

        assertTrue(expedientDAO.updateDocumentStatus(EXPEDIENT_ID, STATUS_REJECTED));
    }

    @Test
    void updateDocumentStatus_noRowsAffected_returnsFalse() throws Exception {
        mockQueryExecution();
        when(preparedStatement.executeUpdate()).thenReturn(NO_ROWS_AFFECTED);

        assertFalse(expedientDAO.updateDocumentStatus(EXPEDIENT_ID, STATUS_ASSIGNED));
    }

    @Test
    void updateDocumentStatus_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.updateDocumentStatus(EXPEDIENT_ID, STATUS_REJECTED));
    }

    @Test
    void getAllDocumentsTypes_typesExist_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(DOCUMENT_TYPE_COLUMN)).thenReturn(DOCUMENT_TYPE);

        assertFalse(expedientDAO.getAllDocumentsTypes().isEmpty());
    }

    @Test
    void getAllDocumentsTypes_noTypes_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getAllDocumentsTypes().isEmpty());
    }

    @Test
    void getAllDocumentsTypes_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class, () -> expedientDAO.getAllDocumentsTypes());
    }

    @Test
    void isFinalReportValidated_reportValidated_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idEstatus")).thenReturn(STATUS_ASSIGNED);
 
        assertTrue(expedientDAO.isFinalReportValidated(STUDENT_ID));
    }
 
    @Test
    void isFinalReportValidated_reportNotValidated_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idEstatus")).thenReturn(STATUS_REQUESTED);
 
        assertFalse(expedientDAO.isFinalReportValidated(STUDENT_ID));
    }
 
    @Test
    void isFinalReportValidated_reportNotFound_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);
 
        assertFalse(expedientDAO.isFinalReportValidated(STUDENT_ID));
    }
 
    @Test
    void isFinalReportValidated_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));
 
        assertThrows(OperationException.class,
            () -> expedientDAO.isFinalReportValidated(STUDENT_ID));
    }

    @Test
    void isDocumentTypeValidated_documentValidated_returnsTrue() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(FIRST_VALUE);

        assertTrue(expedientDAO.isDocumentTypeValidated(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void isDocumentTypeValidated_documentNotValidated_returnsFalse() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(NO_ROWS_AFFECTED);

        assertFalse(expedientDAO.isDocumentTypeValidated(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void isDocumentTypeValidated_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.isDocumentTypeValidated(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void countDocumentsByStudentAndType_documentsExist_returnsCount() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(FIRST_VALUE);

        assertEquals(FIRST_VALUE, expedientDAO.countDocumentsByStudentAndType(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void countDocumentsByStudentAndType_noDocuments_returnsZero() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(FIRST_VALUE)).thenReturn(NO_ROWS_AFFECTED);

        assertEquals(NO_ROWS_AFFECTED, expedientDAO.countDocumentsByStudentAndType(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void countDocumentsByStudentAndType_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.countDocumentsByStudentAndType(STUDENT_ID, DOCUMENT_TYPE_ID));
    }

    @Test
    void getStudentIdsWithInitialDocuments_documentsExist_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(STUDENT_ID_COLUMN)).thenReturn(STUDENT_ID);

        assertFalse(expedientDAO.getStudentIdsWithInitialDocuments(NRC).isEmpty());
    }

    @Test
    void getStudentIdsWithInitialDocuments_noDocuments_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getStudentIdsWithInitialDocuments(NRC).isEmpty());
    }

    @Test
    void getStudentIdsWithInitialDocuments_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.getStudentIdsWithInitialDocuments(NRC));
    }

    @Test
    void getStudentIdsWithDocumentType_documentsExist_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(STUDENT_ID_COLUMN)).thenReturn(STUDENT_ID);

        assertFalse(expedientDAO.getStudentIdsWithDocumentType(NRC, DOCUMENT_TYPE_ID).isEmpty());
    }

    @Test
    void getStudentIdsWithDocumentType_noDocuments_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getStudentIdsWithDocumentType(NRC, DOCUMENT_TYPE_ID).isEmpty());
    }

    @Test
    void getStudentIdsWithDocumentType_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.getStudentIdsWithDocumentType(NRC, DOCUMENT_TYPE_ID));
    }

    @Test
    void getStudentIdsWithLiberationLetter_studentsExist_returnsNonEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(STUDENT_ID_COLUMN)).thenReturn(STUDENT_ID);

        assertFalse(expedientDAO.getStudentIdsWithLiberationLetter().isEmpty());
    }

    @Test
    void getStudentIdsWithLiberationLetter_noStudents_returnsEmptyList() throws Exception {
        mockQueryExecution();
        when(resultSet.next()).thenReturn(false);

        assertTrue(expedientDAO.getStudentIdsWithLiberationLetter().isEmpty());
    }

    @Test
    void getStudentIdsWithLiberationLetter_sqlError_throwsOperationException() throws SQLException {
        when(connectionManager.getConnection()).thenThrow(new SQLException(DATABASE_ERROR_MESSAGE));

        assertThrows(OperationException.class,
            () -> expedientDAO.getStudentIdsWithLiberationLetter());
    }
}