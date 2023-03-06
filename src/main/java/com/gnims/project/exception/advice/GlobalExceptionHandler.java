package com.gnims.project.exception.advice;

import com.gnims.project.exception.dto.ExceptionResponseListMessage;
import com.gnims.project.exception.dto.ExceptionResponseMessage;
import com.gnims.project.share.slack.SlackMessageSender;
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

    private final SlackMessageSender slackMessageSender;
    private static final String STATUS = "status : ";
    private static final String MESSAGE =  "\nmessage: ";

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> handleIllegalArgumentException(IllegalArgumentException exception) {
        ExceptionResponseMessage message = new ExceptionResponseMessage(BAD_REQUEST.value(), exception.getMessage());
        slackMessageSender.trackError(STATUS + BAD_REQUEST.getReasonPhrase() + MESSAGE + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseListMessage> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        List<String> errorMessages = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage())
                                                .collect(Collectors.toList());

        ExceptionResponseListMessage messages = new ExceptionResponseListMessage(BAD_REQUEST.value(), errorMessages);
        slackMessageSender.trackError(STATUS + BAD_REQUEST.getReasonPhrase() + MESSAGE + errorMessages);
        return new ResponseEntity<>(messages, HttpStatus.valueOf(messages.getStatus()));

    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> handleBadCredentialsException(BadCredentialsException exception) {
        ExceptionResponseMessage message = new ExceptionResponseMessage(UNAUTHORIZED.value(), exception.getMessage());
        slackMessageSender.trackError(STATUS + UNAUTHORIZED.getReasonPhrase() + MESSAGE + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponseMessage> handleSecurityException(SecurityException exception) {
        ExceptionResponseMessage message = new ExceptionResponseMessage(FORBIDDEN.value(), exception.getMessage());
        slackMessageSender.trackError(STATUS + FORBIDDEN.getReasonPhrase() + MESSAGE + exception.getMessage());
        return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatus()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    public void handleUnexpectedException(NullPointerException exception) {
        slackMessageSender.trackError(STATUS + INTERNAL_SERVER_ERROR.getReasonPhrase() + MESSAGE + exception.getMessage());
        log.info("[ERROR class : {} MESSAGE : {}]", exception.getClass(), exception.getMessage());
    }
}
