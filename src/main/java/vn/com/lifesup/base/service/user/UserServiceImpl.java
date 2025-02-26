package vn.com.lifesup.base.service.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.lifesup.base.constant.Constants;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.user.UserDTO;
import vn.com.lifesup.base.dto.user.UserSearchDTO;
import vn.com.lifesup.base.model.User;
import vn.com.lifesup.base.repository.UserCusRepository;
import vn.com.lifesup.base.repository.UserRepository;
import vn.com.lifesup.base.security.SecurityUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Transactional
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepositoryJPA;

    private final PasswordEncoder passwordEncoder;

    private final UserCusRepository userCusRepository;



    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepositoryJPA
                .findOneByActivationKey(key)
                .map(
                        user -> {
                            // activate given user for the registration key.
                            user.setActivated(true);
                            user.setActivationKey(null);
                            log.debug("Activated user: {}", user);
                            return user;
                        }
                );
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepositoryJPA
                .findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(Instant.now().plusSeconds(86400)))
                .map(
                        user -> {
                            user.setPassword(passwordEncoder.encode(newPassword));
                            user.setResetKey(null);
                            user.setResetDate(null);
                            return user;
                        }
                );
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepositoryJPA
                .findOneByEmailIgnoreCase(mail)
                .filter(User::isActivated)
                .map(
                        user -> {
                            user.setResetKey(RandomStringUtils.randomAlphanumeric(20));
                            user.setResetDate(Instant.now());
                            return user;
                        }
                );
    }

    public ApiResponse<?> deleteUser(String login) {
        log.info("Service to delete user by username={}", login);
        return userRepositoryJPA
                .findOneByUsername(login)
                .map(
                        user -> {
                            userRepositoryJPA.delete(user);
                            log.debug("Deleted User: {}", user);
                            return ApiResponse.success(null);
                        }
                ).orElse(ApiResponse.invalid("Tài khoản không tồn tại"));
    }

    @Transactional
    public ApiResponse<?> changePassword(String currentClearTextPassword, String newPassword) {
        return SecurityUtils
                .getCurrentUserLogin()
                .flatMap(userRepositoryJPA::findOneByUsername)
                .map(
                        user -> {
                            String currentEncryptedPassword = user.getPassword();
                            if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                                return ApiResponse.invalid("Mật khẩu mới và mật khẩu xác nhận không trùng khớp");
                            }
                            String encryptedPassword = passwordEncoder.encode(newPassword);
                            user.setPassword(encryptedPassword);
                            log.debug("Changed password for User: {}", user);
                            return ApiResponse.success(null);
                        }
                ).orElseGet(() -> ApiResponse.invalid("Tài khoản không tồn tại"));
    }

    public ApiResponse<List<UserDTO>> filter(UserSearchDTO searchDTO) {
        log.info("Service to search Users: {}", searchDTO);
        return userCusRepository.filter(searchDTO);
    }


    public ApiResponse<?> checkEmailExisted(String email) {
        log.info("Service to check email existed: {}", email);
        return userRepositoryJPA.findOneByEmailIgnoreCase(email)
                .map(user -> ApiResponse.success("Email đã được sử dụng"))
                .orElse(ApiResponse.invalid("Email chưa được sử dụng"));
    }

    private ApiResponse<?> validateRequest(UserDTO userDTO, boolean isCreate) {
        if (isCreate) {
            if (userDTO.getId() != null) {
                return ApiResponse.invalid("Tài khoản tạo mới không truyền id");
            }

            if (StringUtils.isBlank(userDTO.getUsername())) {
                return ApiResponse.invalid("Tên đăng nhập không được để trống");
            }

            if (!userDTO.getUsername().matches(Constants.LOGIN_REGEX)) {
                return ApiResponse.invalid("Tên đăng nhập chứa kí tự không hợp lệ");
            }

            if (StringUtils.isBlank(userDTO.getEmail())) {
                return ApiResponse.invalid("Email không được để trống");
            }

            if (StringUtils.isBlank(userDTO.getPassword())) {
                return ApiResponse.invalid("Mật khẩu không được để trống");
            }

            if (StringUtils.isBlank(userDTO.getConfirmPassword())) {
                return ApiResponse.invalid("Mật khẩu xác nhận không được để trống");
            }

            if (!userDTO.getConfirmPassword().equals(userDTO.getPassword())) {
                return ApiResponse.invalid("Mật khẩu xác nhận không trùng khớp");
            }

            if (userRepositoryJPA.findOneByUsername(userDTO.getUsername().toLowerCase()).isPresent()) {
                return ApiResponse.invalid("Tên đăng nhập đã tồn tại");
            }
            if (userRepositoryJPA.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
                return ApiResponse.invalid("Email đã tồn tại");
            }
            return ApiResponse.success(null);
        }
        if (userDTO.getId() == null) {
            return ApiResponse.invalid("Mã người dùng không được để trống");
        }
        if (StringUtils.isBlank(userDTO.getEmail())) {
            return ApiResponse.invalid("Email không được để trống");
        }
        User user = userRepositoryJPA.findOneByUsername(userDTO.getUsername().toLowerCase()).orElse(null);
        if (user != null && !user.getId().equals(userDTO.getId())) {
            return ApiResponse.invalid("Email đã tồn tại");
        }

        if (StringUtils.isNotBlank(userDTO.getPassword())
                && !userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            return ApiResponse.invalid("Mật khẩu xác nhận không trùng khớp");
        }

        return ApiResponse.success(null);
    }

    private ApiResponse<?> validateAccount(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return ApiResponse.invalid("Mã người dùng không được để trống");
        }
        if (StringUtils.isBlank(userDTO.getEmail())) {
            return ApiResponse.invalid("Email không được để trống");
        }
        User user = userRepositoryJPA.findOneByUsername(userDTO.getUsername().toLowerCase()).orElse(null);
        if (user != null && !user.getId().equals(userDTO.getId())) {
            return ApiResponse.invalid("Email đã tồn tại");
        }

        return ApiResponse.success(null);
    }
}
