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

    private static final String TEST_STUDENT_ID = "S23013127_test";
    private File sourceFile;
    private String savedFilePath;

    @BeforeEach
    void setUp() throws IOException {
        sourceFile = File.createTempFile("documento_prueba", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(new byte[]{0x25, 0x50, 0x44, 0x46});
            fos.write(new byte[100]);
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

        Path studentDirectory = Paths.get("Expediente", TEST_STUDENT_ID);
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
            () -> FileManager.deleteFile("ruta/que/no/existe/archivo.pdf"));
    }
}