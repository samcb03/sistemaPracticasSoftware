package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_ROWS_AFFECTED;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.dataaccess.MySQLConnectionManager;
import uv.lis.logic.contracts.IExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileManager;
import uv.lis.logic.utils.FileValidator;

public class ExpedientDAO implements IExpedientDAO {
    private static final Logger LOGGER = Logger.getLogger(ExpedientDAO.class.getName());

    private static final int MONTHLY_REPORT_DOCUMENT_TYPE_ID = 3;
    private static final int FINAL_REPORT_DOCUMENT_TYPE_ID = 2;

    private MySQLConnectionManager connectionManager;
    public ExpedientDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ExpedientDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public int saveDocument(Expedient expedient) throws OperationException {
        int generatedId = -1;
        String expedientQuery = "INSERT INTO expediente (nombre, url, matricula, idTipoDocumento)" 
                              + " VALUES (?, ?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery, 
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getUrl()); 
            preparedStatement.setString(3, expedient.getIdStudent());
            preparedStatement.setInt(4, expedient.getIdTypeDocument());
            
            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > NO_ROWS_AFFECTED) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el documento en el expediente", e);
            throw new OperationException("Error al guardar el documento en el expediente.", e);
        }
        
        return generatedId;
    }

    @Override
    public List<Expedient> getAllDocuments() throws OperationException {
        List<Expedient> documents = new ArrayList<>();
        String expedientQuery = "SELECT e.nombre, td.nombreTipoDocumento, e.url, e.matricula, "
                              + "e.idTipoDocumento "
                              + "FROM expediente e "
                              + "INNER JOIN Tipo_Documento td "
                              + "ON e.idTipoDocumento = td.idTipoDocumento";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                Expedient expedient = new Expedient(
                    resultSet.getString("nombre"),
                    resultSet.getString("nombreTipoDocumento"),
                    resultSet.getString("url"),
                    resultSet.getString("matricula"),
                    resultSet.getInt("idTipoDocumento")
                );
                documents.add(expedient);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los documentos del expediente", e);
            throw new OperationException("No se pudieron obtener los documentos", e);
        }
        
        return documents;
    }

    @Override
    public Optional<Integer> getIdDocumentTypeByName(String typeName) throws OperationException {
        Optional<Integer> idType = Optional.empty();
        String expedientQuery = "SELECT idTipoDocumento FROM Tipo_Documento WHERE nombreTipoDocumento = ?";
        
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
            
            preparedStatement.setString(1, typeName);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("idTipoDocumento");
                    idType = Optional.of(id);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener el ID del tipo de documento", e);
            throw new OperationException("Error al consultar el catálogo de documentos", e);
        }
        
        return idType;
    }

    @Override
    public void uploadDocument(String idStudent, String typeDocument, File file) throws OperationException {
        FileValidator.validateFile(file);
        Optional<Integer> validTypeDocument = getIdDocumentTypeByName(typeDocument);

        if (validTypeDocument.isEmpty()) {
            throw new OperationException("Tipo de documento no válido: " + typeDocument, null);
        }

        int idTypeDocument = validTypeDocument.get();
        String url = FileManager.saveFile(file, idStudent);
        boolean isRegistered = false;

        try {
            Expedient expedient = new Expedient(file.getName(), typeDocument, url, idStudent, idTypeDocument);
            isRegistered = persistDocument(expedient);
        } finally {
            if (!isRegistered) {
                FileManager.deleteFile(url);
            }
        }

        if (!isRegistered) {
            throw new OperationException("No se pudo registrar el documento en la base de datos.", null);
        }
    }

    @Override
    public ArrayList<String> getAllDocumentsTypes() throws OperationException {
        String documentTypesQuery = "SELECT nombreTipoDocumento FROM Tipo_Documento";
        ArrayList<String> documentTypes = new ArrayList<>();

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(documentTypesQuery);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                documentTypes.add(resultSet.getString("nombreTipoDocumento"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los tipos de documentos", e);
            throw new OperationException("No se pudieron obtener los tipos de documentos", e);
        }

        return documentTypes;
    }

    @Override
    public List<Expedient> getDocumentsByStudentId(String idStudent) throws OperationException {
        List<Expedient> studentDocuments = new ArrayList<>();
        String expedientQuery = "SELECT e.idExpediente, e.nombre, td.nombreTipoDocumento, e.url, "
                              + "e.matricula, e.idTipoDocumento, e.estaValidado "
                              + "FROM expediente e "
                              + "INNER JOIN Tipo_Documento td "
                              + "ON e.idTipoDocumento = td.idTipoDocumento "
                              + "WHERE e.matricula = ?";
    
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
    
            preparedStatement.setString(1, idStudent);
    
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Expedient expedient = buildExpedientFromResultSet(resultSet);
                    studentDocuments.add(expedient);
                }
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Error al obtener los documentos del alumno", exception);
            throw new OperationException(
                "No se pudieron obtener los documentos del alumno. Intente más tarde", exception);
        }
        return studentDocuments;
    }

    @Override
    public boolean updateValidationStatus(int idExpedient, boolean isValidated) throws OperationException {
        boolean isUpdated = false;
        String expedientQuery = "UPDATE expediente SET estaValidado = ? WHERE idExpediente = ?";
    
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
    
            preparedStatement.setBoolean(1, isValidated);
            preparedStatement.setInt(2, idExpedient);
    
            int affectedRows = preparedStatement.executeUpdate();
            isUpdated = affectedRows > NO_ROWS_AFFECTED;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado de validación del documento", e);
            throw new OperationException("No se pudo actualizar el estado del documento. Intente más tarde", e);
        }
        return isUpdated;
    }

    @Override
    public boolean isFinalReportValidated(String idStudent) throws OperationException {
        boolean isValidated = false;
        String expedientQuery = "SELECT estaValidado FROM expediente "
                              + "WHERE matricula = ? AND idTipoDocumento = ? LIMIT 1";
 
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
 
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, FINAL_REPORT_DOCUMENT_TYPE_ID);
 
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isValidated = resultSet.getBoolean("estaValidado");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la validación del reporte final", e);
            throw new OperationException("Error al verificar el reporte final. Intente más tarde", e);
        }
        return isValidated;
    }

    private Expedient buildExpedientFromResultSet(ResultSet resultSet) throws SQLException {
        Expedient expedient = new Expedient(
            resultSet.getString("nombre"),
            resultSet.getString("nombreTipoDocumento"),
            resultSet.getString("url"),
            resultSet.getString("matricula"),
            resultSet.getInt("idTipoDocumento")
        );
        expedient.setId(resultSet.getInt("idExpediente"));
        expedient.setIsValidated(resultSet.getBoolean("estaValidado"));
        return expedient;
    }

    private Optional<String> getDocumentUrl(String idStudent, int idTypeDocument) throws OperationException {
        Optional<String> documentUrl = Optional.empty();
        String expedientQuery = "SELECT url FROM expediente "
                              + "WHERE matricula = ? AND idTipoDocumento = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idTypeDocument);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    documentUrl = Optional.ofNullable(resultSet.getString("url"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al consultar el documento previo del expediente", e);
            throw new OperationException("Error al consultar el documento previo", e);
        }
        return documentUrl;
    }

    private boolean replaceDocument(Expedient expedient) throws OperationException {
        boolean isReplaced = false;
        String expedientQuery = "UPDATE expediente SET nombre = ?, url = ?, estaValidado = ? "
                              + "WHERE matricula = ? AND idTipoDocumento = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getUrl());
            preparedStatement.setBoolean(3, false);
            preparedStatement.setString(4, expedient.getIdStudent());
            preparedStatement.setInt(5, expedient.getIdTypeDocument());

            isReplaced = preparedStatement.executeUpdate() > NO_ROWS_AFFECTED;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al reemplazar el documento del expediente", e);
            throw new OperationException("No se pudo reemplazar el documento. Intente más tarde", e);
        }
        return isReplaced;
    }

    private boolean persistDocument(Expedient expedient) throws OperationException {
        boolean isSaved;

        if (expedient.getIdTypeDocument() == MONTHLY_REPORT_DOCUMENT_TYPE_ID) {
            isSaved = saveDocument(expedient) > NO_ROWS_AFFECTED;
        } else {
            isSaved = replaceOrInsertDocument(expedient);
        }
        return isSaved;
    }

    private boolean replaceOrInsertDocument(Expedient expedient) throws OperationException {
        Optional<String> previousUrl =
            getDocumentUrl(expedient.getIdStudent(), expedient.getIdTypeDocument());
        boolean isSaved;

        if (previousUrl.isPresent()) {
            isSaved = replaceDocument(expedient);
            if (isSaved && !previousUrl.get().equals(expedient.getUrl())) {
                FileManager.deleteFile(previousUrl.get());
            }
        } else {
            isSaved = saveDocument(expedient) > NO_ROWS_AFFECTED;
        }
        return isSaved;
    }
}