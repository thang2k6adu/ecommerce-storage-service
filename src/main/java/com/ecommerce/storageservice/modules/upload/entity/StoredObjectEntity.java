package com.ecommerce.storageservice.modules.upload.entity;

import com.ecommerce.storageservice.config.StorageProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_objects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredObjectEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "object_key", nullable = false, unique = true, length = 768)
    private String objectKey;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StorageProperties.Vendor provider;

    @Column(name = "original_filename", length = 512)
    private String originalFilename;

    @Column(name = "content_type", length = 256)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
