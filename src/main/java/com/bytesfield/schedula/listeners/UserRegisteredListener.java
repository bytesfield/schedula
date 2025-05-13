package com.bytesfield.schedula.listeners;

import com.bytesfield.schedula.config.rabbitmq.UserRegisteredRabbitMQConfig;
import com.bytesfield.schedula.dtos.UserDto;
import com.bytesfield.schedula.exceptions.UserNotFoundException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.entities.UserVerification;
import com.bytesfield.schedula.models.enums.UserVerificationChannel;
import com.bytesfield.schedula.models.enums.UserVerificationStatus;
import com.bytesfield.schedula.models.enums.UserVerificationType;
import com.bytesfield.schedula.repositories.UserRepository;
import com.bytesfield.schedula.repositories.UserVerificationRepository;
import com.bytesfield.schedula.services.utils.EmailService;
import com.bytesfield.schedula.utils.Helper;
import com.bytesfield.schedula.utils.security.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserRegisteredListener {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final EmailService emailService;

    @Value("${spring.application.url}")
    private String applicationUrl;

    public UserRegisteredListener(UserRepository userRepository, UserVerificationRepository userVerificationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.emailService = emailService;
    }

    @RabbitListener(queues = UserRegisteredRabbitMQConfig.QUEUE)
    public void listen(UserDto userDto) {
        String userEmail = userDto.getEmail();

        log.info("📩 Received user registered Email={} for scheduling", userEmail);

        User user = getUser(userEmail);

        handleUserRegistered(user);

    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void handleUserRegistered(User user) {
        log.info("🗓️ Handling UserRegistered task for user: {}", user.getEmail());

        UserVerification emailVerification = userVerificationRepository.findUserVerification(user, UserVerificationType.EMAIL, UserVerificationChannel.EMAIL);

        if (emailVerification != null) {
            this.sendEmail(user, emailVerification);
        }
    }

    private void sendEmail(User user, UserVerification emailVerification) {
        log.info("Sending email notification to {}", user.getEmail());

        try {
            Helper.retryWithBackoff(() -> {
                emailService.sendEmail(buidSendEMailData(user));
                return null;
            }, 3, 1000);

            emailVerification.setStatus(UserVerificationStatus.SENT);
            emailVerification.setSentAt(Instant.now());
        } catch (Exception ex) {
            log.error("❌ Sending email failed: ID={}, Error={}", emailVerification.getId(), ex.getMessage());
        } finally {
            userVerificationRepository.save(emailVerification);
        }
    }


    private SendEmailData buidSendEMailData(User user) {
        SendEmailData data = new SendEmailData();

        Map<String, Object> templateData = new HashMap<>();

        String verificationToken = EncryptionUtil.decrypt(user.getEmailVerificationToken());

        String verificationUrl = applicationUrl + "/verify?token=" + verificationToken;

        templateData.put("userName", user.getFirstName());
        templateData.put("verificationUrl", verificationUrl);

        data.setRecipient(user.getEmail());
        data.setSubject("Email Verification");
        data.setTemplateName("emails/email-verification-mail");
        data.setTemplateVariables(templateData);

        return data;
    }
}
