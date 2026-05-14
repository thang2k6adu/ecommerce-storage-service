package com.ecommerce.storageservice.config;

import com.cloudinary.Cloudinary;
import com.ecommerce.storageservice.modules.storage.backend.CloudinaryStorageBackend;
import com.ecommerce.storageservice.modules.storage.backend.LocalStorageBackend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageBackendConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
    public CloudinaryStorageBackend cloudinaryStorageBackend(
            Cloudinary cloudinary,
            StorageProperties storageProperties) {
        return new CloudinaryStorageBackend(cloudinary, storageProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
    public LocalStorageBackend localStorageBackend(StorageProperties storageProperties) {
        return new LocalStorageBackend(storageProperties);
    }
}
