package com.trip.planit.User.service;

import com.trip.planit.User.entity.EmailVerification;
import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TemporaryUserRepository temporaryUserRepository;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);


    @Autowired
    public EmailService(JavaMailSender javaMailSender,
                        EmailVerificationRepository emailVerificationRepository,
                        TemporaryUserRepository temporaryUserRepository) {
        this.javaMailSender = javaMailSender;
        this.emailVerificationRepository = emailVerificationRepository;
        this.temporaryUserRepository = temporaryUserRepository;
    }

    // 이메일 코드 전송 & 저장
    @Transactional
    public void sendVerificationCode(String email) {
        log.info("Sending verification code to email: {}", email);

        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        invalidateOldVerificationCodes(temporaryUser);

        int verificationCode = generateVerificationCode();
        sendEmail(email, "Your code is: " + verificationCode);

        EmailVerification emailVerification = EmailVerification.builder()
                .temporaryUser(temporaryUser)
                .verificationCode(verificationCode)
                .verifiedEmail(false)
                .createTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(3)) // 유효 코드 - 3분
                .build();

        emailVerificationRepository.save(emailVerification);
    }

    // 이전 인증 코드 무효화
    private void invalidateOldVerificationCodes(TemporaryUser temporaryUser) {
        emailVerificationRepository.findTopByTemporaryUserAndVerifiedEmailFalseOrderByCreateTimeDesc(temporaryUser)
                .ifPresent(verification -> {
                    verification.setVerifiedEmail(true);
                    emailVerificationRepository.save(verification);
                });
    }

    // UserService.java

    // 임시 사용자에 저장 여부
    public boolean existsTemporaryUserByEmail(String email) {
        return temporaryUserRepository.findByEmail(email).isPresent();
    }

    // 인증 코드 검증
    public boolean verifyEmailCode(String email, int code) {
        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        Optional<EmailVerification> verificationOpt =
                emailVerificationRepository.findTopByTemporaryUserAndVerifiedEmailFalseOrderByCreateTimeDesc(temporaryUser);

        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();

            if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Verification code has expired.");
            }

            if (verification.getVerificationCode() == code) {
                verification.setVerifiedEmail(true);
                emailVerificationRepository.save(verification);
                return true;
            }
        }
        return false;
    }

    // 이메일 전송
    private void sendEmail(String to, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[PlanIt] Verification Code");
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("Failed to send email.", e);
        }
    }

    // 랜덤 인증 코드 생성
    public int generateVerificationCode() {
        return 1000 + new Random().nextInt(9000); // 4자리 숫자 생성
    }
}
