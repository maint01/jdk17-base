package vn.com.lifesup.base.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }

    public BusinessException(String message) {
        super(message);
        this.code = null;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

