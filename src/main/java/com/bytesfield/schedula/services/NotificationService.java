package com.bytesfield.schedula.services;

import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.models.entities.Notification;
import com.bytesfield.schedula.models.enums.NotificationStatus;
import com.bytesfield.schedula.models.enums.NotificationType;
import com.bytesfield.schedula.repositories.NotificationRepository;
import com.bytesfield.schedula.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    private final NotificationRepository notificationRepository;

    public void sendNotification(Notification notification) throws Exception {
        try {
            if (Objects.requireNonNull(notification.getType()) == NotificationType.EMAIL) {
                log.info("Sending email notification to {}", notification.getTask().getUser().getEmail());
                //emailService.sendEmail(buidSendEMailData(notification));
            } else {
                throw new ConflictException("Invalid notification type");
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setAttemptNumber(notification.getAttemptNumber() + 1);
        } catch (Exception ex) {
            Helper.retryWithBackoff(() -> {
                handleFailure(notification, ex);
                return null;
            }, 3, 1000);
        } finally {
            notificationRepository.save(notification);
        }
    }


    private void handleFailure(Notification notification, Exception ex) throws Exception {
        log.error("Notification failed: {}", ex.getMessage());

        notification.setStatus(NotificationStatus.FAILED);
        notification.setAttemptNumber(notification.getAttemptNumber() + 1);
        notification.setErrorMessage(ex.getMessage());
        notification.setResponseStatus(500);
        notification.setResponseBody(ex.getMessage());

        notificationRepository.save(notification);

        sendNotification(notification);
    }

    private SendEmailData buidSendEMailData(Notification notification) {
        SendEmailData sendEmailData = new SendEmailData();

        sendEmailData.setRecipient(notification.getTask().getUser().getEmail());
        sendEmailData.setSubject("Task Executed Notification");
        sendEmailData.setTemplateName("email_template"); //Add your email template name here

        return sendEmailData;
    }
}
