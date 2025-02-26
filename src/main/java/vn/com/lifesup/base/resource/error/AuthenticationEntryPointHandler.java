package vn.com.lifesup.base.resource.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.util.ErrorCode;

import java.io.IOException;
import java.io.OutputStream;

@RequiredArgsConstructor
@Component
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final MessageSource messageSource;

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        unauthorized(response);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        unauthorized(response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.NO_PERMISSION.getCode());
        apiResponse.setMessage(messageSource.getMessage(ErrorCode.NO_PERMISSION.getMessage(), null, LocaleContextHolder.getLocale()));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        OutputStream os = response.getOutputStream();
        objectMapper.writeValue(os, apiResponse);
        os.flush();
    }

}
