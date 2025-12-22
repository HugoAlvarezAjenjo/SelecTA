package es.hugoalvarezajenjo.selecta.services.storage;

import java.io.InputStream;

public interface StorageService {
    String uploadFile(String fileName, InputStream inputStream, long size, String contentType);
    InputStream downloadFile(String filePath);
    void deleteFile(String filePath);
    boolean fileExists(String filePath);
}
