package com.ecommerce.storageservice.common.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"success", "data", "meta", "error", "message"})
public class ApiResponse<T> {

    private Boolean success;
    private T data;
    private PageResponse.Meta meta;
    private String error;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<List<T>> success(PageResponse<T> pageResponse) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .data(pageResponse.getContent())
                .meta(pageResponse.toMeta())
                .build();
    }

    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
}
