package com.gnims.project.exception.advice;

import com.gnims.project.exception.dto.ExceptionResponseListMessage;
import com.gnims.project.exception.dto.ExceptionResponseMessage;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> illegalArgumentExceptionHandle(IllegalArgumentException exception) {
        ExceptionResponseMessage message = new ExceptionResponseMessage(BAD_REQUEST.value(), exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseListMessage> methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        List<String> errorMessages = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage())
                                                .collect(Collectors.toList());

        ExceptionResponseListMessage messages = new ExceptionResponseListMessage(BAD_REQUEST.value(), errorMessages);
        return new ResponseEntity<>(messages, HttpStatus.valueOf(messages.getStatus()));

    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> BadCredentialsExceptionHandle(BadCredentialsException exception) {
        ExceptionResponseMessage message = new ExceptionResponseMessage(UNAUTHORIZED.value(), exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

//    @ExceptionHandler
//    public ResponseEntity<ExceptionResponseMessage> SecurityExceptionHandle(SecurityException exception) {
//        ExceptionResponseMessage message = new ExceptionResponseMessage(FORBIDDEN.value(), exception.getMessage());
//        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
//    }

}
