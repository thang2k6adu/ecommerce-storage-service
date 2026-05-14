package com.ecommerce.storageservice.modules.upload;

import com.ecommerce.storageservice.common.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary")
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageProvider implements StorageProvider {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public StoredFile upload(MultipartFile file, String objectKey) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", objectKey,
                            "resource_type", "auto",
                            "overwrite", false
                    ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Cloudinary upload successful: {}", url);
            return new StoredFile(url, objectKey);
        } catch (Exception e) {
            log.error("Cloudinary upload failed: ", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            cloudinary.uploader().destroy(objectKey, ObjectUtils.asMap("resource_type", "auto"));
            log.info("Cloudinary delete successful: {}", objectKey);
        } catch (Exception e) {
            log.error("Cloudinary delete failed: ", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }
}
