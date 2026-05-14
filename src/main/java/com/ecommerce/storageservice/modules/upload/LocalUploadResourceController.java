package com.ecommerce.storageservice.modules.upload;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/uploads")
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
@RequiredArgsConstructor
public class LocalUploadResourceController {

    private final LocalStorageProvider localStorageProvider;

    @GetMapping("/{*relativePath}")
    public ResponseEntity<Resource> getFile(@PathVariable("relativePath") String relativePath) {
        if (relativePath == null || relativePath.contains("..")) {
            throw new BadRequestException("Invalid path");
        }
        Resource resource = localStorageProvider.loadAsResource(relativePath);
        try {
            Path path = resource.getFile().toPath();
            String contentType = Files.probeContentType(path);
            String resolvedType = contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, resolvedType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(resource);
        }
    }
}
