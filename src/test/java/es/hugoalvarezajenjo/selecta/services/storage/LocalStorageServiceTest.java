package es.hugoalvarezajenjo.selecta.services.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService(tempDir.toString());
    }

    @Test
    void uploadFile_shouldSaveFileToDisk() throws IOException {
        String fileName = "test.txt";
        String content = "Hello World";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String savedName = localStorageService.uploadFile(fileName, inputStream, content.length(), "text/plain");

        assertEquals(fileName, savedName);
        assertTrue(tempDir.resolve(fileName).toFile().exists());
        assertEquals(content, Files.readString(tempDir.resolve(fileName)));
    }

    @Test
    void downloadFile_shouldReturnInputStream() throws IOException {
        String fileName = "download.txt";
        String content = "Download Content";
        java.nio.file.Files.writeString(tempDir.resolve(fileName), content);

        try (InputStream inputStream = localStorageService.downloadFile(fileName)) {
            assertNotNull(inputStream);
            String downloadedContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, downloadedContent);
        }
    }

    @Test
    void deleteFile_shouldRemoveFileFromDisk() throws IOException {
        String fileName = "delete.txt";
        java.nio.file.Files.createFile(tempDir.resolve(fileName));

        localStorageService.deleteFile(fileName);

        assertFalse(tempDir.resolve(fileName).toFile().exists());
    }

    @Test
    void fileExists_shouldReturnCorrectStatus() throws IOException {
        String fileName = "exists.txt";

        assertFalse(localStorageService.fileExists(fileName));

        java.nio.file.Files.createFile(tempDir.resolve(fileName));

        assertTrue(localStorageService.fileExists(fileName));
    }
}
