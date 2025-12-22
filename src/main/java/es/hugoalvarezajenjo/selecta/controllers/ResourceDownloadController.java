package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@Controller
@RequestMapping("/download")
@RequiredArgsConstructor
public class ResourceDownloadController {

    private final StorageService storageService;
    private final SubjectResourceService subjectResourceService;

    /**
     * Download a resource by its internal ID (secure - doesn't expose file paths)
     * 
     * @param resourceId The internal database ID of the resource
     * @return The file as a streaming response
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<StreamingResponseBody> downloadResource(final @PathVariable Long resourceId) {
        try {
            // Fetch resource metadata by ID
            final SubjectResource resource = this.subjectResourceService.findById(resourceId);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // Get the storage path from the resource (stored in the 'url' field)
            final String storagePath = resource.getUrl();

            // Verify file exists
            if (!this.storageService.fileExists(storagePath)) {
                return ResponseEntity.notFound().build();
            }

            final InputStream inputStream = this.storageService.downloadFile(storagePath);

            // Use the resource name from database for the filename
            final String filename = resource.getName();

            final StreamingResponseBody streamingBody = outputStream -> {
                int bytesRead;
                byte[] buffer = new byte[8192];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(streamingBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}