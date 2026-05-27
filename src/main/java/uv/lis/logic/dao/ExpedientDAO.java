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
        String expedientQuery = "INSERT INTO expediente (nombre, tipoDocumento,url, matricula,idTipoDocumento)" 
                              + " VALUES (?, ?, ?,?,?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery, 
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getTypeDocument()); 
            preparedStatement.setString(3, expedient.getUrl()); 
            preparedStatement.setString(4, expedient.getIdStudent());
            preparedStatement.setInt(5,expedient.getIdTypeDocument());
            
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
            throw new OperationException("Error de SQL al guardar el documento en el expediente.", e);
        }
        
        return generatedId;
    }

    @Override
    public List<Expedient> getAllDocuments() throws OperationException {
        List<Expedient> documents = new ArrayList<>();
        String expedientQuery = "SELECT nombre, tipoDocumento, url, matricula, idTipoDocumento FROM expediente";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                Expedient expedient = new Expedient(
                    resultSet.getString("nombre"),
                    resultSet.getString("tipoDocumento"),
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

    public void uploadDocument(String idStudent, String typeDocument, File file) throws OperationException {
        FileValidator.validateFile(file);
        Optional<Integer> valideTypeDocument = getIdDocumentTypeByName(typeDocument);

        if (valideTypeDocument.isEmpty()) {
            throw new OperationException("Tipo de documento no válido: " + typeDocument, null);
        }

        int idTypeDocument = valideTypeDocument.get();

        deletePreviousDocumentIfExists(idStudent, idTypeDocument);
        String url = FileManager.saveFile(file, idStudent);

        Expedient expedient = new Expedient(file.getName(), typeDocument, url, idStudent, idTypeDocument);
        int generatedId = saveDocument(expedient);

        if (generatedId <= NO_ROWS_AFFECTED) {
            FileManager.deleteFile(url);
            throw new OperationException("No se pudo registrar el documento en la base de datos.", null);
        }
    }

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
        String expedientQuery = "SELECT idExpediente, nombre, tipoDocumento, url, "
                            + "matricula, idTipoDocumento, estaValidado "
                            + "FROM expediente "
                            + "WHERE matricula = ?";
    
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
    
            preparedStatement.setString(1, idStudent);
    
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Expedient expedient = buildExpedientFromResultSet(resultSet);
                    studentDocuments.add(expedient);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los documentos del alumno", e);
            throw new OperationException(
                "No se pudieron obtener los documentos del alumno. Intente más tarde", e);
        }
        return studentDocuments;
    }

    @Override
    public boolean updateValidationStatus(int idExpedient, boolean isValidated)
            throws OperationException {
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
            throw new OperationException(
                "No se pudo actualizar el estado del documento. Intente más tarde", e);
        }
        return isUpdated;
    }

    private Expedient buildExpedientFromResultSet(ResultSet resultSet) throws SQLException {
        Expedient expedient = new Expedient(
            resultSet.getString("nombre"),
            resultSet.getString("tipoDocumento"),
            resultSet.getString("url"),
            resultSet.getString("matricula"),
            resultSet.getInt("idTipoDocumento")
        );
        expedient.setId(resultSet.getInt("idExpediente"));
        expedient.setIsValidated(resultSet.getBoolean("estaValidado"));
        return expedient;
    }

    private void deletePreviousDocumentIfExists(String idStudent, int idDocumentType) throws OperationException {
    List<Integer> uniqueIds = new ArrayList<>();
        uniqueIds.add(1);
        uniqueIds.add(2);
        uniqueIds.add(3);
        uniqueIds.add(4);
        uniqueIds.add(5);
        uniqueIds.add(6);
        uniqueIds.add(9);

        if (uniqueIds.contains(idDocumentType)) {
            String  insertDocumentQuery= "SELECT url FROM expediente WHERE matricula = ? AND idTipoDocumento = ?";
            String remplaceQuery = "DELETE FROM expediente WHERE matricula = ? AND idTipoDocumento = ?";
            String oldUrl = null;

            try (Connection conn = connectionManager.getConnection();
                PreparedStatement preparedStatementInsert = conn.prepareStatement(insertDocumentQuery)) {
                
                preparedStatementInsert.setString(1, idStudent);
                preparedStatementInsert.setInt(2, idDocumentType);
                
                try (ResultSet resultSet = preparedStatementInsert.executeQuery()) {
                    if (resultSet.next()) {
                        oldUrl = resultSet.getString("url");
                    }
                }
                
                if (oldUrl != null) {
                    try (PreparedStatement preparedStatementDelete = conn.prepareStatement(remplaceQuery)) {
                        preparedStatementDelete.setString(1, idStudent);
                        preparedStatementDelete.setInt(2, idDocumentType);
                        preparedStatementDelete.executeUpdate();
                    }
                    FileManager.deleteFile(oldUrl);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al procesar el reemplazo de documento", e);
                throw new OperationException("Error al intentar reemplazar el archivo anterior", e);
            }
        }
    }

}