package vn.com.lifesup.base.exception;

import lombok.Getter;

@Getter
public class UncheckBusinessException extends RuntimeException {
    private final String code;

    public UncheckBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }

    public UncheckBusinessException(String message) {
        super(message);
        this.code = null;
    }

    public UncheckBusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

