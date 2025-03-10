package com.trip.planit.User.service;

import com.trip.planit.User.config.exception.BadRequestException;
import com.trip.planit.User.entity.EmailVerification;
import com.trip.planit.User.entity.TemporaryUser;
import com.trip.planit.User.entity.User;
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
    public void sendRegistrationVerificationEmail(String email) {

        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Temporary user not found."));

        // 해당 이메일로 User가 이미 존재할 경우.
        boolean userExists = userRepository.existsByEmail(temporaryUser.getEmail());

        if (userExists) {
            throw new IllegalArgumentException("The user is already registered.");
        }

        // 기존 인증번호 삭제: 기존에 저장된 인증 코드가 있다면 삭제
        emailVerificationRepository.findByTemporaryUserId_Email(email)
                .ifPresent(existingVerification -> {
                    emailVerificationRepository.delete(existingVerification);
                    log.info("Existing verification code deleted for email: {}", email);
                });


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

    @Transactional
    public void sendPasswordResetVerificationEmail(String email) {
        // User 엔티티에서 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // 기존의 인증 코드가 존재한다면 삭제하여 무효화 (또는 초기화)
        invalidateOldVerificationCodesForUser(user);

        int verificationCode = generateVerificationCode();
        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)  // 비밀번호 찾기 용도로 User와 연관
                .verificationCode(verificationCode)
                .verifiedEmail(false)
                .createTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(5)) // 5분 유효 (원하는 시간으로 조정)
                .build();
        emailVerificationRepository.save(emailVerification);

        String htmlContent = generateEmailContent(String.valueOf(verificationCode));

        try {
            log.info("Preparing to send password reset email to {}", email);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[PlanIt] 비밀번호 찾기 인증코드");
            helper.setText(htmlContent, true); // HTML 이메일 설정

            javaMailSender.send(message);
            log.info("Password reset verification email sent successfully to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            throw new IllegalStateException("Failed to send password reset email.", e);
        }
    }

    @Transactional
    public boolean verifyPasswordResetEmailCode(String email, int code) {
        // User를 기준으로 EmailVerification 레코드 조회
        EmailVerification verification = emailVerificationRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 검증 정보를 찾을 수 없습니다: " + email));

        // 만료 시간 체크
        if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired.");
        }

        // 인증 코드 일치 여부 확인
        if (verification.getVerificationCode() != code) {
            return false;
        }

        // 인증 성공 시 EmailVerification 정보 삭제하여 재사용 방지
        emailVerificationRepository.delete(verification);
        return true;
    }



    //이전 인증 코드 무효화 - user
    private void invalidateOldVerificationCodesForUser(User user) {
        // User의 이메일을 기준으로 기존 인증 코드가 있으면 삭제
        emailVerificationRepository.findByUserEmail(user.getEmail())
                .ifPresent(emailVerificationRepository::delete);
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

    @Transactional
    public boolean verifyUserEmailCode(String email, int verificationCode) {
        // User와 연관된 EmailVerification 정보를 조회
        EmailVerification verification = emailVerificationRepository.findByUserEmail(email)
                .orElseThrow(() -> new BadRequestException("이메일 검증 정보를 찾을 수 없습니다: " + email));

        // 만료 시간 체크
        if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("인증 코드가 만료되었습니다. 새로운 코드를 요청해주세요.");
        }

        // 인증 코드 비교
        if (verification.getVerificationCode() != verificationCode) {
            return false;
        }

        // 인증 성공 시 EmailVerification 정보 삭제
        emailVerificationRepository.delete(verification);
        return true;
    }

    // 랜덤 인증 코드 생성
    public int generateVerificationCode() {
        return 1000 + new Random().nextInt(9000); // 4자리 숫자 생성
    }

    private static final int MAX_ATTEMPTS = 5;

    // 재시도 횟수 체크 로직을 분리한 메서드
    public void checkFailedAttempts(int failedAttempts) {
        if (failedAttempts >= MAX_ATTEMPTS) {
            throw new BadRequestException("You have exceeded the maximum number of attempts. Please request a new verification code.");
        }
    }


}
