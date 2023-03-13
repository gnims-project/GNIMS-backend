package com.gnims.project.share.email;

import com.gnims.project.domain.user.dto.SimpleMessageResult;
import com.gnims.project.share.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.gnims.project.share.message.ResponseMessage.SECRET_UPDATE_SUCCESS_MESSAGE;
import static com.gnims.project.share.message.ResponseMessage.SUCCESS_AUTH_EMAIL_MESSAGE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PatchMapping("/auth/password")
    public ResponseEntity<SimpleMessageResult> updatePassword(
            @Validated(ValidationSequence.class) @RequestBody EmailPasswordDto request)  throws Exception {
        emailService.updatePassword(request);
        return ok(new SimpleMessageResult(OK.value(), SECRET_UPDATE_SUCCESS_MESSAGE));
    }

    @PatchMapping("/auth/code")
    public ResponseEntity<SimpleMessageResult> updatePassword(@RequestBody AuthCodeDto request)  throws Exception {
        emailService.checkCode(request);
        return ok(new SimpleMessageResult(OK.value(), SUCCESS_AUTH_EMAIL_MESSAGE));
    }
}
