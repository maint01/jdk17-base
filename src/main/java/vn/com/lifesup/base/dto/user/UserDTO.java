package vn.com.lifesup.base.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class UserDTO {
    @Schema(description = "Id người dùng", example = "1")
    private Long id;

    @Schema(description = "Tên đăng nhập", example = "test", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Email", example = "abc@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    private String imageUrl;

    @Schema(description = "Trạng thái", example = "1 or 0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "Trạng thái(tên)", example = "1 or 0", requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusName;

    @Schema(description = "Mật khẩu", example = "DFE$#43")
    private String password;

    @Schema(description = "Mật khẩu xác nhận", example = "DFE$#43")
    private String confirmPassword;

    @Schema(description = "Số điện thoại", example = "09856231512")
    private String mobilePhone;

    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

}
