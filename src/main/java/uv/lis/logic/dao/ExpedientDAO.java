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
    private MySQLConnectionManager connectionManager;

    public ExpedientDAO() {
        this.connectionManager = new MySQLConnectionManager();
    }

    @Override
    public int saveDocument(Expedient expedient) throws OperationException {
        int generatedId = -1;
        String query = "INSERT INTO expediente (nombre, tipo_documento, direccion_archivo) VALUES (?, ?, ?)";

        try (Connection databaseConnection = connectionManager.getConnection();
             PreparedStatement ps = databaseConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, expedient.getName());
            ps.setString(2, expedient.getTypeDocument()); 
            ps.setString(3, expedient.getUrl()); 
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
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
             PreparedStatement ps = databaseConnection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Expedient exp = new Expedient(
                    rs.getInt("id"), 
                    rs.getString("nombre"),
                    rs.getString("tipo_documento"),
                    rs.getString("direccion_archivo"),
                    rs.getString("idStudent")
                );
                documents.add(exp);
            }
        } catch (SQLException e) {
            throw new OperationException("Error de SQL al recuperar los documentos.", e);
        }
        
        return documents;
    }
}
