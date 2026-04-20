package uv.lis.logic.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import uv.lis.logic.exceptions.OperationException;


public class FileManager {
    
    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    public String uploadDocument(String documentName, String documentType, File sourceFile, String idStudent) throws OperationException {
        
        if (sourceFile == null || !sourceFile.exists()) {
            throw new OperationException("El archivo de origen no existe o es nulo.", null);
        }
        
        try {
            Path userDir = Paths.get("src", "main", "resources", "expedientes", idStudent);
            Files.createDirectories(userDir);

            Path destination = userDir.resolve(documentType + ".pdf");
            
            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            
            return destination.toString();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error subiendo el documento para el alumno " + idStudent, e);
            throw new OperationException("No se pudo guardar el archivo ", e);
        }
    }
}