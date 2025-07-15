package com.fastcampus.ecommerce.config.middleware;

import com.fastcampus.ecommerce.common.errors.*;
import com.fastcampus.ecommerce.model.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {

    @ExceptionHandler({
            ResourceNotFoundException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException exception) {
        log.error("Terjadi error. status code " + HttpStatus.NOT_FOUND + ". error message " + exception.getMessage());
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleBadRequestException(HttpServletRequest req, BadRequestException exception) {
        log.error("Terjadi error. status code " + HttpStatus.BAD_REQUEST + ". error message " + exception.getMessage());
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleGenericException(HttpServletRequest req, HttpServletResponse resp, Exception exception) {
        if(exception instanceof BadCredentialsException ||
                exception instanceof AccountStatusException ||
                exception instanceof SignatureException ||
                exception instanceof ExpiredJwtException ||
                exception instanceof AuthenticationException ||
                exception instanceof InsufficientAuthenticationException
        ) {
            log.error("Terjadi error. status code " + HttpStatus.FORBIDDEN + ". error message " + exception.getMessage());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ErrorResponse.builder()
                    .code(HttpStatus.FORBIDDEN.value())
                    .message(exception.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        log.error("Terjadi error. status code " + HttpStatus.INTERNAL_SERVER_ERROR + ". error message " + exception.getMessage());
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errors.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handleUnauthenticatedException(HttpServletRequest req, Exception exception) {
        log.error("Terjadi error. status code " + HttpStatus.UNAUTHORIZED + ". error message " + exception.getMessage());
        return ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({
            UsernameAlreadyExistsException.class,
            EmailAlreadyExistsException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public @ResponseBody ErrorResponse handleConflictException(HttpServletRequest req, Exception exception) {
        log.error("Terjadi error. status code " + HttpStatus.CONFLICT + ". error message " + exception.getMessage());
        return ErrorResponse.builder()
                .code(HttpStatus.CONFLICT.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public @ResponseBody ErrorResponse handleForbiddenException(
            HttpServletRequest req,
            Exception exception
    ) {
        return ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
