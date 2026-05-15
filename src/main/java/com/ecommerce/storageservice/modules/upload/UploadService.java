package com.ecommerce.storageservice.modules.upload;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.ecommerce.storageservice.common.exception.ResourceNotFoundException;
import com.ecommerce.storageservice.config.StorageProperties;
import com.ecommerce.storageservice.modules.upload.dto.UploadCreatedResponse;
import com.ecommerce.storageservice.modules.upload.entity.StoredObjectEntity;
import com.ecommerce.storageservice.modules.upload.repository.StoredObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private static final String OCTET_STREAM = "application/octet-stream";

    private final Tika tika = new Tika();

    private final StorageProvider storageProvider;
    private final StorageProperties storageProperties;
    private final StoredObjectRepository storedObjectRepository;

    @Transactional
    public UploadCreatedResponse upload(MultipartFile file) {
        validateFile(file);
        UUID fileId = UUID.randomUUID();
        String objectKey = buildObjectKey(fileId, file.getOriginalFilename());
        StoredFile stored = storageProvider.upload(file, objectKey);
        try {
            StoredObjectEntity entity = StoredObjectEntity.builder()
                    .id(fileId)
                    .objectKey(stored.objectKey())
                    .url(stored.url())
                    .provider(storageProperties.getProvider())
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();
            storedObjectRepository.save(entity);
        } catch (Exception e) {
            try {
                storageProvider.delete(objectKey);
            } catch (Exception cleanup) {
                log.warn("Failed to roll back uploaded object {}: {}", objectKey, cleanup.getMessage());
            }
            throw e;
        }
        return new UploadCreatedResponse(fileId, stored.url());
    }

    @Transactional
    public void delete(UUID fileId) {
        StoredObjectEntity entity = storedObjectRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
        storageProvider.delete(entity.getObjectKey());
        storedObjectRepository.delete(entity);
    }

    private String buildObjectKey(UUID fileId, String originalFilename) {
        String extension = resolveExtension(originalFilename);
        LocalDate utc = LocalDate.now(ZoneOffset.UTC);
        String datePath = String.format(
                Locale.ROOT,
                "%04d/%02d/%02d",
                utc.getYear(),
                utc.getMonthValue(),
                utc.getDayOfMonth());
        String base = "uploads/" + datePath + "/" + fileId;
        if (storageProperties.getProvider() == StorageProperties.Vendor.CLOUDINARY) {
            return base;
        }
        return base + "." + extension;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new BadRequestException("File exceeds maximum allowed size");
        }
        String extension = resolveExtension(file.getOriginalFilename());
        if (extension.isEmpty()) {
            throw new BadRequestException("Could not determine file extension");
        }
        Set<String> allowedExtensions = csvToLowerSet(storageProperties.getAllowedExtensions());
        if (!allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException(
                    "File type not allowed. Allowed extensions: " + storageProperties.getAllowedExtensions());
        }

        Set<String> allowedMimes = csvToLowerSet(storageProperties.getAllowedMimeTypes());
        if (allowedMimes.isEmpty()) {
            throw new BadRequestException("Server misconfiguration: no allowed MIME types configured");
        }

        String detectedMime;
        try (InputStream in = file.getInputStream()) {
            // Do not pass filename: avoids letting a forged extension influence sniffing.
            detectedMime = tika.detect(in);
        } catch (IOException e) {
            log.warn("Failed to read upload for content sniffing", e);
            throw new BadRequestException("Could not validate file content");
        }

        String normalizedDetected = normalizeMimeType(detectedMime);
        if (normalizedDetected.isEmpty() || !allowedMimes.contains(normalizedDetected)) {
            throw new BadRequestException("File content does not match an allowed type (magic-byte check failed)");
        }

        String declared = normalizeMimeType(file.getContentType());
        if (!declared.isEmpty()) {
            if (!allowedMimes.contains(declared)) {
                throw new BadRequestException("Declared content type is not allowed");
            }
            if (!OCTET_STREAM.equals(declared) && !declared.equals(normalizedDetected)) {
                throw new BadRequestException("Declared content type does not match actual file contents");
            }
        }
        // Optional: virus scan (e.g. ClamAV) after structural checks pass.
    }

    private static Set<String> csvToLowerSet(String csv) {
        return Arrays.stream(csv.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static String normalizeMimeType(String mime) {
        if (mime == null) {
            return "";
        }
        int semi = mime.indexOf(';');
        String base = semi >= 0 ? mime.substring(0, semi) : mime;
        return base.trim().toLowerCase(Locale.ROOT);
    }

    private static String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
}
