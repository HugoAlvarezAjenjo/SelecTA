package es.hugoalvarezajenjo.selecta.services.storage;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {

    private final String storagePath = "./file-storage/"; // Carpeta en tu proyecto

    public LocalStorageService() {
        // Crear la carpeta si no existe
        new File(storagePath).mkdirs();
    }

    @Override
    public String uploadFile(String fileName, InputStream inputStream, long size, String contentType) {
        try {
            String filePath = storagePath + fileName;
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            return filePath; // Devuelve la ruta donde se guardó
        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo", e);
        }
    }

    @Override
    public InputStream downloadFile(String filePath) {
        final String path = storagePath + filePath;
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Archivo no encontrado: " + path, e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar archivo", e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(this.storagePath + filePath));
    }
}