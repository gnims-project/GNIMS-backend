package com.gnims.project.exception.advice;

import com.gnims.project.exception.dto.ExceptionResponseListMessage;
import com.gnims.project.exception.dto.ExceptionResponseMessage;
import com.gnims.project.share.slack.SlackController;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

/**
 * SLACK의 경우 추후 AOP 도입하여 더 많은 내용을 효과적으로 담을 예정
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SlackController slackController;

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> HandleIllegalArgumentException
            (IllegalArgumentException exception) throws IOException {
        ExceptionResponseMessage message = new ExceptionResponseMessage(BAD_REQUEST.value(), exception.getMessage());
        slackController.trackError("**status** : " + BAD_REQUEST.getReasonPhrase() + "\n**message** : " + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseListMessage> HandleMethodArgumentNotValidException
            (MethodArgumentNotValidException exception) throws IOException {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        List<String> errorMessages = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage())
                                                .collect(Collectors.toList());

        ExceptionResponseListMessage messages = new ExceptionResponseListMessage(BAD_REQUEST.value(), errorMessages);
        slackController.trackError("**status** : " + BAD_REQUEST.getReasonPhrase() + "\n**message** : " + errorMessages);
        return new ResponseEntity<>(messages, HttpStatus.valueOf(messages.getStatus()));

    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> HandleBadCredentialsException(BadCredentialsException exception)
            throws IOException {
        ExceptionResponseMessage message = new ExceptionResponseMessage(UNAUTHORIZED.value(), exception.getMessage());
        slackController.trackError("**status** : " + UNAUTHORIZED.getReasonPhrase() + "\n**message** : " + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> HandleSecurityException(SecurityException exception)
            throws IOException {
        ExceptionResponseMessage message = new ExceptionResponseMessage(FORBIDDEN.value(), exception.getMessage());
        slackController.trackError("**status** : " + FORBIDDEN.getReasonPhrase() + "\n**message** : " + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleUnexpectedException(Exception exception) throws IOException {
        slackController.trackError("**status** : " + INTERNAL_SERVER_ERROR.getReasonPhrase() + "\n**message** : " + exception.getMessage());
        log.info("[error class : {} message : {}]", exception.getClass(), exception.getMessage());

    }

}
