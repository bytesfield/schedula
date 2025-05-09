package com.bytesfield.schedula.services;

import com.bytesfield.schedula.exceptions.EmailProcessingException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.services.providers.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final Map<String, EmailProvider> emailProviders;
    private final TemplateEngine templateEngine;

    @Value("${email.provider}")
    private String selectedProvider;

    public void sendEmail(SendEmailData data) {
        try {
            Context context = new Context();
            context.setVariables(data.getTemplateVariables());

            String htmlContent = getEmailContent(data, context);
            data.setHtmlContent(htmlContent);

            EmailProvider provider = emailProviders.get(selectedProvider);

            if (provider == null || !provider.getClass().isAnnotationPresent(Component.class)) {
                throw new IllegalArgumentException("Invalid email provider");
            }

            provider.sendEmail(data);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            
            throw new EmailProcessingException("Email delivery failed", e);
        }
    }

    private String getEmailContent(SendEmailData data, Context context) {
        if (data.getTemplateName() == null) {
            throw new IllegalArgumentException("Template name is required for email processing");
        }

        return templateEngine.process(data.getTemplateName(), context);
    }
}
