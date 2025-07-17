// Generic API Response wrapper
package com.talentradar.assessment_service.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    
    private T data;
    
    private List<String> errors;
    
    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .build();
    }
    
    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .errors(errors)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .errors(List.of(error))
                .build();
    }
}