package uv.lis.logic.services;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import uv.lis.logic.contracts.IExpedientDAO;
import uv.lis.logic.dao.ExpedientDAO;
import uv.lis.logic.dto.Expedient;
import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileManager;

public class ExpedientService {

    private static final Logger LOGGER = Logger.getLogger(ExpedientService.class.getName());

    private IExpedientDAO expedientDAO;
    private FileManager fileManager;

    public ExpedientService() {
        this.expedientDAO = new ExpedientDAO();
        this.fileManager = new FileManager();
    }

    public boolean uploadAndRegisterDocument(String enrollment, String documentName, String documentType, File sourceFile) throws OperationException {
        if (enrollment == null || enrollment.trim().isEmpty()) {
            throw new OperationException("La matrícula del alumno es obligatoria para guardar un documento.", null);
        }
        if (sourceFile == null || !sourceFile.exists()) {
            throw new OperationException("No se ha seleccionado un archivo válido.", null);
        }

        try {
            String finalUrl = fileManager.uploadDocument(enrollment, documentType, sourceFile);
            
            Expedient newDocument = new Expedient(0, documentName, documentType, finalUrl);
            
            int generatedId = expedientDAO.saveDocument(newDocument);

            return generatedId > 0;

        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error en el proceso de subir y registrar el documento para {0}", enrollment);
            throw e; 
        }
    }

    public List<Expedient> getAllDocuments() throws OperationException {
        try {
            return expedientDAO.getAllDocuments();
        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de expedientes", e);
            throw e;
        }
    }
}