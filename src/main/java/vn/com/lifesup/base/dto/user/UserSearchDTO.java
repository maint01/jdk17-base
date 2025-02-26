package vn.com.lifesup.base.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lifesup.base.dto.common.BaseSearchDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSearchDTO extends BaseSearchDTO {
    @Schema(description = "Tên đăng nhập", example = "test")
    private String username;

    @Schema(description = "Họ và tên", example = "test")
    private String fullName;

    @Schema(description = "Số điện thoại", example = "09856236521")
    private String mobilePhone;

    @Schema(description = "Nhóm người dùng", example = "ROLE_ADMIN")
    private String authority;

    @Schema(description = "Danh sách nhóm người dùng", example = "['ROLE_ADMIN', 'ROLE_CUSTOMER']")
    private String authorities;

    @Schema(description = "Trạng thái", example = "1")
    private Integer status;
}