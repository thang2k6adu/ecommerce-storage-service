package com.ecommerce.storageservice.modules.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadCreatedResponse {

    private UUID fileId;
    private String url;
}
