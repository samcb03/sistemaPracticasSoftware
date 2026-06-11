package uv.lis.logic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import uv.lis.logic.exceptions.OperationException;

/**
 * Guards the application against unsafe or malformed uploads before any
 * file is persisted or processed.
 *
 * Validation is layered intentionally: size is checked first because it
 * is the cheapest guard, then extension and magic bytes together to prevent
 * content spoofing, and finally the file name to block characters that could
 * cause issues in the file system or in path construction.
 */
public class FileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024;
    public static final String EXTENSION_PDF = ".pdf";

    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
        EXTENSION_PDF, new byte[] {0x25, 0x50, 0x44, 0x46}
    );

    /**
     * Validates that a file meets all requirements to be accepted by the system.
     *
     * @param file the candidate file to validate
     * 
     * @throws OperationException if the file violates any of the enforced constraints
     */
    public static void validateFile(File file) throws OperationException {
        validateSize(file);
        validateExtension(file);
        validateMagicBytes(file);
        validateFileName(file);
    }

    /**
     * Verifies that the file does not exceed the maximum allowed size.
     *
     * @param file the file to evaluate
     * 
     * @throws OperationException if the file exceeds 20 MB
     */
    private static void validateSize(File file) throws OperationException {
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            throw new OperationException("El documento no debe pasar de los 20MB", null);
        }
    }

    /**
     * Verifies that the file has a PDF extension.
     *
     * @param file the file to evaluate
     * 
     * @throws OperationException if the extension is not .pdf
     */
    private static void validateExtension(File file) throws OperationException {
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(EXTENSION_PDF)) {
            throw new OperationException("Solo se permiten documentos PDF", null);
        }
    }

    /**
     * Verifies that the file content matches the PDF format,
     * regardless of its declared extension.
     *
     * @param file the file to evaluate
     * 
     * @throws OperationException if the content does not correspond to a PDF
     *                            or the file cannot be read
     */
    private static void validateMagicBytes(File file) throws OperationException {
        byte[] expectedMagicBytes = MAGIC_BYTES.get(EXTENSION_PDF);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] actualMagicBytes = new byte[expectedMagicBytes.length];
            fileInputStream.read(actualMagicBytes);
            if (!Arrays.equals(actualMagicBytes, expectedMagicBytes)) {
                throw new OperationException("El documento no coincide con el tipo declarado", null);
            }
        } catch (IOException e) {
            throw new OperationException("Error al leer el archivo", e);
        }
    }

    /**
     * Verifies that the file name contains only permitted characters.
     *
     * @param file the file to evaluate
     * 
     * @throws OperationException if the name contains characters outside
     *                            the permitted set
     */
    private static void validateFileName(File file) throws OperationException {
        String fileName = file.getName();
        if (!fileName.matches("[a-zA-Z0-9._\\-\\sñÑáéíóúÁÉÍÓÚ()]+")) {
            throw new OperationException("El nombre del archivo contiene caracteres no permitidos",
                null);
        }
    }
}