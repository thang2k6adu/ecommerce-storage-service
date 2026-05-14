package com.ecommerce.storageservice.modules.upload.repository;

import com.ecommerce.storageservice.modules.upload.entity.StoredObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredObjectRepository extends JpaRepository<StoredObjectEntity, UUID> {
}
