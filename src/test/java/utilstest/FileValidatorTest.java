package utilstest;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uv.lis.logic.exceptions.OperationException;
import uv.lis.logic.utils.FileValidator;

class FileValidatorTest {

    private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46};
    private static final byte[] INVALID_MAGIC_BYTES = {0x00, 0x00, 0x00, 0x00};
    private static final int MAX_SIZE_MB = 21;
    private static final int BYTES_PER_MB = 1024 * 1024;
    private static final String INVALID_FILE_NAME   = "archivo<invalido>.pdf";

    private File validPdfFile;
    private File oversizedFile;
    private File nonPdfFile;
    private File invalidMagicBytesFile;
    private File invalidNameFile;

    @BeforeEach
    void setUp() throws IOException {
        validPdfFile = File.createTempFile("documento_valido", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(validPdfFile)) {
            fos.write(PDF_MAGIC_BYTES);
            fos.write(new byte[100]);
        }

        nonPdfFile = File.createTempFile("documento", ".txt");

        invalidMagicBytesFile = File.createTempFile("falso_pdf", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(invalidMagicBytesFile)) {
            fos.write(INVALID_MAGIC_BYTES);
        }

        invalidNameFile = new File(System.getProperty("java.io.tmpdir"),
            INVALID_FILE_NAME);
    }

    @AfterEach
    void tearDown() {
        if (validPdfFile != null) { 
            validPdfFile.delete(); 
        }
        if (nonPdfFile != null) { 
            nonPdfFile.delete(); 
        }
        if (invalidMagicBytesFile != null) { 
            invalidMagicBytesFile.delete(); 
        }
        if (invalidNameFile != null) { 
            invalidNameFile.delete(); 
        }
        if (oversizedFile != null) { 
            oversizedFile.delete(); 
        }
    }

    @Test
    void validateFile_validPdf_doesNotThrow() {
        assertDoesNotThrow(() -> FileValidator.validateFile(validPdfFile));
    }

    @Test
    void validateFile_nonPdfExtension_throwsOperationException() {
        assertThrows(OperationException.class,
            () -> FileValidator.validateFile(nonPdfFile));
    }

    @Test
    void validateFile_invalidMagicBytes_throwsOperationException() {
        assertThrows(OperationException.class,
            () -> FileValidator.validateFile(invalidMagicBytesFile));
    }

    @Test
    void validateFile_invalidFileName_throwsOperationException() {
        assertThrows(OperationException.class,
            () -> FileValidator.validateFile(invalidNameFile));
    }

    @Test
    void validateFile_oversizedFile_throwsOperationException() throws IOException {
        oversizedFile = File.createTempFile("archivo_grande", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(oversizedFile)) {
            fos.write(PDF_MAGIC_BYTES);
            byte[] chunk = new byte[BYTES_PER_MB];
            for (int i = 0; i < MAX_SIZE_MB; i++) {
                fos.write(chunk);
            }
        }

        assertThrows(OperationException.class,
            () -> FileValidator.validateFile(oversizedFile));
    }
}