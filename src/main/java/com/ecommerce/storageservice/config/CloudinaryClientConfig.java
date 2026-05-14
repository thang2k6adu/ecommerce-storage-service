package com.ecommerce.storageservice.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryClientConfig {

    @Bean
    public Cloudinary cloudinary(StorageProperties storageProperties) {
        StorageProperties.Cloudinary c = storageProperties.getCloudinary();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", c.getCloudName());
        config.put("api_key", c.getApiKey());
        config.put("api_secret", c.getApiSecret());
        return new Cloudinary(config);
    }
}
