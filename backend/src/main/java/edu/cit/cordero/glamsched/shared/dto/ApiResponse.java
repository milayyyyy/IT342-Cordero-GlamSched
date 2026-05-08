package edu.cit.cordero.glamsched.shared.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, ApiError error, String timestamp) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
    }

    public static class ApiError {
        private String code;
        private String message;

        public ApiError() {
        }

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now().toString());
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(new ApiError(code, message));
        response.setTimestamp(LocalDateTime.now().toString());
        return response;
    }
}
