package vn.com.lifesup.base.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import vn.com.lifesup.base.util.ErrorCode;
import vn.com.lifesup.base.util.Translator;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    private Integer totalRecords;

    private Object extraData;

    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = Translator.getMessage(message);
        this.data = data;
    }

    public ApiResponse(String code, String message, T data, Integer totalRecords) {
        this.code = code;
        this.message = Translator.getMessage(message);
        this.data = data;
        this.totalRecords = totalRecords;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(), data);
    }


    public static <T> ApiResponse<List<T>> success(List<T> data, Integer totalRecords) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(), data, totalRecords);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                message, data);
    }

    public static <T> ApiResponse<T> error(@NonNull String message) {
        return new ApiResponse<>(ErrorCode.FAILED.getCode(), message, null);
    }

    public static <T> ApiResponse<T> serverError() {
        return error(ErrorCode.SERVER_ERROR);
    }

    public static <T> ApiResponse<T> invalid(@NonNull String message) {
        return new ApiResponse<>(ErrorCode.INVALIDATE.getCode(), message, null);
    }


    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), Translator.getMessage(errorCode), null);
    }

    public static <T> ApiResponse<T> error(T data, ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), Translator.getMessage(errorCode), data);
    }

    public static <T> ApiResponse<T> error(T data, String errorCode, String errorMessage) {
        return new ApiResponse<>(errorCode, errorMessage, data);
    }
}
