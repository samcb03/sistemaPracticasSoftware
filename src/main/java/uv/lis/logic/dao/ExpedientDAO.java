package uv.lis.logic.dao;

import static uv.lis.logic.utils.InputValidator.NO_VALUE;
import static uv.lis.logic.utils.InputValidator.STATUS_ASSIGNED;
import static uv.lis.logic.utils.InputValidator.STATUS_REQUESTED;

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
    private static final int FIRST_INITIAL_DOCUMENT_TYPE_ID = 5;
    private static final int NO_MISSING_INITIAL_DOCUMENTS = 0;
    private static final int NO_GENERATED_ID = -1;
    private static final int NEXT_ENTRY_OFFSET = 1;

    private MySQLConnectionManager connectionManager;
    public ExpedientDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    public ExpedientDAO(MySQLConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public int saveDocument(Expedient expedient) throws OperationException {
        int generatedId = NO_GENERATED_ID;
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
            
            if (affectedRows > NO_VALUE) {
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
                              + "e.matricula, e.idTipoDocumento, e.idEstatus, ed.estatus "
                              + "FROM expediente e "
                              + "INNER JOIN Tipo_Documento td "
                              + "ON e.idTipoDocumento = td.idTipoDocumento "
                              + "INNER JOIN EstatusDocumento ed "
                              + "ON e.idEstatus = ed.idEstatus "
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
    public boolean updateDocumentStatus(int idExpedient, int idStatus) throws OperationException {
        boolean isUpdated = false;
        String expedientQuery = "UPDATE expediente SET idEstatus = ? WHERE idExpediente = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setInt(1, idStatus);
            preparedStatement.setInt(2, idExpedient);

            int affectedRows = preparedStatement.executeUpdate();
            isUpdated = affectedRows > NO_VALUE;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estatus del documento", e);
            throw new OperationException("No se pudo actualizar el estatus del documento. Intente más tarde", e);
        }
        return isUpdated;
    }

    @Override
    public boolean isFinalReportValidated(String idStudent) throws OperationException {
        boolean isValidated = false;
        String expedientQuery = "SELECT idEstatus FROM expediente "
                              + "WHERE matricula = ? AND idTipoDocumento = ? LIMIT 1";
 
        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {
 
            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, FINAL_REPORT_DOCUMENT_TYPE_ID);
 
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isValidated = resultSet.getInt("idEstatus") == STATUS_ASSIGNED;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la validación del reporte final", e);
            throw new OperationException("Error al verificar el reporte final. Intente más tarde", e);
        }
        return isValidated;
    }

    @Override
    public boolean areInitialDocumentsValidated(String idStudent) throws OperationException {
        boolean areValidated = false;
        String expedientQuery = "SELECT COUNT(*) AS documentosFaltantes "
                              + "FROM Tipo_Documento td "
                              + "WHERE td.idTipoDocumento >= ? "
                              + "AND NOT EXISTS ("
                              + "SELECT 1 FROM expediente e "
                              + "WHERE e.matricula = ? "
                              + "AND e.idTipoDocumento = td.idTipoDocumento "
                              + "AND e.idEstatus = ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setInt(1, FIRST_INITIAL_DOCUMENT_TYPE_ID);
            preparedStatement.setString(2, idStudent);
            preparedStatement.setInt(3, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int missingDocuments = resultSet.getInt("documentosFaltantes");
                    areValidated = missingDocuments == NO_MISSING_INITIAL_DOCUMENTS;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la validación de los documentos iniciales", e);
            throw new OperationException("Error al verificar los documentos iniciales. Intente más tarde", e);
        }
        return areValidated;
    }

    @Override
    public boolean isDocumentTypeValidated(String idStudent, int idTypeDocument) throws OperationException {
        boolean isValidated = false;
        String expedientQuery = "SELECT COUNT(*) FROM expediente "
                              + "WHERE matricula = ? AND idTipoDocumento = ? AND idEstatus = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setString(1, idStudent);
            preparedStatement.setInt(2, idTypeDocument);
            preparedStatement.setInt(3, STATUS_ASSIGNED);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isValidated = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar si el documento ya está validado", e);
            throw new OperationException("Error al verificar el estatus del documento. Intente más tarde", e);
        }
        return isValidated;
    }

    @Override
    public List<String> getStudentIdsWithInitialDocuments(int nrc) throws OperationException {
        List<String> studentIds = new ArrayList<>();
        String expedientQuery = "SELECT DISTINCT e.matricula "
                              + "FROM expediente e "
                              + "INNER JOIN Alumno_Esta_EE aee ON e.matricula = aee.matricula "
                              + "WHERE aee.NRC = ? AND e.idTipoDocumento >= ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setInt(1, nrc);
            preparedStatement.setInt(2, FIRST_INITIAL_DOCUMENT_TYPE_ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    studentIds.add(resultSet.getString("matricula"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los alumnos con documentos iniciales subidos", e);
            throw new OperationException("Error al filtrar los alumnos por documentos. Intente más tarde", e);
        }
        return studentIds;
    }

    @Override
    public List<String> getStudentIdsWithDocumentType(int nrc, int idTypeDocument) throws OperationException {
        List<String> studentIds = new ArrayList<>();
        String expedientQuery = "SELECT DISTINCT e.matricula "
                              + "FROM expediente e "
                              + "INNER JOIN Alumno_Esta_EE aee ON e.matricula = aee.matricula "
                              + "WHERE aee.NRC = ? AND e.idTipoDocumento = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setInt(1, nrc);
            preparedStatement.setInt(2, idTypeDocument);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    studentIds.add(resultSet.getString("matricula"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los alumnos por tipo de documento subido", e);
            throw new OperationException("Error al filtrar los alumnos por documentos. Intente más tarde", e);
        }
        return studentIds;
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
        expedient.setIdStatus(resultSet.getInt("idEstatus"));
        expedient.setStatusName(resultSet.getString("estatus"));
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
        String expedientQuery = "UPDATE expediente SET nombre = ?, url = ?, idEstatus = ? "
                              + "WHERE matricula = ? AND idTipoDocumento = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getUrl());
            preparedStatement.setInt(3, STATUS_REQUESTED);
            preparedStatement.setString(4, expedient.getIdStudent());
            preparedStatement.setInt(5, expedient.getIdTypeDocument());

            isReplaced = preparedStatement.executeUpdate() > NO_VALUE;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al reemplazar el documento del expediente", e);
            throw new OperationException("No se pudo reemplazar el documento. Intente más tarde", e);
        }
        return isReplaced;
    }

    private boolean persistDocument(Expedient expedient) throws OperationException {
        boolean isSaved;

        if (expedient.getIdTypeDocument() == MONTHLY_REPORT_DOCUMENT_TYPE_ID) {
            int nextEntry = countDocumentsByStudentAndType(expedient.getIdStudent(),
                MONTHLY_REPORT_DOCUMENT_TYPE_ID) + NEXT_ENTRY_OFFSET;
            isSaved = saveMonthlyDocument(expedient, nextEntry) > NO_VALUE;
        } else {
            isSaved = replaceOrInsertDocument(expedient);
        }
        return isSaved;
    }

    private int saveMonthlyDocument(Expedient expedient, int entryNumber) throws OperationException {
        int generatedId = NO_GENERATED_ID;
        String expedientQuery = "INSERT INTO Expediente "
                              + "(nombre, url, matricula, idTipoDocumento, numeroEntrega) "
                              + "VALUES (?, ?, ?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(expedientQuery,
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getUrl());
            preparedStatement.setString(3, expedient.getIdStudent());
            preparedStatement.setInt(4, expedient.getIdTypeDocument());
            preparedStatement.setInt(5, entryNumber);

            if (preparedStatement.executeUpdate() > NO_VALUE) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar el reporte mensual en el expediente", e);
            throw new OperationException("Error al guardar el documento en el expediente.", e);
        }
        return generatedId;
    }

    private boolean replaceOrInsertDocument(Expedient expedient) throws OperationException {
        Optional<String> previousUrl = getDocumentUrl(expedient.getIdStudent(), expedient.getIdTypeDocument());
        boolean isSaved;

        if (previousUrl.isPresent()) {
            isSaved = replaceDocument(expedient);
            if (isSaved && !previousUrl.get().equals(expedient.getUrl())) {
                FileManager.deleteFile(previousUrl.get());
            }
        } else {
            isSaved = saveDocument(expedient) > NO_VALUE;
        }
        return isSaved;
    }


    @Override
    public int countDocumentsByStudentAndType(String studentId, int typeId) throws OperationException {
        int count = 0;
        String documentQuery = "SELECT COUNT(*) FROM Expediente "
                             + "WHERE matricula = ? AND idTipoDocumento = ?";

        try (Connection databaseConnection = connectionManager.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(documentQuery)) {

            preparedStatement.setString(1, studentId);
            preparedStatement.setInt(2, typeId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar documentos por tipo", e);
            throw new OperationException("Error al verificar documentos subidos", e);
        }
        return count;
    }
}