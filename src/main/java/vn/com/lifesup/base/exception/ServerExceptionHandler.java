package vn.com.lifesup.base.exception;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.com.lifesup.base.dto.common.ApiResponse;
import vn.com.lifesup.base.util.ErrorCode;
import vn.com.lifesup.base.util.MessageUtil;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@Log4j2
@ControllerAdvice
public class ServerExceptionHandler {

    @ExceptionHandler({AuthenticationServiceException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Object>> handlerAuthenticationServiceException(Exception ex, WebRequest request) {
        log.info(request);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Object>> handlerAuthorizationDeniedExceptionException(Exception ex, WebRequest request) {
        log.info(request);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getBindingResult().getAllErrors(), ErrorCode.ERR_DATA.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, NumberFormatException.class,
            HttpMessageNotWritableException.class, InvalidFormatException.class, JsonParseException.class,
            HttpMessageConversionException.class, UncheckBusinessException.class})
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterExceptions(
            Exception ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundExceptions(Exception ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedExceptions(Exception ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<Object> handleException(BusinessException ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        log.info(request);
        log.error(ex.getMessage(), ex);

        if (ex instanceof BadRequestException) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ApiResponse.error(MessageUtil.getMessage("err.system.internal-server")), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
