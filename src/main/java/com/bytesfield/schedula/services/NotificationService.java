package com.bytesfield.schedula.services;

import com.bytesfield.schedula.exceptions.ConflictException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.models.entities.Notification;
import com.bytesfield.schedula.models.entities.User;
import com.bytesfield.schedula.models.enums.NotificationStatus;
import com.bytesfield.schedula.models.enums.NotificationType;
import com.bytesfield.schedula.repositories.NotificationRepository;
import com.bytesfield.schedula.services.utils.EmailService;
import com.bytesfield.schedula.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final EmailService emailService;

    private final NotificationRepository notificationRepository;

    public void sendNotification(Notification notification) {
        try {
            Helper.retryWithBackoff(() -> {
                attemptSend(notification);
                return null;
            }, 3, 1000);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } catch (Exception ex) {
            handleFailure(notification, ex);
        } finally {
            notification.setAttemptNumber(notification.getAttemptNumber() + 1);
            notificationRepository.save(notification);
        }
    }

    private void attemptSend(Notification notification) {
        User user = notification.getTask().getUser();

        if (notification.getType() != NotificationType.EMAIL) {
            throw new ConflictException("Invalid notification type");
        }

        log.info("Sending email notification to {}", user.getEmail());

        emailService.sendEmail(buidSendEMailData(notification, user));
    }

    private void handleFailure(Notification notification, Exception ex) {
        log.error("Notification failed: {}", ex.getMessage());

        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(ex.getMessage());
        notification.setResponseStatus(500);
        notification.setResponseBody(ex.getMessage());
    }

    private SendEmailData buidSendEMailData(Notification notification, User user) {
        SendEmailData data = new SendEmailData();

        Map<String, Object> templateData = new HashMap<>();

        templateData.put("userName", user.getFirstName());
        templateData.put("taskTitle", notification.getTask().getTitle());

        data.setRecipient(user.getEmail());
        data.setSubject("Task Executed Notification");
        data.setTemplateName("emails/schedule-task-executed-mail");
        data.setTemplateVariables(templateData);

        return data;
    }
}
