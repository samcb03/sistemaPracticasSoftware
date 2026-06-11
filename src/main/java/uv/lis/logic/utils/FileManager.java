package uv.lis.logic.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import uv.lis.logic.exceptions.OperationException;

/**
 * Centralizes file storage for student records so that no other layer
 * needs to know the directory structure or naming conventions.
 *
 * Files are isolated per student to avoid name collisions across records.
 * The path traversal check ensures a maliciously crafted file name cannot
 * escape the student's subdirectory.
 */
public class FileManager {

    private static final String BASE_PATH = "Expediente";

    /**
     * Persists a file under the student's dedicated directory, guaranteeing
     * that the stored copy is always reachable via the returned path and that
     * no file from one student can overwrite or access files from another.
     *
     * The directory is created on demand so callers are not responsible
     * for setting up the file system before invoking this method.
     *
     * An existing file with the same name is silently replaced, which
     * allows re-uploading a corrected document without extra cleanup steps.
     *
     * @param file the source file to persist
     * 
     * @param idStudent the student owner; determines both the subdirectory
     *                  and the filename prefix to prevent cross-student collisions
     * 
     * @return the absolute path where the file was stored, ready to be saved
     *         as a reference in the database
     * 
     * @throws OperationException if the file cannot be stored for any reason,
     *                            including a path that would escape the base directory
     */
    public static String saveFile(File file, String idStudent) throws OperationException {

        File studentDirectory = new File(BASE_PATH, idStudent);
        if (!studentDirectory.exists()) {
            boolean created = studentDirectory.mkdirs();
            if (!created) {
                throw new OperationException("No se pudo crear el directorio", null);
            }
        }

        Path absoluteBase   = Paths.get(BASE_PATH, idStudent).toAbsolutePath().normalize();
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

    /**
     * Removes the physical file when its database record is deleted,
     * keeping the file system consistent with the application state.
     *
     * If the file is already absent, nothing happens — the operation is
     * idempotent so it is safe to call even after a partial deletion or a
     * failed previous attempt.
     *
     * @param url the path previously returned by {@link #saveFile}
     */
    public static void deleteFile(String url) {
        File file = new File(url);
        if (file.exists()) {
            file.delete();
        }
    }
}