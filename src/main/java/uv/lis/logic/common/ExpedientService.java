package uv.lis.logic.common;


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

    public boolean uploadAndRegisterDocument(Expedient expedient, File sourceFile) throws OperationException {
        if (expedient.getEnrollment() == null || expedient.getEnrollment().trim().isEmpty()) {
            throw new OperationException("La matrícula del alumno es obligatoria para guardar un documento.", null);
        }
        if (sourceFile == null || !sourceFile.exists()) {
            throw new OperationException("No se ha seleccionado un archivo válido.", null);
        }

        try {
            String finalUrl = fileManager.uploadDocument(
            expedient.getEnrollment(), 
            expedient.getTypeDocument(), 
            sourceFile);

            expedient.setUrl(finalUrl);

            int generatedId = expedientDAO.saveDocument(expedient);

            boolean wasRegistered = generatedId > 0;
            return wasRegistered;

        } catch (OperationException e) {
            LOGGER.log(Level.SEVERE, "Error en el proceso de subir y registrar el documento para {0}", 
                expedient.getEnrollment());
            throw new OperationException("Error en el proceso de subir y registrar el documento", e);
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