package com.gnims.project.exception.advice;

import com.gnims.project.exception.NotFoundInformationException;
import com.gnims.project.domain.friendship.exception.NotFriendshipException;
import com.gnims.project.exception.dto.ExceptionResponse;
import com.gnims.project.exception.dto.ExceptionResponses;
import com.gnims.project.share.slack.SlackMessageSender;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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
    private static final String STATUS = "\nstatus : ";
    private static final String MESSAGE =  "\nmessage: ";
    private static final String EXCEPTION = "\nexception: ";

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ExceptionResponse response = ExceptionResponse.of(400, exception);
        slackMessageSender.trackError(STATUS + BAD_REQUEST.getReasonPhrase() + MESSAGE + exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponses> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        List<String> errorMessages = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage())
                                                .collect(Collectors.toList());

        ExceptionResponses response = new ExceptionResponses(400, errorMessages);
        slackMessageSender.trackError(STATUS + BAD_REQUEST.getReasonPhrase() + MESSAGE + errorMessages);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleBadCredentialsException(BadCredentialsException exception) {
        ExceptionResponse response = ExceptionResponse.of(401, exception);
        slackMessageSender.trackError(STATUS + UNAUTHORIZED.getReasonPhrase() + MESSAGE + exception.getMessage());
        return ResponseEntity.status(UNAUTHORIZED.value()).body(response);
    }

    @ExceptionHandler({NotFriendshipException.class, SecurityException.class})
    public ResponseEntity<ExceptionResponse> handleNotPermissionException(RuntimeException exception) {
        ExceptionResponse response = ExceptionResponse.of(403, exception);
        slackMessageSender.trackError(STATUS + FORBIDDEN.getReasonPhrase() + MESSAGE + exception.getMessage());
        return ResponseEntity.status(FORBIDDEN.value()).body(response);
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundInformationException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundInformationException(NotFoundInformationException exception) {
        ExceptionResponse response = ExceptionResponse.of(404, exception);
        slackMessageSender.trackError(STATUS + NOT_FOUND.getReasonPhrase() + MESSAGE + exception.getMessage());
        return ResponseEntity.status(NOT_FOUND.value()).body(response);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleUnexpectedException(Exception exception) {
        slackMessageSender.trackError(EXCEPTION + exception.getClass().getSimpleName() +
                STATUS + INTERNAL_SERVER_ERROR.getReasonPhrase() + MESSAGE + exception.getMessage());
        log.info("[ERROR class : {} MESSAGE : {}]", exception.getClass().getSimpleName(), exception.getMessage());
    }
}
