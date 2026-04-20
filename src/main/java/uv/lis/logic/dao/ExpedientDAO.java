package uv.lis.logic.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import uv.lis.logic.contracts.IExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.dataaccess.MySQLConnectionManager;


public class ExpedientDAO implements IExpedientDAO {
    private static final int NO_ROWS_AFFECTED = 0;
    private MySQLConnectionManager connectionManager;

    public ExpedientDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public int saveDocument(Expedient expedient) throws OperationException {
        int generatedId = -1;
        String query = "INSERT INTO expediente (nombre, tipo_documento, direccion_archivo) VALUES (?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query, 
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, expedient.getName());
            preparedStatement.setString(2, expedient.getTypeDocument()); 
            preparedStatement.setString(3, expedient.getUrl()); 
            
            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > NO_ROWS_AFFECTED) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationException("Error de SQL al guardar el documento en el expediente.", e);
        }
        
        return generatedId;
    }

    @Override
    public List<Expedient> getAllDocuments() throws OperationException {
        List<Expedient> documents = new ArrayList<>();
        String query = "SELECT id, nombre, tipo_documento, direccion_archivo FROM expediente";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
             
            while (resultSet.next()) {
                Expedient expedient = new Expedient(
                    resultSet.getString("nombre"),
                    resultSet.getString("tipo_documento"),
                    resultSet.getString("direccion_archivo")
                );
                documents.add(expedient);
            }
        } catch (SQLException e) {
            throw new OperationException("Error al recuperar los documentos.", e);
        }
        
        return documents;
    }
}
