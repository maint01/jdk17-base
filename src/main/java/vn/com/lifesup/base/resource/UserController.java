package vn.com.lifesup.base.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.dto.user.UserDTO;
import vn.com.lifesup.base.dto.user.UserSearchDTO;
import vn.com.lifesup.base.service.user.UserService;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userservice;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> filter(UserSearchDTO request) {
        log.info("Request to search User: {}", request);
        return ResponseEntity.ok(userservice.filter(request));
    }
}
