package com.gnims.project.share.gmail;

import com.gnims.project.domain.user.entity.SocialCode;
import com.gnims.project.domain.user.entity.User;
import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.gnims.project.share.message.ExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    @Value("${AdminMail.id}")
    private String hostEmail;   //이메일을 날리는 주체의 이메일

    @Transactional
    public void updatePassword(EmailPasswordDto request) {

        //DB에서 해당 이메일의 유저를 찾음
        User user = userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(NON_EXISTED_EMAIL)
        );

        //해당 이메일의 인증 정보가 없을 때
        EmailValidation emailValidation = emailRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(INVALID_CODE_ERROR)
        );

        //인증 상태가 false 일때
        if (!emailValidation.getIsChecked()) {
            throw new IllegalArgumentException(UNAUTHENTICATED_EMAIL_ERROR);
        }

        //인증 후 3 시간 이상 지날 시 인증 실패, 인증 메일 삭제
        if (emailValidation.getModifiedAt().isBefore(LocalDateTime.now().minusHours(3))) {
            throw new IllegalArgumentException(INVALID_CODE_ERROR);
        }

        //암호화 후 저장
        user.updatePassword(passwordEncoder.encode(request.getPassword()));

        emailRepository.delete(emailValidation);
    }

    private MimeMessage createMessage(String to, String link, String email) throws Exception {

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email);//보내는 대상
        message.setSubject("그님스 이메일 인증");//제목

        //이메일에 들어갈 내용
        String msgg = "";
        msgg += "<div style='margin:100px;'>";
        msgg += "<h1> 안녕하세요 그님스입니다. </h1>";
        msgg += "<br>";
        msgg += "<p><" + to + "> 님의 그님스 인증을 위해 아래 코드를 사용해주세요.<p>";
        msgg += "<p>인증 후 3 시간 이내에 비밀번호를 재설정 해주세요.<p>";
        msgg += "<br>";
        msgg += "<p>감사합니다!<p>";
        msgg += "<br>";
        msgg += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg += "<h2 style='color:blue;'>인증 코드 입니다.</h2>";
        msgg += "<div style='font-size:130%'>";
        msgg += "인증코드 : <strong>" + link + "</strong><div><br/> ";
        msgg += "</div>";
        message.setText(msgg, "utf-8", "html");
        message.setFrom(new InternetAddress(hostEmail, "그님스"));//보내는 사람

        return message;
    }

    public String createCode() {

        //12자리의 임의 코드 생성
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public void createEmailValidation(String to, String email) throws Exception {

        //인증 코드 생성
        String code = createCode();

        //인증 메일 생성
        MimeMessage message = createMessage(to, code, email);

        //예외처리
        try {
            Optional<EmailValidation> byEmail = emailRepository.findByEmail(email);

            //DB에 존재하는 인증 메일인지 검사
            if (byEmail.isPresent()) {
                //삭제 후 재 생성
                emailRepository.delete(byEmail.get());
            }

            //인증 객체 생성 / 저장
            EmailValidation emailValidation = new EmailValidation(code, email);
            emailRepository.save(emailValidation);

            //인증 메일 발송
            emailSender.send(message);
        } catch (Exception es) {
            es.printStackTrace();
            throw new IllegalArgumentException(POSTING_EMAIL_ERROR);
        }
    }

    @Transactional
    public void checkCode(AuthCodeDto request) {

        //인증하는 메일의 인증 객체가 존재하지 않을 시
        EmailValidation emailValidation = emailRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(INVALID_CODE_ERROR)
        );

        //인증 코드가 일치하지 않을 때
        if (!emailValidation.getCode().equals(request.getCode())) {
            throw new IllegalArgumentException(INVALID_CODE_ERROR);
        }

        //3분이 지낫을 시 인증 실패, DB에서 인증정보 삭제
        if (emailValidation.getCreateAt().isBefore(LocalDateTime.now().minusMinutes(3))) {
            emailRepository.delete(emailValidation);
            throw new IllegalArgumentException(INVALID_CODE_ERROR);
        }

        //인증 객체의 상태를 true로 바꿈
        emailValidation.isCheckedTrue();
    }
}

