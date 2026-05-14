package com.ecommerce.storageservice.modules.upload;

final class UploadUrls {

    private UploadUrls() {
    }

    static String joinPublic(String publicBaseUrl, String objectKey) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.replaceAll("/+$", "");
        String key = objectKey == null ? "" : objectKey.replaceAll("^/+", "");
        return base + "/" + key;
    }
}
