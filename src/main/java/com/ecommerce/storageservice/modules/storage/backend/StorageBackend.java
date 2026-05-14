package com.ecommerce.storageservice.modules.storage.backend;

import com.ecommerce.storageservice.modules.storage.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StorageBackend {

    UploadResponse upload(MultipartFile file, String folder);

    void delete(String objectKey);
}
