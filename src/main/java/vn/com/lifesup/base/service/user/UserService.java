package vn.com.lifesup.base.service.user;

import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.user.UserDTO;
import vn.com.lifesup.base.dto.user.UserSearchDTO;

import java.util.List;

public interface UserService {
    ApiResponse<List<UserDTO>> filter(UserSearchDTO searchDTO);
}
