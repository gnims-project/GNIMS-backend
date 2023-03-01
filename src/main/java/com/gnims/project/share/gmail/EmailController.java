package com.gnims.project.share.gmail;

import com.gnims.project.domain.user.dto.SimpleMessageResult;
import com.gnims.project.share.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.gnims.project.share.message.ResponseMessage.PASSWORD_UPDATE_SUCCESS_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.SUCCESS_AUTH_EMAIL_MESSAGE;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PatchMapping("/auth/password")
    public ResponseEntity<SimpleMessageResult> updatePassword(@Validated(ValidationSequence.class) @RequestBody EmailPasswordDto request)  throws Exception {

        emailService.updatePassword(request);

        return new ResponseEntity<>(new SimpleMessageResult(OK.value(), PASSWORD_UPDATE_SUCCESS_MESSAGE), OK);
    }

    @PatchMapping("/auth/code")
    public ResponseEntity<SimpleMessageResult> updatePassword(@RequestBody AuthCodeDto request)  throws Exception {

        emailService.checkCode(request);

        return new ResponseEntity<>(new SimpleMessageResult(OK.value(), SUCCESS_AUTH_EMAIL_MESSAGE), OK);
    }
}
