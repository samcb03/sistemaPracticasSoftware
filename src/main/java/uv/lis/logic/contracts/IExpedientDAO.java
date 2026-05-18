package uv.lis.logic.contracts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;

public interface IExpedientDAO {
    int saveDocument(Expedient expedient) throws OperationException;

    List<Expedient> getAllDocuments() throws OperationException;

    Optional<Integer> getIdDocumentTypeByName(String name) throws OperationException;

    void uploadDocument(String idStudent, String typeDocument, File file) throws OperationException;

    ArrayList<String> getAllDocumentsTypes() throws OperationException;
}
