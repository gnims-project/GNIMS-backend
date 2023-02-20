package com.gnims.project.util.gmail;

import com.gnims.project.domain.user.dto.SimpleMessageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailServiceImpl emailServiceImpl;

    @PatchMapping("/email/password")
    public ResponseEntity<SimpleMessageResult> updatePassword(@RequestBody EmailPasswordDto request)  throws Exception {

        emailServiceImpl.updatePassword(request);

        return new ResponseEntity<>(new SimpleMessageResult(OK.value(), "비밀번호 변경 성공"), OK);
    }
}
