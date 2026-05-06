package uv.lis.logic.utils;

import static uv.lis.logic.utils.FileValidator.EXTENSION_PDF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import uv.lis.logic.exceptions.OperationException;

public class FileManager {
    private static final String BASE_PATH = "Expediente";
    
    public static String saveFile(File file, String idStudent) throws OperationException {
        String savedFilePath = null;
        
        File directory = new File(BASE_PATH);
        if(!directory.exists()) {
            directory.mkdirs(); 
        }

        String fileName = idStudent + "_" + UUID.randomUUID() + EXTENSION_PDF;
        
        Path baseAbsoluta = Paths.get(BASE_PATH).toAbsolutePath().normalize();
        
        Path targetAbsoluta = baseAbsoluta.resolve(fileName).normalize();
        
        if (!targetAbsoluta.startsWith(baseAbsoluta)) {
            throw new OperationException("Ruta de archivo no permitida", null);
        } else {
            try {
                Files.copy(file.toPath(), targetAbsoluta, StandardCopyOption.REPLACE_EXISTING);
                savedFilePath = targetAbsoluta.toString();
            } catch (IOException e) {
                throw new OperationException("Error al guardar el archivo en el servidor", e);
            }
        }
        
        return savedFilePath;
    }

    public static void deleteFile(String url) {
        File file = new File(url);
        if (file.exists()) {
            file.delete();
        }
    }
}