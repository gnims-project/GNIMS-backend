package com.gnims.project.util.gmail;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.gnims.project.exception.dto.ExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl{

    private final JavaMailSender emailSender;
    private final PasswordEncoder passwordEncoder;
//    @Value("${email.hostAddress}")
//    private String hostAddress; //바꿔야함 <- 백 배포주소
////        private static String hostAddress = "https://eb.jxxhxxx.shop"; //바꿔야함 <- 백 배포주소 //필요없을지도?
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
        if(!emailValidation.getIsChecked()) {
            throw new IllegalArgumentException(UNAUTHENTICATED_EMAIL_ERROR);
        }

        //DB에 해당 이메일이 없을 때
        userRepository.findByEmail(SocialCode.EMAIL.getValue() + request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(NON_EXISTED_EMAIL)
        )
        //암호화 후 저장
        .updatePassword(passwordEncoder.encode(request.getPassword()));

        emailRepository.deleteByEmail(request.getEmail());
    }

    private MimeMessage createMessage(String to, String link, String email)throws Exception{

        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email);//보내는 대상
        message.setSubject("그님스 이메일 인증");//제목

        String msgg="";
        msgg+= "<div style='margin:100px;'>";
        msgg+= "<h1> 안녕하세요 그님스입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>>" + to + " 님의 메일 인증을 위해 아래 코드를 사용해주세요.<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다!<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h2 style='color:blue;'>인증 코드 입니다.</h2>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "인증코드 : <strong>"  + link + "</strong><div><br/> ";
//        msgg+= "<a href=\"" + hostAddress + "/email/" + link + "/" + email + "\">"; //프론트의 주소 + api/email/대충암호화된문자열/대충누군가의이메일 <- 백엔드로 바로 안가는 이유는 프론트와 연결하기 위해서
//        msgg+= "<눌러서 인증하기>" + "</strong><div><br/> ";                           //프론트에서 받고 다시 백엔드로 link, email 이랑 재설정한 비밀번호를 넘김
//        msgg+= "인증코드 : ";                           //프론트에서 받고 다시 백엔드로 link, email 이랑 재설정한 비밀번호를 넘김
        msgg+= "</div>";                                                            //백에서 link 로 인증유저임을 확인하고(이 로직이 없을경우 임의의유저가 인증없이 비밀번호를 바꾸는 가능성이생김)
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress(hostEmail,"그님스"));//보내는 사람

        return message;
    }

    public String createLink(String userId) throws NoSuchAlgorithmException {

        StringBuffer key = new StringBuffer();

        MessageDigest messageDigest = MessageDigest.getInstance("SHa-256");
        messageDigest.update(userId.getBytes());

        return bytesToHex(messageDigest.digest());
    }
    private String bytesToHex(byte[] bytes) {

        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    //    public String createKey() {
//        StringBuilder key = new StringBuilder();
//        Random rnd = new Random();
//        rnd.setSeed(System.currentTimeMillis());
////        rnd.setSeed(1);   //  되긴 하네
//        for (int i = 0; i < 8; i++) { // 인증코드 8자리
//            int index = rnd.nextInt(3); // 0~2 까지 랜덤
//            switch (index) {
//                case 0:
//                    key.append((char) (rnd.nextInt(26) + 97));
//                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
//                    break;
//                case 1:
//                    key.append((char) (rnd.nextInt(26) + 65));
//                    //  A~Z
//                    break;
//                case 2:
//                    key.append((rnd.nextInt(10)));
//                    // 0~9
//                    break;
//            }
//        }
//        return key.toString();
//    }

    public String sendSimpleMessage(String to, String email) throws Exception {

        // TODO Auto-generated method stub
        String code = createLink(email);
        MimeMessage message = createMessage(to, code, email);
        try{//예외처리
            Optional<EmailValidation> byEmail = emailRepository.findByEmail(email);

            if(byEmail.isPresent()) {
                byEmail.get().isCheckedFalse();
            }

            else {
                EmailValidation emailValidation = new EmailValidation(code, email);
                emailRepository.save(emailValidation);
            }
            emailSender.send(message);
        }/*catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException(POSTING_EMAIL_ERROR);
        } catch(DataIntegrityViolationException es) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }*/
        catch (Exception es) {
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

        if(!emailValidation.getCode().equals(request.getCode())) {
            throw new IllegalArgumentException(INVALID_CODE_ERROR);
        }

        //인증 상태를 true로 바꿈
        emailValidation.isCheckedTrue();
    }
}

