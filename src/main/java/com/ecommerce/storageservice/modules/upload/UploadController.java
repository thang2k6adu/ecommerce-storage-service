package com.ecommerce.storageservice.modules.upload;

import com.ecommerce.storageservice.common.api.ApiResponse;
import com.ecommerce.storageservice.modules.upload.dto.UploadCreatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadCreatedResponse>> upload(@RequestParam("file") MultipartFile file) {
        UploadCreatedResponse body = uploadService.upload(file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", body));
    }

    @DeleteMapping("/uploads/{fileId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID fileId) {
        uploadService.delete(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
