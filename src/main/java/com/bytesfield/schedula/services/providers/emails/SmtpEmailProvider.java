package com.bytesfield.schedula.services.providers.emails;

import com.bytesfield.schedula.exceptions.EmailProcessingException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.services.providers.EmailProvider;
import com.bytesfield.schedula.utils.Helper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component("smtp")
@RequiredArgsConstructor
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(SendEmailData data) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(data.getRecipient());
            helper.setSubject(data.getSubject());
            helper.setText(data.getTextContent(), true);

            Helper.retryWithBackoff(() -> {
                mailSender.send(message);
                log.info("SMTP email sent successfully to {}", data.getRecipient());
                return null;
            }, 3, 500);
        } catch (MessagingException e) {
            log.error("Failed to send SMTP email to {}: {}", data.getRecipient(), e.getMessage(), e);

            throw new EmailProcessingException("Failed to send email via SMTP", e);
        } catch (Exception e) {
            throw new EmailProcessingException("An unexpected error occurred while sending email via SMTP", e);
        }
    }
}