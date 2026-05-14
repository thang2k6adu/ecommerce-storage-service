package com.ecommerce.storageservice.modules.storage.backend;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.ecommerce.storageservice.common.exception.ResourceNotFoundException;
import com.ecommerce.storageservice.config.StorageProperties;
import com.ecommerce.storageservice.modules.storage.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class LocalStorageBackend implements StorageBackend {

    private final StorageProperties storageProperties;

    @Override
    public UploadResponse upload(MultipartFile file, String folder) {
        try {
            Path root = getRootPath();
            Path dir = root.resolve(folder).normalize();
            if (!dir.startsWith(root)) {
                throw new BadRequestException("Invalid storage path");
            }
            Files.createDirectories(dir);

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID() + extension;
            Path destination = dir.resolve(filename).normalize();
            if (!destination.startsWith(root)) {
                throw new BadRequestException("Invalid storage path");
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String relative = root.relativize(destination).toString().replace('\\', '/');
            String baseUrl = storageProperties.getLocal().getPublicBaseUrl().replaceAll("/+$", "");
            String url = baseUrl + "/" + relative;

            log.info("Local file stored: {}", url);
            return UploadResponse.builder()
                    .url(url)
                    .objectKey(relative)
                    .provider(StorageProperties.Provider.LOCAL.name().toLowerCase())
                    .build();
        } catch (IOException e) {
            log.error("Error saving file locally: ", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Path path = resolveExisting(objectKey);
            Files.deleteIfExists(path);
            log.info("Local file deleted: {}", objectKey);
        } catch (IOException e) {
            log.error("Error deleting local file: ", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    public Resource loadAsResource(String relativePath) {
        Path path = resolveExisting(relativePath);
        return new FileSystemResource(path.toFile());
    }

    private Path resolveExisting(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.contains("..")) {
            throw new BadRequestException("Invalid object key");
        }
        String normalizedKey = objectKey.replace('\\', '/').replaceAll("^/+", "");
        Path root = getRootPath();
        Path file = root.resolve(normalizedKey).normalize();
        if (!file.startsWith(root)) {
            throw new BadRequestException("Invalid object key");
        }
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("File", "path", normalizedKey);
        }
        return file;
    }

    private Path getRootPath() {
        try {
            return Paths.get(storageProperties.getLocal().getRootDir()).toAbsolutePath().normalize();
        } catch (Exception e) {
            throw new BadRequestException("Invalid storage root directory");
        }
    }
}
