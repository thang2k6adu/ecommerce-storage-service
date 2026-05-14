package com.ecommerce.storageservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private Vendor provider = Vendor.LOCAL;

    private String allowedExtensions = "jpg,jpeg,png,webp,gif,pdf";

    private long maxSize = 10 * 1024 * 1024L;

    private final Cloudinary cloudinary = new Cloudinary();

    private final Local local = new Local();

    public enum Vendor {
        LOCAL,
        CLOUDINARY
    }

    @Getter
    @Setter
    public static class Cloudinary {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }

    @Getter
    @Setter
    public static class Local {
        private String rootDir = "./data/storage";
        private String publicBaseUrl = "http://localhost:8087";
    }
}
