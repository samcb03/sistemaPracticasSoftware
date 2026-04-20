package uv.lis.logic.common;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.logic.contracts.IExpedientDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileManager;


public class ExpedientCommon {

    private static final Logger LOGGER = Logger.getLogger(ExpedientCommon.class.getName());

    private IExpedientDAO expedientDAO;
    private FileManager fileManager;

    public ExpedientCommon() {
        this.expedientDAO = new ExpedientDAO();
        this.fileManager = new FileManager();
    }

    public boolean uploadAndRegisterDocument(String documentName, String documentType,
        File sourceFile,String idStudent) throws OperationException {

        if (idStudent == null || idStudent.trim().isEmpty()) {
            throw new OperationException("La matrícula del alumno es obligatoria para guardar un documento.", 
                null);
        }
        if (sourceFile == null || !sourceFile.exists()) {
            throw new OperationException("No se ha seleccionado un archivo válido.", null);
    }

        try {
            String finalUrl = fileManager.uploadDocument(documentName, documentType,
            sourceFile,idStudent);
            
            Expedient newDocument = new Expedient(documentName, documentType, finalUrl,idStudent);

            int generatedId = expedientDAO.saveDocument(newDocument);

            return generatedId > 0;

        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error en el proceso de subir y registrar el documento para {0}", idStudent);
            throw e; 
        }
    }
}