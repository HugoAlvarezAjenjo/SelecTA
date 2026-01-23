package es.hugoalvarezajenjo.selecta.services.storage;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {

    private final String storagePath = "file-storage/subject_resources/"; // Relative to execution dir

    public LocalStorageService() {
        // Create the folder if it doesn't exist
        final File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public String uploadFile(final String fileName, final InputStream inputStream, final long size,
            final String contentType) {
        try {
            final Path targetLocation = Paths.get(storagePath).resolve(fileName);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName; // Return logical path (filename)
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public InputStream downloadFile(final String filePath) {
        try {
            final Path file = Paths.get(storagePath).resolve(filePath);
            return new FileInputStream(file.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(final String filePath) {
        try {
            final Path file = Paths.get(storagePath).resolve(filePath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file", e);
        }
    }

    @Override
    public boolean fileExists(final String filePath) {
        final Path file = Paths.get(storagePath).resolve(filePath);
        return Files.exists(file);
    }
}