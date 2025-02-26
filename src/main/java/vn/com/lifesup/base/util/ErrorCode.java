package vn.com.lifesup.base.util;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS("00", "success.data"),
    ERR_LOGIN("01", "err.login"),
    NO_PERMISSION("02", "err.no_permission"),
    ERR_NO_SESSION("03", "err.no_session"),
    DATA_EMPTY("04", "data.empty"),
    FIRST_NAME_EMPTY("05", "keycloak.validate.firstName.null"),
    LAST_NAME_EMPTY("06", "keycloak.validate.lastName.null"),
    POSITION_EMPTY("07",  "keycloak.validate.position.null"),
    USERNAME_EMPTY("08", "keycloak.validate.username.null"),
    PHONE_EMPTY("09", "keycloak.validate.phoneNumber.null"),
    EMAIL_EMPTY("10", "keycloak.validate.email.null"),
    IMAGE_FORMAT_WRONG("11", "folder.validate.fileImage.format.wrong"),
    USERNAME_EXIST("12", "keycloak.validate.username.exist"),
    ERR_DATA("13", "err.data"),
    USERNAME_NOT_EXIST("14", "keycloak.validate.username.not-exist"),
    SERVER_ERROR("ERR00", "error.server_error"),
    INVALIDATE("ERR01", "Validate error"),
    FAILED("ERR05", "error.failed");

    private final String code;
    private final String message;

    ErrorCode(String code, String description) {
        this.code = code;
        this.message = description;
    }

}
