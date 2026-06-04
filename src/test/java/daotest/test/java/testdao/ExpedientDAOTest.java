package daotest.test.java.testdao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

@ExtendWith(MockitoExtension.class)
class ExpedientDAOTest {

    @Mock private MySQLConnectionManager connectionManager;
    @Mock private Connection databaseConnection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    @Mock private ResultSet generatedKeys;

    private ExpedientDAO expedientDAO;
    private static final int EXPEDIENT_ID = 2;

    @BeforeEach
    void setUp() throws Exception {
        expedientDAO = new ExpedientDAO();
        Field field = ExpedientDAO.class.getDeclaredField("connectionManager");
        field.setAccessible(true);
        field.set(expedientDAO, connectionManager);
        when(connectionManager.getConnection()).thenReturn(databaseConnection);
    }

    @Test
    void saveDocument_validExpedient_returnsGeneratedId() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(42);

        int result = expedientDAO.saveDocument(buildExpedient());

        assertNotEquals(-1, result);
    }

    @Test
    void saveDocument_noRowsAffected_returnsMinusOne() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        int result = expedientDAO.saveDocument(buildExpedient());

        assertEquals(-1, result);
    }

    @Test
    void saveDocument_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> expedientDAO.saveDocument(buildExpedient()));
    }

    @Test
    void getAllDocuments_documentsExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("nombre")).thenReturn("documento.pdf");
        when(resultSet.getString("tipoDocumento")).thenReturn("Carta de presentación");
        when(resultSet.getString("url")).thenReturn("/ruta/documento.pdf");
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getInt("idTipoDocumento")).thenReturn(1);

        List<Expedient> result = expedientDAO.getAllDocuments();

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllDocuments_noDocuments_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Expedient> result = expedientDAO.getAllDocuments();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDocuments_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> expedientDAO.getAllDocuments());
    }

    @Test
    void getDocumentsByStudentId_documentsFound_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("idExpediente")).thenReturn(1);
        when(resultSet.getString("nombre")).thenReturn("documento.pdf");
        when(resultSet.getString("tipoDocumento")).thenReturn("Carta de presentación");
        when(resultSet.getString("url")).thenReturn("/ruta/documento.pdf");
        when(resultSet.getString("matricula")).thenReturn("S23013127");
        when(resultSet.getInt("idTipoDocumento")).thenReturn(1);
        when(resultSet.getBoolean("estaValidado")).thenReturn(false);

        List<Expedient> result = expedientDAO.getDocumentsByStudentId("S23013127");

        assertFalse(result.isEmpty());
    }

    @Test
    void getDocumentsByStudentId_noDocuments_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Expedient> result = expedientDAO.getDocumentsByStudentId("S99999999");

        assertTrue(result.isEmpty());
    }

    @Test
    void getDocumentsByStudentId_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> expedientDAO.getDocumentsByStudentId("S23013127"));
    }

    @Test
    void getIdDocumentTypeByName_typeFound_returnsNonEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idTipoDocumento")).thenReturn(1);

        Optional<Integer> result = expedientDAO.getIdDocumentTypeByName("Carta de presentación");

        assertTrue(result.isPresent());
    }

    @Test
    void getIdDocumentTypeByName_typeNotFound_returnsEmpty() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Integer> result = expedientDAO.getIdDocumentTypeByName("TipoInexistente");

        assertFalse(result.isPresent());
    }

    @Test
    void getIdDocumentTypeByName_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> expedientDAO.getIdDocumentTypeByName("Carta de presentación"));
    }

    @Test
    void updateValidationStatus_validExpedient_returnsTrue() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = expedientDAO.updateValidationStatus(EXPEDIENT_ID, true);

        assertTrue(result);
    }

    @Test
    void updateValidationStatus_noRowsAffected_returnsFalse() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = expedientDAO.updateValidationStatus(EXPEDIENT_ID, true);

        assertFalse(result);
    }

    @Test
    void updateValidationStatus_databaseError_throwsOperationException() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(OperationException.class,
            () -> expedientDAO.updateValidationStatus(EXPEDIENT_ID, true));
    }

    @Test
    void getAllDocumentsTypes_typesExist_returnsNonEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("nombreTipoDocumento")).thenReturn("Carta de presentación");

        List<String> result = expedientDAO.getAllDocumentsTypes();

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllDocumentsTypes_noTypes_returnsEmptyList() throws Exception {
        when(databaseConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<String> result = expedientDAO.getAllDocumentsTypes();

        assertTrue(result.isEmpty());
    }

    private Expedient buildExpedient() {
        return new Expedient(
            "documento.pdf",
            "Carta de presentación",
            "/ruta/documento.pdf",
            "S23013127",
            1
        );
    }
}