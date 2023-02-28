package com.gnims.project.share.gmail;

import com.gnims.project.domain.user.entity.SocialCode;
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
    private String hostEmail;

    @Transactional
    public void updatePassword(EmailPasswordDto request) {

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

        //DB에 해당 이메일이 있을 때
        userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).orElseThrow(
                        () -> new IllegalArgumentException(NON_EXISTED_EMAIL)
                )
                //암호화 후 저장
                .updatePassword(passwordEncoder.encode(request.getPassword()));

        emailRepository.delete(emailValidation);
    }

    private MimeMessage createMessage(String to, String link, String email) throws Exception {

        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email);//보내는 대상
        message.setSubject("그님스 이메일 인증");//제목

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
        msgg += "</div>";                                                            //백에서 link 로 인증유저임을 확인하고(이 로직이 없을경우 임의의유저가 인증없이 비밀번호를 바꾸는 가능성이생김)
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress(hostEmail, "그님스"));//보내는 사람

        return message;
    }

    public String createCode() {

        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public String createEmailValidation(String to, String email) throws Exception {

        // TODO Auto-generated method stub
        String code = createCode();
        MimeMessage message = createMessage(to, code, email);
        try {//예외처리
            Optional<EmailValidation> byEmail = emailRepository.findByEmail(email);

            if (byEmail.isPresent()) {
                emailRepository.delete(byEmail.get());
            }

            EmailValidation emailValidation = new EmailValidation(code, email);
            emailRepository.save(emailValidation);

            emailSender.send(message);
        } catch (Exception es) {
            es.printStackTrace();
            throw new IllegalArgumentException(POSTING_EMAIL_ERROR);
        }
        return code;
    }

    @Transactional
    public void checkCode(AuthCodeDto request) {

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

        //인증 상태를 true로 바꿈
        emailValidation.isCheckedTrue();
    }
}

