package com.gnims.project.util.gmail;

import com.gnims.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl{

    private final JavaMailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private static String hostAddress = "http://localhost:8080"; //바꿔야함 <- 프론트
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    @Transactional
    public String emailLinkCheck(EmailPasswordDto request) {

        log.info("이메일 링크 확인 중");

        //인증 정보가 존재하지 않음
        emailRepository.findByLink(request.getLink()).orElseThrow(
                () -> new IllegalArgumentException("인증 실패 / 링크가 유효하지 않습니다.")
        );

        //비밀번호 암호화
        String password = passwordEncoder.encode(request.getPassword());

        userRepository.findByEmail("Gnims.Auth." + request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("해당 이메일의 유저가 존재하지 않습니다.")
        ).updatePassword(password);

        log.info("이메일 인증 완료");

        return null;
    }

    private MimeMessage createMessage(String to, String link, String email)throws Exception{

        System.out.println("보내는 대상 : " + to);
        System.out.println("인증 번호 : " + link);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email);//보내는 대상
        message.setSubject("그님스 이메일 인증");//제목

        String msgg="";
        msgg+= "<div style='margin:100px;'>";
        msgg+= "<h1> 안녕하세요 그님스입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>>" + to + " 님의 메일 인증을 위해 아래 링크를 클릭해주세요.<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다!<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>인증 링크 입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "Link : <strong>";
        msgg+= "<a href=\"" + hostAddress + "/email/" + link + "/" + email + "\">"; //프론트의 주소 + api/email/대충암호화된문자열/대충누군가의이메일 <- 백엔드로 바로 안가는 이유는 프론트와 연결하기 위해서
//        msgg+= "<눌러서 인증하기>" + "</strong><div><br/> ";                           //프론트에서 받고 다시 백엔드로 link, email 이랑 재설정한 비밀번호를 넘김
        msgg+= hostAddress + "/email/" + link + "/" + email + "</strong><div><br/> ";                           //프론트에서 받고 다시 백엔드로 link, email 이랑 재설정한 비밀번호를 넘김
        msgg+= "</div>";                                                            //백에서 link 로 인증유저임을 확인하고(이 로직이 없을경우 임의의유저가 인증없이 비밀번호를 바꾸는 가능성이생김)
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("ymwoo1023@gmail.com","그님스 노예1"));//보내는 사람

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
        String link = createLink(email);
        MimeMessage message = createMessage(to, link, email);
        try{//예외처리
            EmailValidation emailValidation = new EmailValidation(link);
            emailRepository.save(emailValidation);
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException("이메일 발송 오류입니다.");
        } catch(DataIntegrityViolationException es) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        return link;
    }

}

