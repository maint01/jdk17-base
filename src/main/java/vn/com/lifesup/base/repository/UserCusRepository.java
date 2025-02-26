package vn.com.lifesup.base.repository;


import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.user.UserDTO;
import vn.com.lifesup.base.dto.user.UserSearchDTO;

import java.util.List;

public interface UserCusRepository {
    ApiResponse<List<UserDTO>> filter(UserSearchDTO request);
}
