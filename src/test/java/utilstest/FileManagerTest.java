package utilstest;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileManager;

class FileManagerTest {

    private static final String TEST_STUDENT_ID          = "S23013127_test";
    private static final String NON_EXISTING_FILE_PATH   = "ruta/que/no/existe/archivo.pdf";
    private static final String TEMP_FILE_PREFIX         = "documento_prueba";
    private static final String TEMP_FILE_SUFFIX         = ".pdf";
    private static final String STUDENT_DIRECTORY_ROOT   = "Expediente";
    private static final byte[] PDF_HEADER               = {0x25, 0x50, 0x44, 0x46};
    private static final byte[] PDF_CONTENT              = new byte[100];

    private File sourceFile;
    private String savedFilePath;

    @BeforeEach
    void setUp() throws IOException {
        sourceFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(PDF_HEADER);
            fos.write(PDF_CONTENT);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (sourceFile != null) {
            sourceFile.delete();
        }
        if (savedFilePath != null) {
            new File(savedFilePath).delete();
        }

        Path studentDirectory = Paths.get(STUDENT_DIRECTORY_ROOT, TEST_STUDENT_ID);
        if (Files.exists(studentDirectory)) {
            Files.walk(studentDirectory)
                .map(Path::toFile)
                .forEach(File::delete);
            studentDirectory.toFile().delete();
        }
    }

    @Test
    void saveFile_validFile_returnsNonNullPath() throws OperationException {
        savedFilePath = FileManager.saveFile(sourceFile, TEST_STUDENT_ID);

        assertNotNull(savedFilePath);
    }

    @Test
    void saveFile_validFile_createsFileOnDisk() throws OperationException {
        savedFilePath = FileManager.saveFile(sourceFile, TEST_STUDENT_ID);

        assertTrue(new File(savedFilePath).exists());
    }

    @Test
    void saveFile_validFile_pathContainsStudentId() throws OperationException {
        savedFilePath = FileManager.saveFile(sourceFile, TEST_STUDENT_ID);

        assertTrue(savedFilePath.contains(TEST_STUDENT_ID));
    }

    @Test
    void deleteFile_existingFile_deletesSuccessfully() throws OperationException {
        savedFilePath = FileManager.saveFile(sourceFile, TEST_STUDENT_ID);

        FileManager.deleteFile(savedFilePath);

        assertFalse(new File(savedFilePath).exists());
    }

    @Test
    void deleteFile_nonExistingFile_doesNotThrow() {
        assertDoesNotThrow(
            () -> FileManager.deleteFile(NON_EXISTING_FILE_PATH));
    }
}