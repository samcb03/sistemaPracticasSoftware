package uv.lis.logic.contracts;


import java.util.List;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;


public interface IExpedientDAO {
    int saveDocument(Expedient expedient) throws OperationException;

    List<Expedient> getAllDocuments() throws OperationException;
}
