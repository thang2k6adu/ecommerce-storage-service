package com.ecommerce.storageservice.modules.storage;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.ecommerce.storageservice.config.StorageProperties;
import com.ecommerce.storageservice.modules.storage.backend.StorageBackend;
import com.ecommerce.storageservice.modules.storage.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageBackend storageBackend;
    private final StorageProperties storageProperties;

    public UploadResponse upload(MultipartFile file, String folder) {
        validateFile(file);
        String safeFolder = sanitizeFolder(folder);
        return storageBackend.upload(file, safeFolder);
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.contains("..")) {
            throw new BadRequestException("Invalid object key");
        }
        storageBackend.delete(objectKey.trim());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new BadRequestException("File exceeds maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        Set<String> allowed = Arrays.stream(storageProperties.getAllowedExtensions().split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (extension.isEmpty() || !allowed.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("File type not allowed. Allowed: " + storageProperties.getAllowedExtensions());
        }
    }

    private static String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    private static String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "uploads";
        }
        String f = folder.replace('\\', '/').trim();
        if (f.contains("..") || f.startsWith("/")) {
            throw new BadRequestException("Invalid folder");
        }
        return f.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
