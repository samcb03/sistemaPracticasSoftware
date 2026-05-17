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
        
        Path baseAbsoluta = Paths.get(BASE_PATH,idStudent).toAbsolutePath().normalize();
        Path targetAbsoluta = baseAbsoluta.resolve(idStudent + "_" + file.getName()).normalize();
        
        if (!targetAbsoluta.startsWith(baseAbsoluta)) {
            throw new OperationException("Ruta de archivo no permitida", null);
        } else {
            try {
                Files.copy(file.toPath(), targetAbsoluta, StandardCopyOption.REPLACE_EXISTING);
                return targetAbsoluta.toString();
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