package com.nye.backend.upload;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Upload de la photo d'une alerte (Nyé-F2). Stockage local sur disque en dev — à remplacer par
 * un vrai service de stockage objet (S3-compatible) avant la mise en production (§13.5).
 */
@RestController
@RequestMapping("/api/uploads")
@Slf4j
@Tag(name = "Uploads")
public class UploadController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 Mo

    private final Path uploadDir;
    private final String baseUrl;

    public UploadController(
            @Value("${nye.upload.dir}") String uploadDir,
            @Value("${nye.upload.base-url}") String baseUrl) throws IOException {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        Files.createDirectories(this.uploadDir);
    }

    @PostMapping("/photo")
    public ResponseEntity<UploadResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux (max 5 Mo)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Format non supporté (JPEG, PNG ou WebP uniquement)");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String filename = UUID.randomUUID() + extension;

        try {
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
            }
            file.transferTo(target);
        } catch (IOException ex) {
            log.error("Échec de l'enregistrement du fichier uploadé : {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de l'enregistrement du fichier");
        }

        String url = baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponse(url));
    }
}
