package com.gnims.project.util.gmail;

import com.gnims.project.domain.user.dto.SimpleMessageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailServiceImpl emailServiceImpl;

    @GetMapping("/email/password")
    public ResponseEntity<SimpleMessageResult> emailConfirm(EmailPasswordDto request)  throws Exception {
        emailServiceImpl.emailLinkCheck(request);

        SimpleMessageResult response = new SimpleMessageResult(200, "변경 성공");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
