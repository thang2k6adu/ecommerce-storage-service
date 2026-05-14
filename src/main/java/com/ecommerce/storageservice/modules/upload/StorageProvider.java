package com.ecommerce.storageservice.modules.upload;

import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {

    StoredFile upload(MultipartFile file, String objectKey);

    void delete(String objectKey);
}
