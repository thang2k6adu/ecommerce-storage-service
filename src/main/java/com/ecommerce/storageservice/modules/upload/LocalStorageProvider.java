package com.ecommerce.storageservice.modules.upload;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.ecommerce.storageservice.common.exception.ResourceNotFoundException;
import com.ecommerce.storageservice.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
@RequiredArgsConstructor
@Slf4j
public class LocalStorageProvider implements StorageProvider {

    private final StorageProperties storageProperties;

    @Override
    public StoredFile upload(MultipartFile file, String objectKey) {
        try {
            Path root = getRootPath();
            Path destination = root.resolve(objectKey).normalize();
            if (!destination.startsWith(root)) {
                throw new BadRequestException("Invalid storage path");
            }
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            String url = UploadUrls.joinPublic(storageProperties.getLocal().getPublicBaseUrl(), objectKey);
            log.info("Local upload successful: {}", url);
            return new StoredFile(url, objectKey);
        } catch (IOException e) {
            log.error("Local upload failed: ", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Path path = resolveExisting(objectKey);
            Files.deleteIfExists(path);
            log.info("Local delete successful: {}", objectKey);
        } catch (IOException e) {
            log.error("Local delete failed: ", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    public Resource loadAsResource(String pathAfterUploadsPrefix) {
        String objectKey = "uploads/" + pathAfterUploadsPrefix.replace('\\', '/').replaceAll("^/+", "");
        Path path = resolveExisting(objectKey);
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
