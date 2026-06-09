package uv.lis.logic.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import uv.lis.logic.exceptions.OperationException;

public class FileManager {
    private static final String BASE_PATH = "Expediente";
    
    public static String saveFile(File file, String idStudent) throws OperationException {
        
        File studentDirectory = new File(BASE_PATH, idStudent);
        if(!studentDirectory.exists()) {
           boolean created =  studentDirectory.mkdirs(); 
           if(!created) {
            throw new OperationException("No se pudo crear el directorio",null);
           }
        }
        
        Path absoluteBase = Paths.get(BASE_PATH,idStudent).toAbsolutePath().normalize();
        Path absoluteTarget = absoluteBase.resolve(idStudent + "_" + file.getName()).normalize();
        
        if (!absoluteTarget.startsWith(absoluteBase)) {
            throw new OperationException("Ruta de archivo no permitida", null);
        } else {
            try {
                Files.copy(file.toPath(), absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
                return absoluteTarget.toString();
            } catch (IOException e) {
                throw new OperationException("Error al guardar el archivo en el servidor", e);
            }
        }
    }
    
    public static void deleteFile(String url) {
        File file = new File(url);
        if (file.exists()) {
            file.delete();
        }
    }
}