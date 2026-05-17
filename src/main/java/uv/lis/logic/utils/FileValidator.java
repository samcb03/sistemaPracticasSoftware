package uv.lis.logic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import uv.lis.logic.exceptions.OperationException;

public class FileValidator {
    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 *1024;
    public static final String EXTENSION_PDF = ".pdf";
    private static final Map<String,byte[]> MAGIC_BYTES = Map.of(
        EXTENSION_PDF, new byte[] {0x25, 0x50, 0x44, 0x46}
    );
    
    public static void validateFile(File file) throws OperationException {
        validateSize(file);
        validateExtension(file);
        validateMagicBytes(file);
        validateFileName(file);
    }

    private static void validateSize(File file) throws OperationException {
        if(file.length() > MAX_FILE_SIZE_BYTES) {
            throw new OperationException("El documento no debe pasar de los 20MB", null);
        }
    }

    private static void validateExtension(File file) throws OperationException {
        String fileName = file.getName().toLowerCase();
        if(!fileName.endsWith(EXTENSION_PDF)) {
            throw new OperationException("Solo se permiten documentos PDF", null);
        }
    }

    private static void validateMagicBytes(File file) throws OperationException {
        byte[] expectedMagicBytes = MAGIC_BYTES.get(EXTENSION_PDF);
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] actualMagicBytes = new byte[expectedMagicBytes.length];
            fileInputStream.read(actualMagicBytes);
            if(!Arrays.equals(actualMagicBytes,expectedMagicBytes)) {
                throw new OperationException("El documento no coincide con el tipo declarado", null);
            }
        } catch(IOException e) {
            throw new OperationException("Error al leer el archivo", e);
        }
    }

    private static void validateFileName(File file) throws OperationException {
        String fileName = file.getName();
        if(!fileName.matches("[a-zA-Z0-9._\\-\\sñÑáéíóúÁÉÍÓÚ()]+")) {
            throw new OperationException("El nombre del archivo contiene caracteres no permitidos", 
                null);
        }
    }
}
