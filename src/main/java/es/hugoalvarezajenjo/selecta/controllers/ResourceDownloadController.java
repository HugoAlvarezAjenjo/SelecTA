package es.hugoalvarezajenjo.selecta.controllers;

import es.hugoalvarezajenjo.selecta.services.resources.SubjectResource;
import es.hugoalvarezajenjo.selecta.services.resources.SubjectResourceService;
import es.hugoalvarezajenjo.selecta.services.storage.StorageService;
import es.hugoalvarezajenjo.selecta.services.subjects.Subject;
import es.hugoalvarezajenjo.selecta.services.subjects.SubjectService;
import es.hugoalvarezajenjo.selecta.services.user.Teacher;
import es.hugoalvarezajenjo.selecta.services.user.User;
import es.hugoalvarezajenjo.selecta.services.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@Tag(name = "Recursos", description = "Descarga de recursos de asignaturas")
@Controller
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceDownloadController {

    private final StorageService storageService;
    private final SubjectResourceService subjectResourceService;
    private final SubjectService subjectService;
    private final UserRepository userRepository;

    @Operation(summary = "Descargar un recurso",
            description = "Descarga el archivo asociado a un recurso por su ID interno. Recursos privados requieren autenticación.")
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(
            @Parameter(description = "ID del recurso a descargar") final @PathVariable Long resourceId) {
        try {
            final SubjectResource resource = this.subjectResourceService.findById(resourceId);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // Private resources: only teachers of the subject can download
            if (resource.isPrivate()) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                String userEmail = auth.getName();
                User currentUser = userRepository.findByEmail(userEmail).orElse(null);

                if (!(currentUser instanceof Teacher)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                Subject subject = subjectService.getSubjectById(resource.getSubjectId()).orElse(null);
                if (subject == null || !subject.getTeachers().contains(currentUser)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            final String storagePath = resourceId.toString();

            if (!this.storageService.fileExists(storagePath)) {
                return ResponseEntity.notFound().build();
            }

            final InputStream inputStream = this.storageService.downloadFile(storagePath);
            final String filename = resource.getOriginalName();

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
