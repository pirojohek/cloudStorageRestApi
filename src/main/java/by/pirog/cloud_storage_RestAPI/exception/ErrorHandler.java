package by.pirog.cloud_storage_RestAPI.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    // 401 - неверные данные (такого пользователя нет, или пароль неправильный)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problemDetail.setTitle("Bad Credentials");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    // 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    // 500 - неизвестная ошибка
    @ExceptionHandler(UnknownException.class)
    public ResponseEntity<ProblemDetail> handleUnknownException(UnknownException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    // попытка выполнения действия, требующего требующее аутентификации, но пользователь
    // не предоставил или не имеет действительных учетных данных
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex,
                                                                                          HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problemDetail.setTitle("Access Denied");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    // 409 - username занят
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUsernameAlreadyExistsException(
            UsernameAlreadyExistsException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT.value());
        problemDetail.setTitle("Conflicting Username");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    // 400 - ошибки валидации
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("Invalid data");
        problemDetail.setProperty("errors",
                ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                         HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }


    // остальные ошибки
    @ExceptionHandler(Exception.class)
    public  ResponseEntity<ProblemDetail> handleException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setProperty("path", request.getServletPath());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
