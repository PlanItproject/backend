package com.trip.planit.User.service;

import com.trip.planit.User.entity.EmailVerification;
import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.repository.EmailVerificationRepository;
import com.trip.planit.User.repository.TemporaryUserRepository;
import com.trip.planit.User.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TemporaryUserRepository temporaryUserRepository;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;

    @Autowired
    public EmailService(JavaMailSender javaMailSender,
                        EmailVerificationRepository emailVerificationRepository,
                        TemporaryUserRepository temporaryUserRepository, TemplateEngine templateEngine, UserRepository userRepository) {
        this.javaMailSender = javaMailSender;
        this.emailVerificationRepository = emailVerificationRepository;
        this.temporaryUserRepository = temporaryUserRepository;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendVerificationEmail(String email) {

        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // 해당 이메일로 User가 이미 존재할 경우.
        boolean userExists = userRepository.existsByEmail(temporaryUser.getEmail());
        if (userExists) {
            throw new IllegalArgumentException("The user is already registered.");
        }

        invalidateOldVerificationCodes(temporaryUser);

        int verificationCode = generateVerificationCode();
        EmailVerification emailVerification = EmailVerification.builder()
                .temporaryUserId(temporaryUser)
                .verificationCode(verificationCode)
                .verifiedEmail(false)
                .createTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(5)) // 10분 유효
                .build();
        emailVerificationRepository.save(emailVerification);

        String htmlContent = generateEmailContent(String.valueOf(verificationCode));

        try {
            log.info("Preparing to send email to {}", email);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[PlanIt] 인증코드");
            helper.setText(htmlContent, true); // HTML 이메일 설정

            javaMailSender.send(message);
            log.info("Verification email sent successfully to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            throw new IllegalStateException("Failed to send email.", e);
        }
    }

    private String generateEmailContent(String verificationCode) {
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        return templateEngine.process("verificationEmail", context);
    }

    // 이전 인증 코드 무효화
    private void invalidateOldVerificationCodes(TemporaryUser temporaryUser) {
        emailVerificationRepository.findTopByTemporaryUserIdAndVerifiedEmailFalseOrderByCreateTimeDesc(temporaryUser) // 아직 인증이 완료되지 않은 특정 Temp 사용자
                .ifPresent(verification -> {    // 인증코드를 매개변수로 받음
                    verification.setVerifiedEmail(true);    // 무효화
                    emailVerificationRepository.save(verification); // 저장
                });
    }

    // 임시 사용자에 저장 여부
    public boolean existsTemporaryUserByEmail(String email) {
        return temporaryUserRepository.findByEmail(email).isPresent();
    }

    // 인증 코드 검증
    public boolean verifyEmailCode(String email, int code) {
        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        Optional<EmailVerification> verificationOpt =
                emailVerificationRepository.findTopByTemporaryUserIdAndVerifiedEmailFalseOrderByCreateTimeDesc(temporaryUser);

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

    // 랜덤 인증 코드 생성
    public int generateVerificationCode() {
        return 1000 + new Random().nextInt(9000); // 4자리 숫자 생성
    }

}
