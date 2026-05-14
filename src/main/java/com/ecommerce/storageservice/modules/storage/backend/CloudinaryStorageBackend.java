package com.ecommerce.storageservice.modules.storage.backend;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.ecommerce.storageservice.config.StorageProperties;
import com.ecommerce.storageservice.modules.storage.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageBackend implements StorageBackend {

    private final Cloudinary cloudinary;
    private final StorageProperties storageProperties;

    @Override
    @SuppressWarnings("unchecked")
    public UploadResponse upload(MultipartFile file, String folder) {
        try {
            String cloudinaryFolder = Stream.of(
                            storageProperties.getCloudinary().getFolder(),
                            folder)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining("/"));

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", cloudinaryFolder,
                            "resource_type", "auto"
                    ));

            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            log.info("Cloudinary upload successful: {}", url);
            return UploadResponse.builder()
                    .url(url)
                    .objectKey(publicId)
                    .provider(StorageProperties.Provider.CLOUDINARY.name().toLowerCase())
                    .build();
        } catch (Exception e) {
            log.error("Error uploading to Cloudinary: ", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            cloudinary.uploader().destroy(objectKey, ObjectUtils.asMap("resource_type", "auto"));
            log.info("Cloudinary delete successful: {}", objectKey);
        } catch (Exception e) {
            log.error("Error deleting from Cloudinary: ", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }
}
